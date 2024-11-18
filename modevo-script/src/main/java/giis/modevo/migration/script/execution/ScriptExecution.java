package giis.modevo.migration.script.execution;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.datastax.oss.driver.api.core.cql.ColumnDefinition;
import com.datastax.oss.driver.api.core.cql.ColumnDefinitions;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

import giis.modevo.migration.script.ColumnValue;
import giis.modevo.migration.script.For;
import giis.modevo.migration.script.Insert;
import giis.modevo.migration.script.Script;
import giis.modevo.migration.script.ScriptException;
import giis.modevo.migration.script.Select;
import giis.modevo.model.schema.Column;
import lombok.extern.slf4j.Slf4j;
@Slf4j
public class ScriptExecution {
	private static final String FROM = "FROM ";


	/**
	 * Main class to initiate the execution of the script
	 * @param c Object of the open session with the connection establishes to execute the script.
	 */
	public void execute(Script script, CassandraConnection c, String nameKeyspace) {
		if (!script.isExecutable()) {
			log.info("Script is not executable");
			return;
		}
		List<For> highLevelFors = script.getForsHigherLevel();
		List<For> splitFors = script.getForsSplit();
		for (For highFor : highLevelFors) {
			executionFor (highFor, script, c, nameKeyspace);
		}
		for (For splitFor : splitFors) {
			executionFor (splitFor, script, c, nameKeyspace);
		}
		
	}
	private void executionFor (For forToExecute, Script script, CassandraConnection c, String nameKeyspace) {
		List<Select> highLevelSelects = forToExecute.getSelectsFor();
		Select s = highLevelSelects.get(0); //For now there is only one select at most, change into a list when there will be more.
		String nameTable = s.getTable().getName();
		String selectStatementWithKeyspace = s.getSelectStatement().replace(FROM+nameTable,FROM+ "\""+nameKeyspace+"\"."+nameTable);
		ResultSet rs = c.executeStatement(selectStatementWithKeyspace);
		Iterator<Row> resultIt = rs.iterator();
		while (resultIt.hasNext()) {	
			Row rw = resultIt.next();
			List<ColumnValue> cvs = getColumnValuesSearchRow(rw, s);
			List<Select> selectsInside = forToExecute.getSelectsInsideFor();
			List<List<ColumnValue>> cvsFromWhere = executeSelects (cvs, selectsInside, c, nameKeyspace);
			executeInserts (script, cvs, cvsFromWhere, forToExecute, c, nameKeyspace);			
		}
	}
	/**
	 * Execution of the insert statements inside a FOR loop
	 * @param cvsFromWhere 
	 */
	private void executeInserts(Script script, List<ColumnValue> cvs, List<List<ColumnValue>> cvsFromWhere, For highFor, CassandraConnection c, String nameKeyspace) {
		for (Insert i: script.getInserts()) {
			if (i.getInsideFor().equals(highFor)){
				String insertStatement = i.getInsertStatement();
				String nameTableInsert = i.getNameTable();
				String statementInsertWithKeyspace = insertStatement.replace("INSERT INTO "+nameTableInsert, "INSERT INTO "+"\""+nameKeyspace+"\"."+nameTableInsert);
				List<String> insertsInside = new ArrayList<>();
				replaceJoinColumnVariables(statementInsertWithKeyspace, cvs); //For Join column schema changes
				for (ColumnValue cv : cvs) {
					statementInsertWithKeyspace=replaceVariableName (statementInsertWithKeyspace, cv, cvs);
				}
				for (List<ColumnValue> listColumnValues : cvsFromWhere) {
					List<String> replacedVariableNamesList = replaceVariableNamesListValues (listColumnValues, statementInsertWithKeyspace);
					insertsInside.addAll(replacedVariableNamesList);
				}
				if (cvsFromWhere.isEmpty()) {
					String statementWithEmptyValues = statementInsertWithKeyspace.replaceAll("\\$\\d+", "''");
					insertsInside.add(statementWithEmptyValues);	
				}
				for (String statementToExecute : insertsInside) {
					c.executeStatement(statementToExecute);
				}
			}
		}	
	}
	/**
	 * 	Replaces and concatenates all the insertions that come from a join column operation
	 */
	private void replaceJoinColumnVariables(String statementInsertWithKeyspace, List<ColumnValue> cvs) {
		Pattern pattern = Pattern.compile("\\$(\\d+)(\\+\\$(\\d+))*"); //Obtains all the joins that exist
		Matcher matcher = pattern.matcher(statementInsertWithKeyspace);
		while (matcher.find()) {
			String match = matcher.group();
			Pattern patternSingleVariable = Pattern.compile("\\$(\\d+)"); //Obtains each source value to be joined
			Matcher matcherSingleVariable = patternSingleVariable.matcher(match);
			StringBuilder sb = new StringBuilder();
			while (matcherSingleVariable.find()) {
				String variableName = matcherSingleVariable.group();
				for (ColumnValue cv : cvs) {
					if (cv.getVariableName().equals(variableName)) {
						sb.append(cv.getValue());
					}
				}
			}
			statementInsertWithKeyspace=statementInsertWithKeyspace.replace(match, sb.toString());
		}
	}
	/**
	 * Creates a list of statements with its values replaced by the values included in the list.
	 */
	private List<String> replaceVariableNamesListValues(List<ColumnValue> listColumnValues,
			String statementWithVariables) {
		List<String> listStatements = new ArrayList<>();
		String statementReplacedValues = statementWithVariables;
		for (ColumnValue cvInside : listColumnValues) {
			if (statementReplacedValues.contains(cvInside.getVariableName())) {
				statementReplacedValues=replaceVariableName (statementReplacedValues, cvInside, listColumnValues);
			}
		}
		if (!statementReplacedValues.equals(statementWithVariables))
			listStatements.add(statementReplacedValues);
		return listStatements;
	}
	/**
	 * Replaces the variable names of the statement given with the corresponding values from the list of values
	 */
	private String replaceVariableName(String insertStatement, ColumnValue cv, List<ColumnValue> cvs) {
		String variableName = cv.getVariableName();
		if (variableName == null) {
			variableName = cv.getColumnSelectOrigin().getVariableName();
		}
		ColumnValue cvValues = findCVNameVariable (cvs, variableName);
		if (cvValues == null) {
			return insertStatement.replace(variableName, "''");
		}
		return insertStatement.replace(variableName, "'"+cvValues.getValue()+"'");	
	}
	/**
	 * Execution of the Select statements inside a For loop
	 */
	private List<List<ColumnValue>> executeSelects(List<ColumnValue> cvs, List<Select> selectsInside, CassandraConnection c, String nameKeyspace) {
		List<List<ColumnValue>> columnValuesFromWhere = new ArrayList<>();
		for (Select selectInside : selectsInside) {
			String statementSelect = selectInside.getSelectStatement();
			selectInside.getWhereValue();
			ColumnValue primaryCV = null;
			for (Column whereValue: selectInside.getWhere()) {
				String vn = whereValue.getVariableName();
				primaryCV = findCVNameVariable(cvs, vn);
				if (primaryCV == null) {
					throw new ScriptException ("There has been error, the column "+whereValue.getName()+" did not have a value to insert.");
				}
				statementSelect=statementSelect.replace(vn, "'"+primaryCV.getValue()+"'");
			}
			String nameTableInside = selectInside.getTable().getName();
			String statementSelectWithKeyspace = statementSelect.replace(FROM+nameTableInside,FROM+ "\""+nameKeyspace+"\"."+nameTableInside);
			ResultSet rssecond = c.executeStatement(statementSelectWithKeyspace);
			Iterator<Row> rssecondIt = rssecond.iterator();
			while (rssecondIt.hasNext()) {
				List<ColumnValue> cvsIteration = new ArrayList<>();
				Row rws = rssecondIt.next();
				ColumnDefinitions cds = rws.getColumnDefinitions();
				Iterator<ColumnDefinition> it = cds.iterator();
				while (it.hasNext()) {
					ColumnDefinition cd = it.next();
					ColumnValue cv = new ColumnValue ();
					Column column = selectInside.getColumnSearch(cd.getName().asCql(true));
					column.setName(cd.getName().asCql(true));
					cv.setColumn(column);
					cv.setValue(rws.getString(cd.getName().asCql(true).trim()));
					cv.setVariableName(column.getVariableName());
					cvsIteration.add(cv);
				}
				columnValuesFromWhere.add(cvsIteration);
			}
		}
		return columnValuesFromWhere;
		
	}
	private List<ColumnValue> getColumnValuesSearchRow(Row rw, Select s) {
		List<ColumnValue> cvs = new ArrayList<>();
		for (Column c: s.getSearch()) {
			ColumnValue cv = new ColumnValue ();
			cv.setColumn(c);
			cv.setValue(rw.getString(c.getName()));
			cv.setVariableName(c.getVariableName());
			cvs.add(cv);
		}
		return cvs;
	}
	private ColumnValue findCVNameVariable(List<ColumnValue> cvs, String nameVariable) {
		for (ColumnValue cv : cvs) {
			if (cv.getVariableName().equalsIgnoreCase(nameVariable)) {
				return cv;
			}
		}
		return null;
	}
}

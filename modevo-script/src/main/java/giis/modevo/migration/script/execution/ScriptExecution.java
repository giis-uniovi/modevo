package giis.modevo.migration.script.execution;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
		for (For highFor : highLevelFors) {
			List<Select> highLevelSelects = highFor.getSelectsFor();
			Select s = highLevelSelects.get(0); //For now there is only one select at most, change into a list when there will be more.
			String nameTable = s.getTable().getName();
			String selectStatementWithKeyspace = s.getSelectStatement().replace(FROM+nameTable,FROM+ "\""+nameKeyspace+"\"."+nameTable);
			ResultSet rs = c.executeStatement(selectStatementWithKeyspace);
			Iterator<Row> resultIt = rs.iterator();
			while (resultIt.hasNext()) {	
				Row rw = resultIt.next();
				List<ColumnValue> cvs = getColumnValuesSearchRow(rw, s);
				List<Select> selectsInside = highFor.getSelectsInsideFor();
				executeSelects (cvs, selectsInside, c, nameKeyspace);
				executeInserts (script, cvs, highFor, c, nameKeyspace);			
			}
		}
	}
	/**
	 * Execution of the insert statements inside a FOR loop
	 */
	private void executeInserts(Script script, List<ColumnValue> cvs, For highFor, CassandraConnection c, String nameKeyspace) {
		for (Insert i: script.getInserts()) {
			if (i.getInsideFor().equals(highFor)){
				String insertStatement = i.getInsertStatement();
				for (ColumnValue cv : i.getColumnValue()) {
					String variableName = cv.getVariableName();
					if (variableName == null) {
						variableName = cv.getColumnSelectOrigin().getVariableName();
					}
					ColumnValue cvValues = findCVNameVariable (cvs, variableName);
					if (cvValues == null) {
						insertStatement= insertStatement.replace(variableName, "''");
						continue;
					}
					insertStatement= insertStatement.replace(variableName, "'"+cvValues.getValue()+"'");
				}
				String nameTableInsert = i.getNameTable();
				String statementInsertWithKeyspace = insertStatement.replace("INSERT INTO "+nameTableInsert, "INSERT INTO "+"\""+nameKeyspace+"\"."+nameTableInsert);
				c.executeStatement(statementInsertWithKeyspace);
			}
		}	
	}
	/**
	 * Execution of the Select statements inside a For loop
	 */
	private void executeSelects(List<ColumnValue> cvs, List<Select> selectsInside, CassandraConnection c, String nameKeyspace) {
		for (Select selectInside : selectsInside) {
			String statementSelect = selectInside.getSelectStatement();
			selectInside.getWhereValue();
			
			for (Column whereValue: selectInside.getWhere()) {
				String vn = whereValue.getVariableName();
				ColumnValue cv = findCVNameVariable(cvs, vn);
				if (cv == null) {
					throw new ScriptException ("There has been error, the column "+whereValue.getName()+" did not have a value to insert.");
				}
				statementSelect=statementSelect.replace(vn, "'"+cv.getValue()+"'");
			}
			String nameTableInside = selectInside.getTable().getName();
			String statementSelectWithKeyspace = statementSelect.replace(FROM+nameTableInside,FROM+ "\""+nameKeyspace+"\"."+nameTableInside);
			ResultSet rssecond = c.executeStatement(statementSelectWithKeyspace);
			Iterator<Row> rssecondIt = rssecond.iterator();
			while (rssecondIt.hasNext()) {
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
					cvs.add(cv);
				}
			}
		}
		
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

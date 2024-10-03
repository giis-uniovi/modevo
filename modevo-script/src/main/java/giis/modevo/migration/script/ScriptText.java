package giis.modevo.migration.script;

import java.util.List;

import giis.modevo.model.schema.Column;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ScriptText {
	private int counterVariables;

	public ScriptText() {
		counterVariables = 1;
	}
	/**
	 * Generates and returns the textual description of the script contained in the given parameter
	 */
	public String writeScript(Script s) {
		if (!s.isExecutable()) {
			log.warn("Not executable script because there are key columns that cannot be inserted");
			return "Not executable script because there are key columns that cannot be inserted";
		}
		StringBuilder sb = new StringBuilder();
		if (s.getForsSplit().isEmpty()) {
			For forNested = s.getFors().get(0);
			while (forNested != null) {
				writeSyntaxFor (sb, forNested, s);
				forNested = forNested.getNestedFor();
			}
		}
		else { //When there is a split
			List<For> forsSplit = s.getForsSplit();
			for (For forSplit:forsSplit) {
				writeSyntaxFor (sb, forSplit, s);
			}
		}
		writeSyntaxInsert (sb, s);
		log.info("Script screated");
		return sb.toString();
	}
	
	/**
	 * Writes all the Insert statements inside an script
	 */
	private void writeSyntaxInsert(StringBuilder sb, Script s) {
		List<Insert> insertsLocal = s.getInserts();
		for (Insert i: insertsLocal) {
			String nameTable = i.getTable().getName();
			if (!i.getNameNewTable().isEmpty()) {
				nameTable = i.getNameNewTable();
			}
			StringBuilder insertSB = new StringBuilder("INSERT INTO ").append(nameTable).append("(");
			StringBuilder namesColumns = new StringBuilder();
			StringBuilder insertPlaceholders = new StringBuilder();
			for (int j=0; j<i.getColumnValue().size();j++){
				if (j>0) {
					namesColumns.append(", ");
					insertPlaceholders.append(", ");
				}
				ColumnValue cv = i.getColumnValue().get(j);
				if (cv.getVariableName()==null) {
					cv.setVariableName(cv.getColumn().getVariableName());
				}
				if (cv.getVariableName()==null) {
					cv.setVariableName(cv.getColumnSelectOrigin().getVariableName());
				}
				String nameColumn = cv.getColumn().getName();
				String nameVariable=cv.getSelectOrigin().findNameVariable (cv.getColumn().getNameAttribute(), cv.getColumn().getNameEntity());
				if (nameVariable == null) {
					Column columnOrigin = cv.getSelectOrigin().getSplitColumn();
					nameVariable=columnOrigin.getVariableName();

				}
				namesColumns.append(nameColumn);
				insertPlaceholders.append(nameVariable);
			}
			insertSB.append(namesColumns).append(") VALUES (").append(insertPlaceholders).append(");\n");
			sb.append(insertSB);
			i.setInsertStatement(insertSB.toString());
		}
		
	}
	/**
	 * Writes the syntax for a For loop of the script and the Select statements inside of it
	 */
	private void writeSyntaxFor(StringBuilder sb, For forNested, Script se) {
		sb.append("FOR ");
		for (int k =0; k<forNested.getSelectsFor().size();k++) {
			if (k>0) {
				sb.append(" AND ");
			}
			Select s = forNested.getSelectsFor().get(k);
			buildSelect (s, sb, se);
		}
		sb.append("\n");
		for (Select s : forNested.getSelectsInsideFor()) {
			buildSelect (s, sb, se);
			sb.append("\n");
		}
	}
	/**
	 * Returns the Select statement as a string. It also adds the select to parameter sb with the association of the variable names for the textual script
	 */
	private String buildSelect(Select s, StringBuilder sb, Script se) {
		StringBuilder columns = new StringBuilder("SELECT ");
		for (int i=0; i<s.getSearch().size();i++) {
			if (i>0) {
				columns.append(", ");
				sb.append(",");
			}
			String variableName = "$"+counterVariables;
			s.getSearch().get(i).setVariableName(variableName);
			sb.append(variableName);
			columns.append(s.getSearch().get(i).getName());
			counterVariables++;
		}
		columns.append(" FROM ").append(s.getTable().getName());
		if (!se.getForsSplit().isEmpty()) {
			textCriteriaSplit (s, columns);
		}
		for (int j=0; j<s.getWhere().size();j++) {
			if (j==0) {
				columns.append(" WHERE ");
			}
			else {
				columns.append(" AND ");
			}
			Column ce = s.getWhere().get(j);
			String nameVariable = this.getNameVariableColumn (ce, se);
			ce.setVariableName(nameVariable);

			columns.append(ce.getName()).append("=").append(nameVariable);
		}
		if (!s.getWhere().isEmpty()) {
			columns.append(" ALLOW FILTERING");
		}
		sb.append(" = ").append(columns);
		s.setSelectStatement(columns.toString());
		return columns.toString();
		
	}

	private void textCriteriaSplit(Select s, StringBuilder columns) {
			columns.append(" WHERE ");
			String nameColumn = s.getSplitColumn().getName();
			columns.append(nameColumn);
			String criteriaOperator = s.getCriteriaOperator();
			switch (criteriaOperator) {
			case "eq":
				columns.append("=");
				break;
			case "g":
				columns.append(">");
				break;
			case "l":
				columns.append("<");
				break;
			case "ge":
				columns.append(">=");
				break;
			case "le":
				columns.append("<=");
				break;
			}
			columns.append("'"+s.getCriteriaValue()+"'");
			columns.append(" ALLOW FILTERING");
	}
	private String getNameVariableColumn(Column ce, Script s) {
		String nameAttribute = ce.getNameAttribute();
		String nameEntity = ce.getNameEntity();
		for (Select select : s.getSelects()) {
			for (Column column : select.getSearch()) {
				String nameAttributeCurrent = column.getNameAttribute();
				String nameEntityCurrent = column.getNameEntity();
				if (nameAttribute.equalsIgnoreCase(nameAttributeCurrent) && nameEntity.equalsIgnoreCase(nameEntityCurrent)) {
					return column.getVariableName(); //check if this can enter and still be null
				}
			}
		}
		log.warn("No variable name found for column %s", ce.getName());		
		return null;
	}
}

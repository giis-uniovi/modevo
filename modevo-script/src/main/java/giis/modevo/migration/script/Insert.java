package giis.modevo.migration.script;

import java.util.ArrayList;
import java.util.List;

import giis.modevo.model.schema.Column;
import giis.modevo.model.schema.Table;
import lombok.Getter;
import lombok.Setter;
@Getter @Setter
public class Insert {
	private List<ColumnValue> columnValue;
	private Table table;
	private For insideFor;
	private String insertStatement;
	private String nameNewTable; //Only used when a new table is required when adding a new column
	public Insert() {
		columnValue = new ArrayList<>();
		nameNewTable = "";
	}
	public Insert(Table table) {
		this();
		
		this.table = table;
	}
	public Insert(Table table, For insideFor) {
		this(table);
		this.insideFor = insideFor;
	}
	/**
	 * Adds in a ColumnValue object the information of the column to insert, the source of the data and, optionally, 
	 * the columns used to synchronize the migration
	 * @param key If there is no key, value is null
	 * @param target 
	 */
	public ColumnValue addColumnValue(Column columnSelect, Select s, String[] key, Column target) {
		ColumnValue cv = new ColumnValue ();
		cv.setColumn(target);
		s.getValuesExtracted().add(cv);
		cv.setKey(key);
		cv.setColumnSelectOrigin(columnSelect);
		this.getColumnValue().add(cv);
		return cv;
	}
	public String getNameNewTable() {
		return nameNewTable;
	}
	public void setNameNewTable(String nameNewTable) {
		this.nameNewTable = nameNewTable;
	}
	public String getNameTable() {
		String name = this.getTable().getName();
		if (!this.getNameNewTable().isEmpty()) {
			name = this.getNameNewTable();
		}
		return name;
	}
	
}

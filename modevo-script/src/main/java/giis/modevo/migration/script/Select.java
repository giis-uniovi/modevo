package giis.modevo.migration.script;

import java.util.ArrayList;
import java.util.List;

import giis.modevo.model.ModelUtilities;
import giis.modevo.model.schema.Column;
import giis.modevo.model.schema.Schema;
import giis.modevo.model.schema.Table;
import giis.modevo.model.schemaevolution.SchemaEvolution;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
@Getter @Setter @Slf4j
public class Select {
	private List<Column> search;
	private Table table;
	private List<ColumnValue> whereValue;
	private List<Column> where;
	private String selectStatement;
	private String criteriaOperator;
	private String criteriaValue;
	private Column splitColumn;
		
	public Select () {
		where = new ArrayList<>();
		search = new ArrayList<>();
		whereValue = new ArrayList<>();
	}

	public Select(Table table, Column c) {
		this();
		this.table = table;
		search.add(c);
	}

	public Select(Table table) {
		this ();
		this.table = table;
	}
	public boolean includesAllColumns(List<Column> where2) {
		for (Column c: where2) {
			String nameAttribute = c.getNameAttribute();
			String nameEntity = c.getNameEntity();
			if (!columnInSearch(nameEntity, nameAttribute)) {
				return false;
			}
		}
		return true;
		
	}
	private boolean columnInSearch(String nameEntity, String nameAttribute) {
		for (Column c: search) {
			if (c.getNameEntity().equalsIgnoreCase(nameEntity) && c.getNameAttribute().equalsIgnoreCase(nameAttribute)) {
				return true;
			}
		}
		return false;
	}
	public String findNameVariable(String nameAttribute, String nameEntity) {
		for (Column c: search) {
			if (c.getNameAttribute().equalsIgnoreCase(nameAttribute) && c.getNameEntity().equalsIgnoreCase(nameEntity)) {
				return c.getVariableName();
			}
		}
		log.error("No column found for entity %s and attribute %s. There is an inconsistency in the input models", nameEntity, nameAttribute);
		return null;
		
	}

	public Column getColumnSearch(String name) {
		for (Column c: search) {
			if (c.getName().equalsIgnoreCase(name)) {
				return c;
			}
		}
		log.error("No column %s found, there is an inconsistency in the input models", name);
		return null;
	}

	public void addColumnSearch(Schema schema, SchemaEvolution se, String nameTable, Column c) {
		this.getSearch().add(c);
		if (this.getTable() == null) {
			this.setTable(new ModelUtilities().findTable(schema, se, nameTable));
		}
		
	}

	public void addWhere(Schema schema, String[] keyColumnFrom) {
		for (String keyColumn:keyColumnFrom ) {
			Column cFrom= schema.getColumn(table.getName(), keyColumn);
			this.getWhere().add(cFrom);
		}
		
	}
	/**
	 * Gets the SELECT statement that uses in the Where the columns from the parameter
	 */
	public Select getSelectSourceValueWhere(List<Column> where, List<Select> selects) {
		log.info("Get existing SELECT for columns %s", where.toString());
		for (Select s: selects) {
			boolean includesColumns = s.includesAllColumns (where);
			if (includesColumns) {
				return s;
			}
		}
		log.info("No SELECT found for columns %s", where.toString());
		return null;
		
	}
	/**
	 * Gets the SELECT statement that queries the given table
	 */
	public Select getSelectTable(List<Select> selects, String nameTable) {
		for (Select s: selects) {
			if (s.getTable().getName().equalsIgnoreCase(nameTable)) {
				return s;
			}
		}
		log.error("No table found, there is an inconsistency in the input models");
		return null;
	}
}

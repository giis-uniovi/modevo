package giis.modevo.model.schema;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Java class for the Table metaclass of the Schema metamodel. It contains a
 * list of columns that compound the table. It must always contain a table name
 * and at least one column.
 */
@Getter @Setter
public class Table {

	private List<Column> columns;
	private String name;

	public Table(String name) {
		super();
		this.setName(name);
		columns = new ArrayList<>();
	}

	public Table(String name, List<Column> columns) {
		super();
		this.setName(name);
		this.columns = columns;
	}

	/**
	 * @return a list of columns that are part of the primary key (clustering and
	 *         partition key)
	 */
	public List<Column> getKey() {
		List<Column> keys = new ArrayList<>();
		for (Column c : columns) {
			if (c.isPk() || c.isCk()) {
				keys.add(c);
			}
		}
		return keys;
	}

	public Column getColumn(String nameColumn) {
		for (Column c : columns) {
			if (c.getName().equalsIgnoreCase(nameColumn)) {
				return c;
			}
		}
		return null;
	}

}

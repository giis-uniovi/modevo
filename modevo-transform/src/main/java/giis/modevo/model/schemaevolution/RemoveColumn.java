package giis.modevo.model.schemaevolution;

import giis.modevo.model.schema.Column;
import giis.modevo.model.schema.Table;

/**
 * Subclass of SchemaChange to define a column to be removed from a table. It
 * must always contain the object of the column to be removed and the table
 * where it belongs.
 */
public class RemoveColumn extends SchemaChange {

	public RemoveColumn(Column c, Table t) {
		super(c, t);
	}

}

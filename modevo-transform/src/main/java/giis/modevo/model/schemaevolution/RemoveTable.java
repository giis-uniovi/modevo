package giis.modevo.model.schemaevolution;

import giis.modevo.model.schema.Table;

/**
 * Subclass of SchemaChange to define a table to be removed from the schema. It
 * must always contain the object of the table to be removed.
 */
public class RemoveTable extends SchemaChange {

	public RemoveTable(Table t) {
		super(t);
	}

}

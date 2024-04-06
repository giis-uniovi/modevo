package giis.modevo.model.schemaevolution;

import giis.modevo.model.schema.Column;
import giis.modevo.model.schema.Table;

/**
 * Subclass of SchemaChange to define a new column that is added to the system.
 * It must always contain the column to be upgraded as pk and the table where it
 * is going to be added.
 */
public class AddPK extends SchemaChange {

	public AddPK(Column c, Table t) {
		super(c, t);
	}

}

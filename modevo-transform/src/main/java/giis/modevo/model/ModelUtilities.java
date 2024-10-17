package giis.modevo.model;

import giis.modevo.model.schema.Column;
import giis.modevo.model.schema.Schema;
import giis.modevo.model.schema.Table;
import giis.modevo.model.schemaevolution.SchemaEvolution;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ModelUtilities {
	
	/**
	 * 	Returns the table object from either the Schema or the Schema Evolution model
	 */
	public Table findTable(Schema schema, SchemaEvolution se, String name) {
		Table table = schema.getTable(name);
		if (table != null) {
			return table;
		}
		else {
			log.info("Table %s not found in the schema model. Start search on the schema evolution model.", name);
			return se.getTable(name);
		}
	}
	
	/**
	 * 	Returns the column object from either the Schema or the Schema Evolution model
	 */
	public Column findColumn(Schema schema, SchemaEvolution se, String nameTable, String nameColumn) {
		Table t = findTable (schema, se, nameTable);
		Column c = t.getColumn(nameColumn);
		if (c==null) {
			c = se.getTable(nameTable).getColumn(nameColumn);
		}
		return c;
	}
}

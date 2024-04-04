package giis.modevo.model;

import giis.modevo.model.datamigration.DataMigration;
import giis.modevo.model.schema.Schema;
import giis.modevo.model.schemaevolution.SchemaEvolution;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * Encapsulates into an object the models Schema, Schema Evolution and Data Migration from an evolution of the schema in objects. 
 * 
 */
@Getter @Setter
public class ModelObjects {
	private Schema schema;
	private SchemaEvolution schemaEvolution;
	private DataMigration dataMigration;
	
	public ModelObjects(Schema schema, SchemaEvolution schemaEvolution, DataMigration dataMigration) {
		super();
		this.schema = schema;
		this.schemaEvolution = schemaEvolution;
		this.dataMigration = dataMigration;
	}
	
}

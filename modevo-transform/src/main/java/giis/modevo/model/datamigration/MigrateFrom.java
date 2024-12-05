package giis.modevo.model.datamigration;

import lombok.Getter;
import lombok.Setter;

/**
 * Java class for the MigrateFrom metaclass of the DataMigration metamodel. It
 * contains the specifications of how the data is obtained from a source table.
 * It must always contain a value associated to Data, in order to specify the
 * column where the data is obtained. If the migration requires dependencies, it
 * must also contain at least one value in the key array, in order to identify
 * the columns used for synchronizing the migration.
 */
@Getter @Setter
public class MigrateFrom {

	private String data; // Column where data is obtained or migrated
	private String[] key; // Columns used to control the data migration
	private String table; // Name of the table

	public MigrateFrom () {
		key = new String[0];
	}
	/**
	 * It receives the names of the columns that are part of the primary key
	 */
	public void setKey(String[] key) {
		this.key = new String[key.length];
		for (int i = 0; i < this.key.length; i++) {
			this.key[i] = key[i].trim();
		}
	}

}

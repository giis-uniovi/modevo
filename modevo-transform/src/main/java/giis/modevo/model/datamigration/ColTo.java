package giis.modevo.model.datamigration;

import giis.modevo.model.schema.Column;
import lombok.Getter;
import lombok.Setter;

/**
 * Java class for the ColFrom metaclass of the DataMigration metamodel. Class
 * ColTo extends ColFrom as ColTo columns contain the same attributes and new
 * ones. It contains the specifications of how the data is migrated to a target
 * Table. It must always contain a value associated to Data, in order to specify
 * the column where the data is migrated. If the migration requires
 * dependencies, it must also contain at least one value in the key array, in
 * order to identify the columns used to synchronize the migration.
 */
@Getter @Setter
public class ColTo extends ColFrom {

	private boolean dataKey;
	private String newNameTable; // Name of the new table version if required
	public boolean isColumnKey(Column c) {
		for (String nameColumn : super.getKey()) {
			if (c.getName().equals(nameColumn)) {
				return true;
			}
		}
		return false;
	}

}

package giis.modevo.model.datamigration;

import lombok.Getter;
import lombok.Setter;

/**
 * Java class for the MigrationColumn metaclass of the DataMigration metamodel.
 * It contains the required information to migrate data for a specific column
 * specified in the children objects MigrateTo and MigrateFrom
 */
@Getter @Setter
public class MigrationColumn {

	private String name;
	private MigrateTo migrateTo; // Contains the required information to migrate data to a column
	private MigrateFrom migrateFrom; // Contains the required information to migrate data from a column
	private String description; //when it is not possible to proceed with the migration

	public MigrationColumn() {
		super();
		migrateTo = new MigrateTo();
		migrateFrom = new MigrateFrom();
	}

	public MigrationColumn(String name) {
		super();
		this.name = name;
	}

	public MigrationColumn(MigrationColumn c) {
		super();
		this.name = c.name;
	}
	
}

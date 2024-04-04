package giis.modevo.model.datamigration;

import lombok.Getter;
import lombok.Setter;

/**
 * Java class for the MigrationColumn metaclass of the DataMigration metamodel.
 * It contains the required information to migrate data for a specific column
 * specified in the children objects ColTo and ColFrom
 */
public class MigrationColumn {

	private String name;
	private ColTo colTo; // Contains the required information to migrate data to a column
	private ColFrom colFrom; // Contains the required information to migrate data from a column
	private MigrationTable table;
	private String description; //when it is not possible to proceed with the migration

	public MigrationColumn() {
		super();
		colTo = new ColTo();
		colFrom = new ColFrom();
	}

	
	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public ColTo getColTo() {
		return colTo;
	}


	public void setColTo(ColTo colTo) {
		this.colTo = colTo;
	}


	public ColFrom getColFrom() {
		return colFrom;
	}


	public void setColFrom(ColFrom colFrom) {
		this.colFrom = colFrom;
	}


	public MigrationTable getTable() {
		return table;
	}


	public void setTable(MigrationTable table) {
		this.table = table;
	}


	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		this.description = description;
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

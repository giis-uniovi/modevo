package giis.modevo.model.schema;

import lombok.Getter;
import lombok.Setter;

/**
 * Java class for the Column metaclass of the Schema metamodel. It contains the
 * attributes that are related for a Column. It must always contain a name, an
 * attribute name associated to the column, if it is part of the primary key,
 * the name of the entity from where there associated attribute belongs and the
 * Table object that contains the column.
 */
@Getter @Setter
public class Column {

	private String name;
	private String nameAttribute; // Attribute associated to the column in the conceptual model
	private boolean pk;
	private boolean ck;
	private String dataType;
	private String nameEntity;
	private String variableName;
	private String nameTable;
	public Column() {
		super();
	}

	public Column(String name, String nameAttribute, boolean pk, boolean ck, String nameEntity, String nameTable) {
		super();
		this.name = name;
		this.pk = pk;
		this.ck = ck;
		this.setNameAttribute(nameAttribute);
		this.nameEntity = nameEntity;
		this.nameTable = nameTable;

	}

	public Column(Column c) {
		super();
		this.name = c.name;
		this.pk = c.pk;
		this.ck = c.ck;
		this.setNameAttribute(c.nameAttribute);
		this.dataType = c.dataType;
		this.nameEntity = c.nameEntity;
		this.variableName = c.variableName;
		this.nameTable = c.nameTable;
	}

	public Column(String name) {
		this.name = name;
	}
	
	public boolean equalsValues (Column c) {
		return c.getName().equals(this.getName()) && c.getNameTable().equals(this.getNameTable()) && c.getNameAttribute().equals(this.getNameAttribute()) && c.getNameEntity().equals(this.getNameEntity());
	}

}

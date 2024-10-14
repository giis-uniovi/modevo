package giis.modevo.model.datamigration;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import giis.modevo.model.DocumentUtilities;
import giis.modevo.model.schema.Column;
import giis.modevo.model.schema.Schema;
import giis.modevo.model.schema.Table;
import giis.modevo.model.schemaevolution.AddColumn;
import giis.modevo.model.schemaevolution.AddTable;
import giis.modevo.model.schemaevolution.CopyTable;
import giis.modevo.model.schemaevolution.JoinColumn;
import giis.modevo.model.schemaevolution.JoinTable;
import giis.modevo.model.schemaevolution.SchemaChange;
import giis.modevo.model.schemaevolution.SchemaEvolution;
import giis.modevo.model.schemaevolution.SplitColumn;
import giis.modevo.model.schemaevolution.SplitTable;
import lombok.Getter;
import lombok.Setter;

/**
 * Main Java Class of the DataMigration metamodel. It contains a list of
 * MigrationTable objects, one for each table that requires data migrations. The
 * detail of the migrations for each of these tables are specified in the
 * classes MigrationColumn of each MigrationTable
 */
@Getter @Setter
public class DataMigration {
	
	private List<MigrationTable> migrationTables;

	public void addTable(MigrationTable t) {
		if (migrationTables == null) {
			migrationTables = new ArrayList<>();
		}
		migrationTables.add(t);
	}

	/**
	 * Creates and returns an object that stores in a Java object the content of a data
	 * migration model. It also required the objects of the Schema Evolution and Schema models.
	 */
	public DataMigration readDataMigrationModel(String dataMigrationPath, SchemaEvolution schemaEvolution, Schema schema) {
		DataMigration dataMigration = new DataMigration();
		Document doc = new DocumentUtilities().readDocumentGeneric(dataMigrationPath);
		NodeList list = doc.getElementsByTagName("MigrationTable");
		for (int temp = 0; temp < list.getLength(); temp++) {
			Node node = list.item(temp);
			NodeList lista = node.getChildNodes();
			Element nodeMigTab = (Element) node;
			String nameTable = nodeMigTab.getAttribute("name");
			MigrationTable t = new MigrationTable(nameTable);
			dataMigration.addTable(t);
			for (int i = 0; i < lista.getLength(); i++) {
				Node nodeColumn = lista.item(i);
				if (nodeColumn.getNodeType() == Node.ELEMENT_NODE) {
					MigrationColumn c = storeInfoMigrationColumn(nodeColumn, schemaEvolution, schema, t);
					if (c != null) {
						t.getMigrationColumns().add(c);
					}
				}
			}
		}
		return dataMigration;
	}

	/**
	 * Stores in a MigrationColumn object the information of a column that requires data migrations
	 */
	private MigrationColumn storeInfoMigrationColumn(Node nodeColumn, SchemaEvolution schemaEvolution, Schema schema, MigrationTable migrationTable) {

		MigrationColumn c = new MigrationColumn();
		Element elementMigCol = (Element) nodeColumn; // element migcol
		if (!elementMigCol.getAttribute("Description").isEmpty()) {
			return null;
		}
		NodeList toFrom = nodeColumn.getChildNodes();
		// Right now it's only for one from table and one to table
		ColFrom colfrom = new ColFrom();
		ColTo colto = new ColTo();
		for (int j = 0; j < toFrom.getLength(); j++) {
			Node nodeToFrom = toFrom.item(j);
			if (nodeToFrom.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) nodeToFrom;
				if (element.getNodeName().equalsIgnoreCase("colfrom")) {
					colfrom = storeInfoColFrom(element);
				} else if (element.getNodeName().equalsIgnoreCase("colto")) {
					colto = storeInfoColTo(element, schemaEvolution, schema);
					migrationTable.setNewTableName(colto.getNewNameTable());
				}
			}
		}
		c.setColFrom(colfrom);
		c.setColTo(colto);
		return c;
	}

	/**
	 * 	Stores in a ColTo object its data and returns it
	 */
	private ColTo storeInfoColTo(Element element, SchemaEvolution se, Schema sc) {
		String nameColTo = "";
		ColTo colto = new ColTo();
		String table = element.getAttribute("DataTable");
		nameColTo = element.getAttribute("Data");
		String key = element.getAttribute("Key");
		colto.setTable(table);
		colto.setData(nameColTo);
		colto.setDataKey(isColumnKey(table, nameColTo, se, sc));
		String newNameTable = element.getAttribute("NewTableName");
		colto.setNewNameTable(newNameTable);
		String[] keys = key.split(",");
		trimmingArray(keys);
		if (keys.length == 1 && keys[0].equalsIgnoreCase("")) {
			colto.setKey(new String[0]);
		} else {
			colto.setKey(keys);
		}
		return colto;
	}
	
	/**
	 * 	Stores in a ColFrom object its data and returns it
	 */
	private ColFrom storeInfoColFrom(Element element) {
		String table = element.getAttribute("DataTable");
		String column = element.getAttribute("Data");
		String key = element.getAttribute("Key");
		ColFrom colfrom = new ColFrom();
		colfrom.setTable(table);
		colfrom.setData(column);
		String[] keys = key.split(",");
		trimmingArray(keys);
		if (keys.length == 1 && keys[0].equalsIgnoreCase("")) {
			colfrom.setKey(new String[0]);
		} else {
			colfrom.setKey(keys);
		}
		return colfrom;
	}

	private boolean isColumnKey(String nameTable, String nameCol, SchemaEvolution se, Schema sc) {
		Column column = sc.getColumn(nameTable, nameCol);
		if (column != null) {
			return column.isCk() || column.isPk();
		}
		for (SchemaChange change : se.getChanges()) {
			if (foundAndTrue(change, nameCol)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Searches for a column in the given schema change and returns true if found it
	 */
	private boolean foundAndTrue(SchemaChange change, String nameCol) {
		Column column;
		if (change instanceof AddColumn) {
			column = change.getC();
			if (column.getName().equalsIgnoreCase(nameCol)) {
				return column.isCk() || column.isPk();
			}
		} else if (change instanceof AddTable || change instanceof SplitColumn || change instanceof JoinColumn) {
			column = change.getT().getColumn(nameCol);
			checkColumnKey(column);
		} else if (change instanceof JoinTable joinTableChange) {
			Table t = joinTableChange.getNewTable();
			column = t.getColumn(nameCol);
			checkColumnKey(column);
		} else if (change instanceof CopyTable copyTableChange) {
			Table t = copyTableChange.getCopiedTable();
			column = t.getColumn(nameCol);
			checkColumnKey(column);
		} else if (change instanceof SplitTable splittedTableChange) {
			List<Table> tables = splittedTableChange.getSplittedTables();
			for (Table t : tables) {
				column = t.getColumn(nameCol);
				checkColumnKey(column);
			}
		}
		return false;
	}

	private boolean checkColumnKey(Column column) {
		if (column != null) {
			return column.isCk() || column.isPk();
		}
		return false;
	}
	
	private void trimmingArray(String[] keys) {
		for (int i = 0; i < keys.length; i++) {
			keys[i] = keys[i].trim();
		}
	}
	
}
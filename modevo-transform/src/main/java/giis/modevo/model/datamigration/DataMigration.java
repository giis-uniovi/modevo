package giis.modevo.model.datamigration;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import giis.modevo.model.DocumentReader;
import giis.modevo.model.schema.Column;
import giis.modevo.model.schema.Schema;
import giis.modevo.model.schema.Table;
import giis.modevo.model.schemaevolution.AddColumn;
import giis.modevo.model.schemaevolution.AddTable;
import giis.modevo.model.schemaevolution.CopyTable;
import giis.modevo.model.schemaevolution.MergeColumn;
import giis.modevo.model.schemaevolution.MergeTable;
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
		Document doc = new DocumentReader().readDocumentGeneric(dataMigrationPath);
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
		MigrateFrom migrateFrom = new MigrateFrom();
		MigrateTo migrateTo = new MigrateTo();
		for (int j = 0; j < toFrom.getLength(); j++) {
			Node nodeToFrom = toFrom.item(j);
			if (nodeToFrom.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) nodeToFrom;
				if (element.getNodeName().equalsIgnoreCase("MigrateFrom")) {
					migrateFrom = storeInfoMigrateFrom(element);
				} else if (element.getNodeName().equalsIgnoreCase("MigrateTo")) {
					migrateTo = storeInfoMigrateTo(element, schemaEvolution, schema);
					migrationTable.setNewTableName(migrateTo.getNewNameTable());
				}
			}
		}
		c.setMigrateFrom(migrateFrom);
		c.setMigrateTo(migrateTo);
		return c;
	}

	/**
	 * 	Stores in a MigrateTo object its data and returns it
	 */
	private MigrateTo storeInfoMigrateTo(Element element, SchemaEvolution se, Schema sc) {
		String nameMigrateTo = "";
		MigrateTo migrateTo = new MigrateTo();
		String table = element.getAttribute("DataTable");
		nameMigrateTo = element.getAttribute("Data");
		String key = element.getAttribute("Key");
		migrateTo.setTable(table);
		migrateTo.setData(nameMigrateTo);
		migrateTo.setDataKey(isColumnKey(table, nameMigrateTo, se, sc));
		String newNameTable = element.getAttribute("NewTableName");
		migrateTo.setNewNameTable(newNameTable);
		String[] keys = key.split(",");
		trimmingArray(keys);
		if (keys.length == 1 && keys[0].equalsIgnoreCase("")) {
			migrateTo.setKey(new String[0]);
		} else {
			migrateTo.setKey(keys);
		}
		return migrateTo;
	}
	
	/**
	 * 	Stores in a MigrateFrom object its data and returns it
	 */
	private MigrateFrom storeInfoMigrateFrom(Element element) {
		String table = element.getAttribute("DataTable");
		String column = element.getAttribute("Data");
		String key = element.getAttribute("Key");
		MigrateFrom migrateFrom = new MigrateFrom();
		migrateFrom.setTable(table);
		migrateFrom.setData(column);
		String[] keys = key.split(",");
		trimmingArray(keys);
		if (keys.length == 1 && keys[0].equalsIgnoreCase("")) {
			migrateFrom.setKey(new String[0]);
		} else {
			migrateFrom.setKey(keys);
		}
		return migrateFrom;
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
		} else if (change instanceof AddTable || change instanceof SplitColumn || change instanceof MergeColumn) {
			column = change.getT().getColumn(nameCol);
			checkColumnKey(column);
		} else if (change instanceof MergeTable joinTableChange) {
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
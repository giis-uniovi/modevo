package giis.modevo.model.datamigration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import giis.modevo.model.schema.Schema;
import giis.modevo.model.schemaevolution.MergeColumn;
import giis.modevo.model.schemaevolution.RemovePK;
import giis.modevo.model.schemaevolution.SchemaChange;
import giis.modevo.model.schemaevolution.SchemaEvolution;
import giis.modevo.model.schemaevolution.SplitColumn;
import lombok.Getter;
import lombok.Setter;

/**
 * Java class for the MigrationTable metaclass of the DataMigration metamodel.
 * It contains a list of MigrationColumn objects, which each one details the
 * migrations for a column in their children objects MigrateTo and MigrateFrom.
 */
@Getter @Setter
public class MigrationTable {

	private String name;
	private String newTableName;
	private List<MigrationColumn> migrationColumns;

	public MigrationTable() {
		super();
		migrationColumns = new ArrayList<>();
	}
	public MigrationTable(String nameTable) {
		this();
		name = nameTable;
	}
	public boolean migrationFromSeveralTablesNewTable (Schema schema) {
		boolean newTable = isNewTableInSchema(schema);
		if (!newTable) {
			return false;
		}
		return !commonCodeCheckOneTableMigration(); //opposite value
	}
	public List<MigrationColumn> getMigrationColumnsWithoutKey() {
		List<MigrationColumn> mcwithoutkey = new ArrayList<>();
		for (MigrationColumn mc : migrationColumns) {
			if (mc.getMigrateFrom().getKey().length == 0) {
				mcwithoutkey.add(mc);
			}
		}
		return mcwithoutkey;
	}
	public List<MigrationColumn> getMigrationColumnsWithKey() {
		List<MigrationColumn> mcwithkey = new ArrayList<>();
		for (MigrationColumn mc : migrationColumns) {
			if (mc.getMigrateFrom().getKey().length > 0) {
				mcwithkey.add(mc);
			}
		}
		return mcwithkey;
	}
	public boolean migrationFromOneTableNewTable(Schema schema) {
		boolean newTable = isNewTableInSchema(schema);
		if (!newTable) {
			return false;
		}
		return commonCodeCheckOneTableMigration();
		
	}
	/**
	 * Returns true if the table that needs data migrations is new.
	 */
	private boolean isNewTableInSchema (Schema schema) {
		//If this condition is met, is because it is a change that does not require data migrations
		if (migrationColumns.isEmpty()) {
			return false;
		}
		MigrationColumn first=migrationColumns.get(0);
		MigrateTo ct = first.getMigrateTo();
		String nameTableMigrateTo = ct.getTable();
		return schema.getTable(nameTableMigrateTo) == null;
	}
	/**
	 * Determines if the data migration comes from one table (returns True) or from more (returns False)
	 */
	private boolean commonCodeCheckOneTableMigration () {
		MigrationColumn first=migrationColumns.get(0);
		MigrateFrom cf = first.getMigrateFrom();
		String nameTable = cf.getTable();
		for (int i = 1; i<migrationColumns.size(); i++) {
			MigrationColumn current=migrationColumns.get(i);
			MigrateFrom cfcurrent = current.getMigrateFrom();
			String nameTableCurrent = cfcurrent.getTable();
			if (!nameTable.equalsIgnoreCase(nameTableCurrent) && nameTableCurrent != null) {
				return false;
			}
		}
		return true;
	}
	public boolean migrationNewColumn(Schema schema) {
		boolean newTable = isNewTableInSchema(schema);
		return !newTable && !migrationColumns.isEmpty();
	}
	
	/**
	 * Classifies the migration columns by the source table where the data to be migrated to that column will be obtained
	 */
	public Map<String, List<MigrationColumn>> classifyMCsBySourceTable(List<MigrationColumn> migCols) {
		Map<String, List<MigrationColumn>> mcsBySourceTable = new HashMap<>();
		for (MigrationColumn mc : migCols) {
			String nameTable = mc.getMigrateFrom().getTable().toLowerCase();
			mcsBySourceTable.computeIfAbsent(nameTable, k -> new ArrayList<>());
			mcsBySourceTable.get(nameTable).add(mc);
		}
		return mcsBySourceTable;
	}
	public boolean migrationFromRemovePK(SchemaEvolution se, MigrationTable mt) {
		for (SchemaChange sc : se.getChanges()) {
			if (sc instanceof RemovePK rpk) {
				for (MigrationColumn mc : mt.getMigrationColumns()) {
					if (mc.getMigrateFrom().getTable().equalsIgnoreCase(rpk.getNamePreviousTable())) {
						return true;
					}
				}
			}
		}
		return false;
	}
	/**
	 * Checks if the migration script contains a split of a column
	 */
	public boolean migrationSplitColumn(SchemaEvolution se) {
		for (SchemaChange sc : se.getChanges()) {
			if (sc instanceof SplitColumn) {
				return true;
			}
		}
		return false;
	}
	public boolean migrationJoinColumn(SchemaEvolution se) {
		for (SchemaChange sc : se.getChanges()) {
			if (sc instanceof MergeColumn) {
				return true;
			}
		}
		return false;
	}

	
}

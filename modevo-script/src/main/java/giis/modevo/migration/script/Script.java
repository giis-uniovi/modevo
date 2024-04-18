package giis.modevo.migration.script;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import giis.modevo.model.ModelUtilities;
import giis.modevo.model.datamigration.DataMigration;
import giis.modevo.model.datamigration.MigrationColumn;
import giis.modevo.model.datamigration.MigrationTable;
import giis.modevo.model.schema.Column;
import giis.modevo.model.schema.Schema;
import giis.modevo.model.schema.Table;
import giis.modevo.model.schemaevolution.SchemaEvolution;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Main class for the creation of the script. First, it receives the information of the models (Schema, Schema evolution and Data Migration) to create a Script object
 * that contains all the necessary data to execute the data migration.
 */
@Getter @Setter @Slf4j
public class Script {

	private static final String MIGRATION_FOR_COLUMN = "Migration for column %s";
	
	private List<Select> selects;
	private List<For> fors;
	private List<For> forsHigherLevel; //These are the For loops that are not inside other For loops
	private List<Insert> inserts;
	private ScriptText scriptText;
	private boolean executable;
	public Script () {
		setExecutable(true);//default value
		selects = new ArrayList<>();
		fors = new ArrayList<>();
		inserts = new ArrayList<>();
		setForsHigherLevel(new ArrayList<>());
		scriptText = new ScriptText();
	}	
	public boolean isExecutable() {
		return executable;
	}
	public void setExecutable(boolean executable) {
		this.executable = executable;
	}
	/**
	 * Main class to start the creation of the script objects
	 */
	public List<Script> createScript (Schema schema, SchemaEvolution se, DataMigration dm) {
		List<Script> scripts = new ArrayList<>();
		for (MigrationTable mt : dm.getMigrationTables()) {
			if ( mt.migrationFromSeveralTablesNewTable(schema)) {
				scripts.add(migrationNewTable (schema, se, mt, true));
			}
			else if (mt.migrationFromOneTableNewTable(schema)) {
				scripts.add(migrationNewTable (schema, se, mt, false));
			}
			else if (mt.migrationNewColumn(schema) && !mt.migrationFromRemovePK(se, mt)) {
				scripts.add(migrationNewColumn (schema, se, mt));
			}
			else {
				return new ArrayList<>(); //Scenarios not implemented
			}
		}
		checkIfExecutable(scripts);
		return scripts;
	}

	private void checkIfExecutable(List<Script> scripts) {
		for (Script s: scripts) {
			for (Insert i: s.getInserts()) {
				List<Column> keys =i.getTable().getKey();
				for (Column key : keys) {
					if (!inList(key, i.getColumnValue())) {
						s.setExecutable(false);
					}
				}
			}
		}
		
	}
	private Script migrationNewTable (Schema schema, SchemaEvolution se, MigrationTable mt, boolean severalTables) {
		Script s = migrationNewTableAll(schema, se, mt);
		if (severalTables) {
			migrationFromSeveralTables (schema, se, mt, s);
		}
		return s;
	}
	/**
	 * Adds the neccesary information for an existing script of a new migration for a new table when the data is obtained from more than one table
	 */
	private Script migrationFromSeveralTables(Schema schema, SchemaEvolution se, MigrationTable mt, Script script) {
		log.info("New Table Script from several source tables. Target table: %s", mt.getName());
		ModelUtilities mu = new ModelUtilities ();
		List <MigrationColumn> migrationColumnsWithKey = mt.getMigrationColumnsWithKey();
		//These are always the first insert and select of the script which had been added for the columns that do not require key columns for the migrations
		Insert insert = script.getInserts().get(0);
		Select select = script.getSelects().get(0);
		/*
		 * Association in the Insert statement of the columns that require an addition query to another table different from the one being looped in the FOR.
		 * These additional queries will use data in the WHERE clause obtained from their iteration of the loop
		 */
		for (MigrationColumn mc: migrationColumnsWithKey) {
			log.info(MIGRATION_FOR_COLUMN, mc.getName());
			String nameTable = mc.getColFrom().getTable();
			if (select.getTable() == null) {
				select.setTable(mu.findTable(schema, se, nameTable));
			}
			String nameColumn =  mc.getColTo().getData(); //nameOfColumn where data will be migrated
			String[] keyColumnTo = mc.getColTo().getKey();
			String[] keyColumnFrom = mc.getColFrom().getKey();
			Table from = schema.getTable(nameTable);
			Column c= mu.findColumn (schema,se,from.getName(),nameColumn);
			Select selectInside = script.insertSelect(c, from, keyColumnFrom, schema);
			Column target = mu.findColumn(schema, se, mc.getColTo().getTable(), mc.getColTo().getData());
			//Includes the information required to insert the data queries by 'select' in an iteration into 'target'.
			insert.addColumnValue (c, selectInside, keyColumnTo, target);
		}
		return script;
	}
	/**
	 * Creates the script needed for the migration of a new table.
	 */
	private Script migrationNewTableAll(Schema schema, SchemaEvolution se, MigrationTable mt) {
		log.info("New Table Script from one source table. Target table: %s", mt.getName());
		//Initialization of Script object for the migration of a table
		Script script = new Script ();
		ModelUtilities mu = new ModelUtilities ();
		//Initilization of first For statement, Select looped by the for and the Insert inside the loop  
		For firstFor = new For ();
		Select s = new Select (firstFor);
		firstFor.getSelectsFor().add(s);
		Table to = mu.findTable (schema, se, mt.getName());
		Insert insert = new Insert( to, firstFor);
		insert.setNameNewTable(mt.getNewTableName());
		List <MigrationColumn> migrationColumnsWithoutKey = mt.getMigrationColumnsWithoutKey();
		script.addForSelectInsert (firstFor, s, insert);
		script.getForsHigherLevel().add(firstFor);
		//Association in the Insert statement and SELECT statement of the columns that will get the data from the SELECT statement this SELECT statement
		for (MigrationColumn mc: migrationColumnsWithoutKey) {
			log.info(MIGRATION_FOR_COLUMN, mc.getName());
			String nameColumn = mc.getColFrom().getData(); 
			String nameTable = mc.getColFrom().getTable();
			Column c= mu.findColumn (schema,se,nameTable,nameColumn);
			if (s.getTable() == null) {
				s.setTable(mu.findTable(schema, se, nameTable));
			}
			//Includes the information required to obtain data from column 'c'
			s.addColumnSearch (schema, se, nameTable, c);
			Column target = mu.findColumn(schema, se, mc.getColTo().getTable(), mc.getColTo().getData());

			//Includes the information required to insert the data queries by 's' in an iteration into 'c'
			insert.addColumnValue (c, s, null, target);
		}
		return script;
	}
	/**
	 * Creates the script when there is one or more new columns in a table
	 */
	private Script migrationNewColumn(Schema schema, SchemaEvolution se, MigrationTable mt) {
		log.info("New Column Script");
		//Initialization of Script object for the migration of a table
		Script script = new Script ();
		ModelUtilities mu = new ModelUtilities ();
		//Because the migration consists on adding data to an existing table, there will always be at least one key column for the migration
		List <MigrationColumn> migrationColumns = mt.getMigrationColumnsWithKey();
		Map<String, List<MigrationColumn>> mcsBySourceTable = mt.classifyMCsBySourceTable (migrationColumns);
		Set<String> nameSourceTables = mcsBySourceTable.keySet();
		Iterator<String> iterator = nameSourceTables.iterator();
		//A For-Select-Insert operation will be created to loop over the values to be inserted from each source table into the target table
		while (iterator.hasNext()) {
			String nameTable = iterator.next();
			For forSourceKey;
			Table targetTable = schema.getTable(mt.getName());
			Select selectTargetKey = new Select().getSelectSourceValueWhere(targetTable.getKey(), script.getSelects());
			boolean existed = true;
			//If no For-Select statements had been found for this source table, a new one is created
			if (selectTargetKey == null) {
				log.info("New SELECT to table %s", targetTable);
				existed = false;
				forSourceKey = new For ();
				selectTargetKey = new Select (forSourceKey, targetTable);
				forSourceKey.getSelectsFor().add(selectTargetKey);
				script.getSelects().add(selectTargetKey);
				script.getForsHigherLevel().add(forSourceKey);
				script.getFors().add(forSourceKey);
			}
			else {
				log.info("Using existing SELECT to table %s", targetTable);
				forSourceKey = selectTargetKey.getLoopFor();
			}
			Insert insertTarget = new Insert(targetTable, forSourceKey);
			insertTarget.setNameNewTable(mt.getNewTableName());
			log.info("New INSERT for table %s", mt.getNewTableName());

			//All columns from key of the existing table will be queried
			for (Column keyColumn:targetTable.getKey()) {
				if (!existed) {
					selectTargetKey.getSearch().add(keyColumn);
				}
				insertTarget.addColumnValue(keyColumn, selectTargetKey, null, keyColumn); //same column target and source
			}
			List<MigrationColumn> keysTarget = mcsBySourceTable.get(nameTable);
			Table sourceTable = schema.getTable(nameTable);
			for (MigrationColumn mc : keysTarget) {
				log.info(MIGRATION_FOR_COLUMN, mc.getName());
				String nameColumn =  mc.getColFrom().getData(); //nameOfColumn where data will be migrated
				String[] keyColumnTo = mc.getColTo().getKey();
				String[] keyColumnFrom = mc.getColFrom().getKey();
				Column c= mu.findColumn (schema,se,sourceTable.getName(),nameColumn);
				Select select = script.insertSelect(c, sourceTable, keyColumnFrom, schema);
				if (!forSourceKey.getSelectsInsideFor().contains(select)) {
					forSourceKey.getSelectsInsideFor().add(select);
				}
				Column target = mu.findColumn(schema, se, mc.getColTo().getTable(), mc.getColTo().getData());
				insertTarget.addColumnValue(c, select, keyColumnTo, target);
			}
			script.getInserts().add(insertTarget);
		}		
		return script;
	}
	private void addForSelectInsert(For firstFor, Select s, Insert insert) {
		this.getFors().add(firstFor);
		this.getSelects().add(s);
		this.getInserts().add(insert);	
	}
	/**
	 * Adds a column to be searched in a SELECT statement from a table. If a SELECT does not currently exist, it creates one. Returns the SELECT
	 * that is either created or found
	 */
	private Select insertSelect(Column c, Table from, String[] keyColumnFrom, Schema schema) {
		Select s = new Select().getSelectTable (this.getSelects(), from.getName());
		if (s!=null) {
			log.info("Using existing SELECT to query column :%s",c.getName());
			s.getSearch().add(c);
		}
		else {
			log.info("New SELECT to query column : %s", c.getName());
			s = new Select (from, c);
			s.addWhere (schema, keyColumnFrom);		
			Select sourceValues = s.getSelectSourceValueWhere(s.getWhere(), selects);
			if (sourceValues != null) {
				if (sourceValues.getLoopFor() == null) { 
					For newFor = new For();
					newFor.newForSelect(sourceValues);
					this.getFors().add(newFor);
				}
				else {
					s.setInsideFor(sourceValues.getLoopFor());
					sourceValues.getLoopFor().getSelectsInsideFor().add(s);
				}
			}
			this.getSelects().add(s);
		}
		return s;
	}

	public boolean inList(Column c, List<ColumnValue> columnValues) {
		for (ColumnValue cv : columnValues) {
			if (cv.getColumn().equals(c)) {
				return true;
			}
		}
		return false;
	}
}

package giis.modevo.migration.script;

import java.util.List;

import giis.modevo.migration.script.execution.CassandraConnection;
import giis.modevo.migration.script.execution.ScriptExecution;
import giis.modevo.model.ModelObjects;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * Main Class to call for the creation of scripts. It receives the models as objects and calls first the method to create
 * the script object and then, the method to create the textual script.
 */
@Slf4j
public class MainScript {
	
	public String createScriptAndText (ModelObjects models, CassandraConnection c, String nameKeyspace) {
		List<Script> scripts =  new Script().createScript(models.getSchema(), models.getSchemaEvolution(), models.getDataMigration());
		if (scripts.isEmpty()) {
			log.info("Scenario not implemented yet");
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for (Script script : scripts) {
			log.info("Execution script");
			sb.append(new ScriptText().writeScript(script));
			new ScriptExecution().execute (script, c, nameKeyspace);
		}
		
		return sb.toString();
	}
}

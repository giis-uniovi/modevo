package giis.modevo.model.schemaevolution;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import giis.modevo.model.ModelUtilities;
import giis.modevo.model.schema.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * The SchemaEvolution class represents the structure of the SchemaEvolution
 * data model. Each SchemaEvolution object will contain one or more
 * SchemaChanges which each one details a change applied to a Schema. There are
 * also several classes the inherit from SchemaChange, one for each change that
 * can be applied to the schema.
 */
@Getter @Setter @Slf4j
public class SchemaEvolution {

	private List<SchemaChange> changes;

	public SchemaEvolution() {
		changes = new ArrayList<>();
	}
	
	/**
	 * Searches through all the schema changes to find a table with the name given. 
	 * If the table is not found it returns null.
	 */
	public Table getTable(String name) {
		for (SchemaChange c : this.getChanges()) {
			if (c.getT().getName().equalsIgnoreCase(name)) {
				return c.getT();
			} else if (c instanceof CopyTable copyTable) {
				if (copyTable.getCopiedTable().getName().equalsIgnoreCase(name)) {
					return copyTable.getCopiedTable();
				}
			} else if (c instanceof SplitTable splitTable) {
				List<Table> listSplittedTables = splitTable.getSplittedTables();
				for (Table t : listSplittedTables) {
					if (t.getName().equalsIgnoreCase(name)) {
						return t;
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Creates and returns an object that stores in a Java object the content of the
	 * schema evolution model whose path is passed as an argument
	 */
	public SchemaEvolution readSchemaEvolutionModel(String pathSchemaEvolutionModel) {
		SchemaEvolution se = new SchemaEvolution();
		ModelUtilities mu = new ModelUtilities();
		Document doc = mu.readDocumentGeneric(pathSchemaEvolutionModel);
		NodeList xmi = doc.getElementsByTagName("xmi:XMI");
		Node xmiNode = xmi.item(0); // There is only just one
		NodeList list = xmiNode.getChildNodes();
		for (int temp = 0; temp < list.getLength(); temp++) {
			Node node = list.item(temp);
			if (node.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			Element element = (Element) node;
			String nameElement = element.getNodeName();
			if (nameElement.equalsIgnoreCase("Add")) { // Add column
				log.info ("New Add Columns schema modification");
				new AddColumn().storeInfo(se, list, element);
			} else if (nameElement.equalsIgnoreCase("AddTable")) {
				log.info ("New Add Table schema modification");
				new AddTable().storeInfo(se, list, element);
			} else if (nameElement.equalsIgnoreCase("SplitColumn")) {
				log.info ("New Split Column schema modification");
				new SplitColumn().storeInfo(se, list, node);
			} else if (nameElement.equalsIgnoreCase("JoinTable")) {
				log.info ("New Join Table schema modification");
				new JoinTable().storeInfo(se, node);
			} else if (nameElement.equalsIgnoreCase("CopyTable")) {
				log.info ("New Copy Table schema modification");
				new CopyTable().storeInfo(se, list, node);
			} else if (nameElement.equalsIgnoreCase("SplitTable")) {
				log.info ("New Split Table schema modification");
				new SplitTable().storeInfo(se, list, node);
			} else if (nameElement.equalsIgnoreCase("JoinColumn")) {
				log.info ("New Join Column schema modification");
				new JoinColumn().storeInfo(se, list, node);
			} else if (nameElement.equalsIgnoreCase("RemovePK")) {
				log.info ("New Remove PK schema modification");
				new RemovePK().storeInfo(se, node);
			}
		}
		return se;
	}
}

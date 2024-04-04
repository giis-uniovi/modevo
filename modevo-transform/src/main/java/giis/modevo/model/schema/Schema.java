package giis.modevo.model.schema;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import giis.modevo.model.ModelUtilities;
import lombok.Getter;
import lombok.Setter;

/**
 * The Schema class contains the information of a Schema model. It contains a
 * list of tables that compound the schema.
 */
@Getter @Setter
public class Schema {

	private List<Table> tables;

	public Schema(List<Table> tables) {
		super();
		this.tables = tables;
	}

	public Schema() {
		tables = new ArrayList<>();
	}

	/**
	 * Returns the table object with the same name
	 */
	public Table getTable(String name) {
		for (Table t : tables) {
			if (t.getName().equalsIgnoreCase(name)) {
				return t;
			}
		}
		return null;
	}

	/**
	 * Creates and returns a Schema object that stores the schema model that is stored in the
	 * path given as a parameter.
	 */
	public Schema loadSchemaIntoApp(String fileSchema) {
		Schema schema = new Schema(new ArrayList<>());
		ModelUtilities mu = new ModelUtilities();
		Document doc = mu.readDocumentGeneric(fileSchema);
		NodeList list = doc.getElementsByTagName("Table");
		for (int temp = 0; temp < list.getLength(); temp++) {

			Node node = list.item(temp);

			if (node.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			Element elementTable = (Element) node;

			Table table = new Table(elementTable.getAttribute("name"), new ArrayList<>());

			NodeList lista = node.getChildNodes();
			for (int i = 0; i < lista.getLength(); i++) {
				Node nodeColumn = lista.item(i);
				if (nodeColumn.getNodeType() == Node.ELEMENT_NODE) {
					Column col = readColumn(nodeColumn, table);
					table.getColumns().add(col);
				}
			}
			schema.getTables().add(table);
		}
		return schema;
	}

	/**
	 * Creates a column object from a column element of a schema model.
	 * @param nodeColumn Node that is storing the column information
	 * @param table Table object where the object column will be added
	 */
	private Column readColumn(Node nodeColumn, Table table) {
		Element element = (Element) nodeColumn;
		String column = element.getAttribute("name");
		String attribute = element.getAttribute("nameAttribute");
		String pkString = element.getAttribute("pk");
		String ckString = element.getAttribute("ck");
		String nameEntity = element.getAttribute("nameEntity");
		boolean pk = pkString.equalsIgnoreCase("True");
		boolean ck = ckString.equalsIgnoreCase("True");
		return new Column(column, attribute, pk, ck, nameEntity, table);
	}

	/**
	 * Returns a column object given its name and the name of the table where it is located
	 */
	public Column getColumn(String tableName, String columnName) {
		for (Table t : tables) {
			if (t.getName().equalsIgnoreCase(tableName)) {
				List<Column> cols = t.getColumns();
				for (Column c : cols) {
					if (c.getName().equalsIgnoreCase(columnName)) {
						return c;
					}
				}
			}
		}
		return null;
	}

}

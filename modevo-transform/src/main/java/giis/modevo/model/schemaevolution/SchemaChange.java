package giis.modevo.model.schemaevolution;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import giis.modevo.model.schema.Column;
import giis.modevo.model.schema.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Contains the detail of the change applied to a Table and/or column. Each
 * object must be part of a list from a SchemaEvolution object.
 */
@Getter @Setter
public class SchemaChange {

	private Column c;
	private Table t;
	protected static final String TABLE = "table";
	protected static final String TABLE_SOURCE = "table_source";
	private static final String ERROR_STOREINFO = "This exception was thrown because no schema modification type was specified.";
	
	public SchemaChange () {
	}
	
	public SchemaChange(Column c, Table t) {
		super();
		this.c = c;
		this.t = t;
	}

	public SchemaChange(Table t) {
		this.t = t;
	}
	
	protected String messageIdMissing(String id) {
		return "The id " + id + " in the model is not referencing any element";
	}

	protected void readColumnsTable(Table t, NodeList columnsNode) {
		for (int j = 0; j < columnsNode.getLength(); j++) {
			Node nodeToFrom = columnsNode.item(j);
			if (nodeToFrom.getNodeType() == Node.ELEMENT_NODE) {
				Element column = (Element) nodeToFrom;
				Column columnObject = columnFromModelToObject(column, t);
				t.getColumns().add(columnObject);
			}
		}
	}

	protected Element getElementById(NodeList listNodes, String id) {
		for (int temp = 0; temp < listNodes.getLength(); temp++) {
			Node node = listNodes.item(temp);
			if (node.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			Element element = (Element) node;
			String idElement = element.getAttribute("xmi:id");
			if (id.equalsIgnoreCase(idElement)) {
				return element;
			}
		}
		return null;
	}

	protected Node getNodeById(NodeList listNodes, String id) {
		for (int temp = 0; temp < listNodes.getLength(); temp++) {
			Node node = listNodes.item(temp);
			if (node.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			Element element = (Element) node;
			String idElement = element.getAttribute("xmi:id");
			if (id.equalsIgnoreCase(idElement)) {
				return node;
			}
		}
		return null;
	}
	
	protected Column columnFromModelToObject(Element column, Table t) {
		Column columnObject = new Column(column.getAttribute("name"));
		String key = column.getAttribute("key");
		boolean keyBoolean = isKey(key);
		columnObject.setPk(keyBoolean);
		columnObject.setCk(keyBoolean);
		String nameAttribute = column.getAttribute("nameAttribute");
		String nameEntity = column.getAttribute("nameEntity");
		columnObject.setNameAttribute(nameAttribute);
		columnObject.setNameEntity(nameEntity);
		columnObject.setTable(t);
		return columnObject;
	}
	
	private boolean isKey(String key) {
		return key != null && key.equalsIgnoreCase("true");
	}
	
	protected void storeInfo (SchemaEvolution se, NodeList list, Node node) {
		throw new UnsupportedOperationException (ERROR_STOREINFO);
	}
	protected void storeInfo(SchemaEvolution se, NodeList list, Element element) {
		throw new UnsupportedOperationException (ERROR_STOREINFO);
	}
	protected void storeInfo (SchemaEvolution se, Node node) {
		throw new UnsupportedOperationException (ERROR_STOREINFO);
	}
	
	
}

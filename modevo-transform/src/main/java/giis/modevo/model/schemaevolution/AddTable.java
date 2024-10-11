package giis.modevo.model.schemaevolution;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import giis.modevo.model.DocumentException;
import giis.modevo.model.schema.Table;

/**
 * Subclass of SchemaChange to define a new column that is added to the system.
 * It must always contain the object of the new table with their columns
 */
public class AddTable extends SchemaChange {
	
	public AddTable() {
	}
	
	public AddTable(Table t) {
		super(t);
	}
	
	@Override
	protected List<SchemaChange> storeInfo(NodeList list, Element element) {
		List<SchemaChange> changes = new ArrayList<>();
		String idTable = element.getAttribute("tab");
		Element table = getElementById(list, idTable);
		Node nodeTable = getNodeById(list, idTable);
		if (table == null || nodeTable == null) {
			throw new DocumentException(messageIdMissing(idTable));
		}
		String nameTable = table.getAttribute("name");
		Table t = new Table(nameTable);
		NodeList listColumns = nodeTable.getChildNodes(); // columns of the new table nodes
		readColumnsTable(t, listColumns);
		AddTable at = new AddTable(t);
		changes.add(at);
		return changes;

	}
	
}

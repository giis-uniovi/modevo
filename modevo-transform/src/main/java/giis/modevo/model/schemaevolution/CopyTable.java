package giis.modevo.model.schemaevolution;


import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import giis.modevo.model.DocumentException;
import giis.modevo.model.schema.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Subclass of SchemaChange to define the duplication of an existing table.
 */
@Getter @Setter
public class CopyTable extends SchemaChange {
	private Table copiedTable;
	
	public CopyTable() {
	}
	
	public CopyTable(Table t, Table t2) {
		super(t);
		setCopiedTable(t2);
	}
	
	@Override
	protected List<SchemaChange> changesSchemaModel (NodeList list, Node node) {
		List<SchemaChange> changes = new ArrayList<>();
		Element elementCopy = (Element) node;
		String tableSource = elementCopy.getAttribute(TABLE_SOURCE);
		String idCopiedTable = elementCopy.getAttribute("copiedTable");
		Element table = getElementById(list, idCopiedTable);
		Node nodeTable = getNodeById(list, idCopiedTable);
		if (table == null || nodeTable == null) {
			throw new DocumentException(messageIdMissing(idCopiedTable));
		}
		String nameTable = table.getAttribute("name");
		Table source = new Table(tableSource);
		Table t = new Table(nameTable);
		NodeList listColumns = nodeTable.getChildNodes(); // columns of the new table nodes
		readColumnsTable(t, listColumns);
		CopyTable ct = new CopyTable(source, t);
		changes.add(ct);
		return changes;
	}
	
}

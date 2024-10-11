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
 * Subclass of SchemaChange to define the split of a table into two or more.
 */
@Getter @Setter
public class SplitTable extends SchemaChange {
	private List<Table> splittedTables;
	
	public SplitTable() {
	}
	
	public SplitTable(Table sourceTable) {
		super(sourceTable);
		this.setSplittedTables(new ArrayList<>());
	}
	
	@Override
	protected List<SchemaChange> storeInfo(NodeList list, Node node) {
		List<SchemaChange> changes = new ArrayList<>();
		Element elementCopy = (Element) node;
		String tableSource = elementCopy.getAttribute(TABLE_SOURCE);
		String idsResultingTables = elementCopy.getAttribute("resultingTables");
		String[] tablesIdArray = idsResultingTables.split(" ");
		Table source = new Table(tableSource);
		SplitTable st = new SplitTable(source);
		for (String c : tablesIdArray) {
			Element table = getElementById(list, c);
			Node nodeTable = getNodeById(list, c);
			if (table == null || nodeTable == null) {
				throw new DocumentException(messageIdMissing(c));
			}
			String tableName = table.getAttribute("name");
			NodeList listColumns = nodeTable.getChildNodes(); // columns of the new table nodes

			Table resulting = new Table(tableName);
			readColumnsTable(resulting, listColumns);
			st.getSplittedTables().add(resulting);
		}
		changes.add(st);
		return changes;
	}
	
}

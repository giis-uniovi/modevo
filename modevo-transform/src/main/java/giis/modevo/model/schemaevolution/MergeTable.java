package giis.modevo.model.schemaevolution;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import giis.modevo.model.schema.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Subclass of SchemaChange to define two tables to be joined.
 */
@Getter @Setter
public class MergeTable extends SchemaChange {
	
	private Table originalTable;
	private Table newTable;
	
	public MergeTable() {
	}
	
	public MergeTable(Table originalTable, Table newTable) {
		super(originalTable);
		this.setOriginalTable(originalTable);
		this.setNewTable(newTable);
	}
	
	@Override
	protected List<SchemaChange> changesSchemaModel (Node node) {
		List<SchemaChange> changes = new ArrayList<>();
		Element elementSplit = (Element) node;
		String source = elementSplit.getAttribute(TABLE_SOURCE);
		String target = elementSplit.getAttribute("tableTarget");
		Table tableSource = new Table(source);
		Table tableTarget = new Table(target);
		MergeTable jt = new MergeTable(tableSource, tableTarget);
		changes.add(jt);
		return changes;
	}
	
}

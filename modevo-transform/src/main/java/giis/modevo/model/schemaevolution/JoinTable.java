package giis.modevo.model.schemaevolution;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import giis.modevo.model.schema.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Subclass of SchemaChange to define two tables to be joined.
 */
@Getter @Setter
public class JoinTable extends SchemaChange {
	
	private Table originalTable;
	private Table newTable;
	
	public JoinTable() {
	}
	
	public JoinTable(Table originalTable, Table newTable) {
		super(originalTable);
		this.setOriginalTable(originalTable);
		this.setNewTable(newTable);
	}
	
	@Override
	protected void storeInfo(SchemaEvolution se, Node node) {
		Element elementSplit = (Element) node;
		String source = elementSplit.getAttribute(TABLE_SOURCE);
		String target = elementSplit.getAttribute("tableTarget");
		Table tableSource = new Table(source);
		Table tableTarget = new Table(target);
		JoinTable jt = new JoinTable(tableSource, tableTarget);
		se.getChanges().add(jt);
	}
	
}

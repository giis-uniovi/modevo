package giis.modevo.model.schemaevolution;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import giis.modevo.model.schema.Column;
import giis.modevo.model.schema.Schema;
import giis.modevo.model.schema.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Subclass of SchemaChange to define a column to be removed from the primary
 * key. It must always contain the object of the column to be removed from the
 * PK and the table where it belongs.
 */
@Getter @Setter
public class RemovePK extends SchemaChange {
	
	private String namePreviousTable;
	
	public RemovePK() {
		
	}
	
	public RemovePK(Column c, Table t, String namePreviousTable) {
		super(c, t);
		this.setNamePreviousTable(namePreviousTable);
	}
	
	@Override
	protected List<SchemaChange> changesSchemaModel (Node node, Schema sc) {
		List<SchemaChange> changes = new ArrayList<>();
		Element elementCopy = (Element) node;
		String table = elementCopy.getAttribute(TABLE);
		String columnRemoved = elementCopy.getAttribute("columnRemoved");
		String previousTable = elementCopy.getAttribute("previous");
		Column c = new Column(columnRemoved);
		Table t = new Table(table);
		Table previousTableObject = sc.getTable(previousTable);
		for (Column preColumn : previousTableObject.getColumns()) {
			if (!preColumn.getName().equalsIgnoreCase(columnRemoved)) {
				Column newColumn = new Column (preColumn);
				newColumn.setNameTable(table);
				t.getColumns().add(newColumn);
			}
		}
		RemovePK rp = new RemovePK(c, t, previousTable);
		changes.add(rp);
		return changes;
	}
	
}

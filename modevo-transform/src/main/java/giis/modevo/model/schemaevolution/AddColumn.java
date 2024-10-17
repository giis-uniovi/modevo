package giis.modevo.model.schemaevolution;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import giis.modevo.model.DocumentException;
import giis.modevo.model.schema.Column;
import giis.modevo.model.schema.Table;

/**
 * Subclass of SchemaChange to define a new column that is added to the system.
 * It must always contain the column added and the table where it is going to be
 * added.
 */
public class AddColumn extends SchemaChange {
	
	public AddColumn() {
	}
	
	public AddColumn(Column c, Table t) {
		super(c, t);
	}
	
	@Override
	protected List<SchemaChange> changesSchemaModel (NodeList list, Element element) {
		List<SchemaChange> changes = new ArrayList<>();
		String idTable = element.getAttribute("tab");
		Element table = getElementById(list, idTable);
		if (table == null) {
			throw new DocumentException(messageIdMissing(idTable));
		}
		Table t = new Table(table.getAttribute("name"));
		String idColumns = table.getAttribute("cols");
		String[] columnsArray = idColumns.split(" ");
		for (String c : columnsArray) {
			Element column = getElementById(list, c);
			if (column == null) {
				throw new DocumentException(messageIdMissing(c));
			}
			Column columnObject = super.columnFromModelToObject(column, t);
			t.getColumns().add(columnObject);
			AddColumn ac = new AddColumn(columnObject, t);
			changes.add(ac);
		}
		return changes;
	}
	
}
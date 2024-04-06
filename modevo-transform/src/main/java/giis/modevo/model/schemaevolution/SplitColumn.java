package giis.modevo.model.schemaevolution;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import giis.modevo.model.DocumentException;
import giis.modevo.model.schema.Column;
import giis.modevo.model.schema.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Subclass of SchemaChange to define the split of a column into two or more.
 */
@Getter @Setter
public class SplitColumn extends SchemaChange {
	private List<Column> resultColumns;
	private String oldColumn;

	public SplitColumn() {
	}

	public SplitColumn(Table t, String oldColumn) {
		super(t);
		resultColumns = new ArrayList<>();
		this.setOldColumn(oldColumn);
	}

	public SplitColumn(Column c, Table t, List<Column> rs) {
		super(c, t);
		resultColumns = rs;
	}
	
	@Override
	protected void storeInfo(SchemaEvolution se, NodeList list, Node node) {
		Element elementSplit = (Element) node;
		String nameTable = elementSplit.getAttribute(TABLE);
		String oldColumnModel = elementSplit.getAttribute("oldColumn");
		Table t = new Table(nameTable);
		SplitColumn splitChange = new SplitColumn(t, oldColumnModel);
		String idColumns = elementSplit.getAttribute("newColumns");
		String[] columnsArray = idColumns.split(" ");
		for (String c : columnsArray) {
			Element column = getElementById(list, c);
			if (column == null) {
				throw new DocumentException(messageIdMissing(c));
			}
			Column columnObject = columnFromModelToObject(column);
			splitChange.getResultColumns().add(columnObject);
		}
		se.getChanges().add(splitChange);
	}
	
}

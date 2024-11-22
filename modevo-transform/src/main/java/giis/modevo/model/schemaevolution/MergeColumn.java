package giis.modevo.model.schemaevolution;

import java.util.ArrayList;
import java.util.List;

import giis.modevo.model.schema.Column;
import giis.modevo.model.schema.Table;
import lombok.Getter;
import lombok.Setter;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import giis.modevo.model.DocumentException;

/**
 * Subclass of SchemaChange to define the join of two or more columns into one.
 */
@Getter @Setter
public class MergeColumn extends SchemaChange {
	private List<Column> sourceColumns;
	private String criteria; //criteria to join the column
	
	public MergeColumn() {
	}
	
	public MergeColumn(Column c, Table t, String criteria) {
		super(c, t);
		setSourceColumns(new ArrayList<>());
		this.setCriteria(criteria);
	}
	
	@Override
	protected List<SchemaChange> changesSchemaModel (NodeList list, Node node) {
		List<SchemaChange> changes = new ArrayList<>();
		Element elementCopy = (Element) node;
		String table = elementCopy.getAttribute(TABLE);
		String idTargetColumn = elementCopy.getAttribute("targetColumn");
		String idsSourceColumns = elementCopy.getAttribute("sourceColumns");
		Table source = new Table(table);
		Element elementColumnSource = getElementById(list, idTargetColumn);
		if (elementColumnSource == null) {
			throw new DocumentException(messageIdMissing(idTargetColumn));
		}
		Column c = columnFromModelToObject(elementColumnSource, source);
		String criteriaString = elementCopy.getAttribute("criteria");
		MergeColumn jt = new MergeColumn(c, source, criteriaString);
		String[] columnsIdArray = idsSourceColumns.split(" ");
		for (String id : columnsIdArray) {
			Element column = getElementById(list, id);
			if (column == null) {
				throw new DocumentException(messageIdMissing(id));
			}
			Column columnSource = columnFromModelToObject(column, source);
			jt.getSourceColumns().add(columnSource);
		}
		changes.add(jt);
		return changes;
	}
	
}
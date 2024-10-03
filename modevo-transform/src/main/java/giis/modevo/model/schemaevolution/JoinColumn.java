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
public class JoinColumn extends SchemaChange {
	private List<Column> sourceColumns;
	private String criteria; //criteria to join the column
	
	public JoinColumn() {
	}
	
	public JoinColumn(Column c, Table t, String criteria) {
		super(c, t);
		setSourceColumns(new ArrayList<>());
		this.setCriteria(criteria);
	}
	
	@Override
	protected void storeInfo(SchemaEvolution se, NodeList list, Node node) {
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
		JoinColumn jt = new JoinColumn(c, source, criteriaString);
		String[] columnsIdArray = idsSourceColumns.split(" ");
		for (String id : columnsIdArray) {
			Element column = getElementById(list, id);
			if (column == null) {
				throw new DocumentException(messageIdMissing(id));
			}
			Column columnSource = columnFromModelToObject(column, source);
			jt.getSourceColumns().add(columnSource);
		}
		se.getChanges().add(jt);
	}
	
}
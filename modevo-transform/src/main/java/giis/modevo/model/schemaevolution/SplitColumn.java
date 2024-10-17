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
	private List<CriteriaSplit> cs;
	
	public SplitColumn() {
	}

	public SplitColumn(Table t, String oldColumn) {
		super(t);
		resultColumns = new ArrayList<>();
		cs = new ArrayList<>();
		this.setOldColumn(oldColumn);
	}

	public SplitColumn(Column c, Table t, List<Column> rs) {
		super(c, t);
		resultColumns = rs;
	}
	
	public SplitColumn(Column c, Table t, List<Column> rs, List<CriteriaSplit> cs) {
		this(c, t, rs);
		this.cs = cs;
	}
	
	@Override
	protected List<SchemaChange> changesSchemaModel (NodeList list, Node node) {
		List<SchemaChange> changes = new ArrayList<>();
		Element elementSplit = (Element) node;
		String nameTable = elementSplit.getAttribute(TABLE);
		String oldColumnModel = elementSplit.getAttribute("oldColumn");
		Table t = new Table(nameTable);
		SplitColumn splitChange = new SplitColumn(t, oldColumnModel);
		String idColumns = elementSplit.getAttribute("newColumns");
		String criteria = elementSplit.getAttribute ("criteria");
		String[] columnsArray = idColumns.split(" ");
		String[] criteriaArray = criteria.split(" ");
		for (String c : columnsArray) {
			Element column = getElementById(list, c);
			if (column == null) {
				throw new DocumentException(messageIdMissing(c));
			}
			Column columnObject = columnFromModelToObject(column, t);
			splitChange.getResultColumns().add(columnObject);
		}
		for (String c : criteriaArray) {
			Element criteriaElement = getElementById(list, c);
			if (criteriaElement == null) {
				throw new DocumentException(messageIdMissing(c));
			}
			Element column = getElementById(list, criteriaElement.getAttribute("column"));
			if (column == null) {
				throw new DocumentException(messageIdMissing(c));
			}
			Column columnObject = columnFromModelToObject(column, t);
			CriteriaSplit criteriaObject = criteriaFromModelToObject(criteriaElement, columnObject);
			splitChange.getCs().add(criteriaObject);
		}
		changes.add(splitChange);
		return changes;
	}

	private CriteriaSplit criteriaFromModelToObject(Element criteria, Column column) {
		String value = criteria.getAttribute("value");
		String operator = criteria.getAttribute("operator");
		return new CriteriaSplit (column, value, operator);
	}
	
}

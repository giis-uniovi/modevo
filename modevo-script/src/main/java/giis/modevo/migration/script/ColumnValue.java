package giis.modevo.migration.script;

import java.util.List;

import giis.modevo.model.schema.Column;
import lombok.Getter;
import lombok.Setter;
@Getter @Setter
public class ColumnValue {
	private Column column;
	private String[] key;
	private String value; //String generic type, actual type in DB could be different
	private String variableName;
	private Column columnSelectOrigin;
	private List<Column> sourceJoin;
}

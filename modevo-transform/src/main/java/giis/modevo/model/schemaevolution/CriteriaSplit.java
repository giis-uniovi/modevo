package giis.modevo.model.schemaevolution;

import giis.modevo.model.schema.Column;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CriteriaSplit {
	Column column;
	String value;
	String operator;
	
	public CriteriaSplit(Column column, String value, String operator) {
		super();
		this.column = column;
		this.value = value;
		this.operator = operator;
	}
	
}

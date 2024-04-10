package giis.modevo.migration.script;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class For {
	private List<Select> selectsFor;
	private List<Select> selectsInsideFor;
	
 	private For nestedFor;
	public For () {
		selectsFor = new ArrayList<>();
		selectsInsideFor = new ArrayList<>();
	}
	/**
	 * Class that creates a For object to loop a SELECT statement.
	 */
	public void newForSelect (Select s) {
		this.getSelectsFor().add(s);
		For oldFor = s.getInsideFor();
		oldFor.setNestedFor(this); //might change to a list
		s.setLoopFor(this);
		s.setInsideFor(null);
	}
}

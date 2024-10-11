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
	}
	public boolean getSelect(Select select) {
		for (Select s: selectsFor) {
			if (select.equals(s)) {
				return true;
			}
		}
		for (Select s: selectsInsideFor) {
			if (select.equals(s)) {
				return true;
			}
		}
		return false;
	}
}

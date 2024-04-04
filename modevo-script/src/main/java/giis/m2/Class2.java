package giis.m2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Class2 {
	private static final Logger log=LoggerFactory.getLogger(Class2.class);
	public String function21() {
		log.info("Run function21()");
		Class2 class2 = new Class2();
		return class2.function21() + "-" + class2.function21();
	}
}

package edu.washington.cse.longan.trait;

import org.jdom.Element;

public class IsNullTrait extends AbstractTrait {

	public static String ID = "IsNullTrait";

	public String getDescription() {
		return "Is the object ever null?";
	}

	public String getName() {
		return ID;
	}

	public void track(Object obj) {
		if (obj == null)
			getData().add(DATA_KINDS.IS_NULL);
		else
			getData().add(DATA_KINDS.NOT_NULL);
	}

	@Override
	public String toString() {
		int isNull = getData().count(DATA_KINDS.IS_NULL);
		int notNull = getData().count(DATA_KINDS.NOT_NULL);

		String ret = "IsNull - Total: " + getData().size() + " IsNull: " + isNull + " NotNull: " + notNull + ". ";

		return ret;
	}

	public static ITrait parseXML(Element element) {
		throw new AssertionError("Subtypes should implement this method.");
	}
}

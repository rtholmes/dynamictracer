package edu.washington.cse.longan.trait;

import org.jdom.Element;

public class BooleanTrait extends AbstractTrait {

	public static String ID = "BooleanTrait";

	public String getDescription() {
		return "Is the object true or false?";
	}

	public String getName() {
		return ID;
	}

	public void track(Object obj) {
		if (obj != null) {
			if (obj.equals(Boolean.TRUE))
				getData().add(DATA_KINDS.IS_TRUE);
			else
				getData().add(DATA_KINDS.IS_FALSE);
		}
	}

	@Override
	public String toString() {
		int isTrue = getData().count(DATA_KINDS.IS_TRUE);
		int isFalse = getData().count(DATA_KINDS.IS_FALSE);

		String ret = "Boolean - Total: " + getData().size() + " True: " + isTrue + " False: " + isFalse + ". ";

		return ret;
	}

	public static ITrait parseXML(Element element) {
		throw new AssertionError("Subtypes should implement this method.");
	}
}

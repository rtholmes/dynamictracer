package edu.washington.cse.longan.trait;

import org.jdom.Element;

public class StringEmptyTrait extends AbstractTrait {
	public static String ID = "StringEmptyTrait";

	@Override
	public String getDescription() {
		return getName();
	}

	@Override
	public String getName() {
		return ID;
	}

	@Override
	public String toString() {

		int empty = getData().count(DATA_KINDS.EMPTY);
		int notEmpty = getData().count(DATA_KINDS.NOT_EMPTY);

		String ret = "StringEmpty - Total: " + getData().size() + " Empty" + ": " + empty + " not empty: " + notEmpty
				+ ". ";

		return ret;
	}

	@Override
	public void track(Object obj) {
		if (obj != null) {
			int i = ((String) obj).length();
			if (i == 0)
				getData().add(DATA_KINDS.EMPTY);
			else
				getData().add(DATA_KINDS.NOT_EMPTY);
		}

	}

	public static ITrait parseXML(Element element) {
		throw new AssertionError("Subtypes should implement this method.");
	}
}

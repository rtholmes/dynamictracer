package edu.washington.cse.longan.trait;

import org.jdom.Element;

import edu.washington.cse.longan.io.ILonganIO;
import edu.washington.cse.longan.trait.ITrait.DATA_KINDS;

public class BooleanTrait extends AbstractTrait {

	public String getDescription() {
		return "Is the object true or false?";
	}

	public String getName() {
		return "BooleanTrait";
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

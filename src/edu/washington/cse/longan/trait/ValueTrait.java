package edu.washington.cse.longan.trait;

import org.jdom.Element;

public class ValueTrait extends AbstractTrait {

	@Override
	public String getDescription() {
		return "What values were this object?";
	}

	@Override
	public String getName() {
		return "ValueTrait";
	}

	@Override
	public void track(Object obj) {
		if (obj != null)
			getSupplementalData().add(obj.toString());
	}

	@Override
	public String toString() {
		String ret = "";

		for (String value : getSupplementalData().elementSet()) {
			int num = getSupplementalData().count(value);
			ret += value + ": " + num + "; ";
		}

		ret = "Value(s): " + getSupplementalData().elementSet().size() + ", total values: " + getSupplementalData().size() + " - " + ret+". ";

		return ret;
	}

	@Override
	public Element toXML() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static ITrait parseXML(Element element){
		throw new AssertionError("Subtypes should implement this method.");
	}
}

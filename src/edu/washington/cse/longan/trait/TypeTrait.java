package edu.washington.cse.longan.trait;

import org.jdom.Element;

import edu.washington.cse.longan.io.ILonganIO;

public class TypeTrait extends AbstractTrait {
	public static String ID = "TypeTrait";

	@Override
	public String getDescription() {
		return "What types were this object?";
	}

	@Override
	public String getName() {
		return ID;
	}

	@Override
	public void track(Object obj) {
		if (obj != null)
			getSupplementalData().add(obj.getClass().getName());
	}

	@Override
	public String toString() {
		String ret = "";

		for (String type : getSupplementalData().elementSet()) {
			int num = getSupplementalData().count(type);
			ret += type + ": " + num + "; ";
		}

		ret = "Type(s): " + getSupplementalData().elementSet().size() + ", total objects: "
				+ getSupplementalData().size() + " - " + ret + ". ";

		return ret;
	}

	public static ITrait parseXML(Element element) {
		throw new AssertionError("Subtypes should implement this method.");
	}

	@Override
	public Element toXML() {
		Element element = super.toXML();

		for (String kind : getSupplementalData().elementSet()) {
			Element valueElement = new Element(ILonganIO.SUPPLEMENTAL_DATA);
			valueElement.setAttribute(ILonganIO.KEY, kind + "");
			valueElement.setAttribute(ILonganIO.VALUE, getSupplementalData().count(kind) + "");
			element.addContent(valueElement);
		}

		return element;
	}
}

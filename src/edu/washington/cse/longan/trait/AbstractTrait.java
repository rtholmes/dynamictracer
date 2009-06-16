package edu.washington.cse.longan.trait;

import org.jdom.Element;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import edu.washington.cse.longan.io.ILonganIO;

public abstract class AbstractTrait implements ITrait {

	Multiset<DATA_KINDS> _data = HashMultiset.create();

	Multiset<String> _supplementalData = HashMultiset.create();

	public Multiset<DATA_KINDS> getData() {
		return _data;
	}

	public Multiset<String> getSupplementalData() {
		return _supplementalData;
	}

	public abstract String getDescription();

	public abstract String getName();

	public abstract void track(Object obj);

	@Override
	public abstract String toString();

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ITrait) {
			return ((ITrait) obj).getName().equals(getName());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getName().hashCode();
	}

	public static ITrait parseXML(Element element) {
		throw new AssertionError("Subtypes should implement this method.");
	}

	public Element toXML() {
		
		Element element = new Element(ILonganIO.TRAIT);
		element.setAttribute(ILonganIO.KEY, getName());

		for (DATA_KINDS kind : getData().elementSet()) {
			Element valueElement = new Element(ILonganIO.DATA);
			valueElement.setAttribute(ILonganIO.KEY, kind + "");
			valueElement.setAttribute(ILonganIO.VALUE, getData().count(kind) + "");
			element.addContent(valueElement);
		}
		
//		for (String kind : getSupplementalData().elementSet()) {
//			Element valueElement = new Element(ILonganIO.SUPPLEMENTAL_DATA);
//			valueElement.setAttribute(ILonganIO.KEY, kind + "");
//			valueElement.setAttribute(ILonganIO.VALUE, getSupplementalData().count(kind) + "");
//			element.addContent(valueElement);
//		}
		

		return element;
	}
}

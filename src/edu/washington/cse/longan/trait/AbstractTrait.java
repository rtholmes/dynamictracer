package edu.washington.cse.longan.trait;

import org.jdom.Element;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

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
	
	public static ITrait parseXML(Element element){
		throw new AssertionError("Subtypes should implement this method.");
	}
	
	public abstract Element toXML();
}

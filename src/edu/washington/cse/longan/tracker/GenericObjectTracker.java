package edu.washington.cse.longan.tracker;

import edu.washington.cse.longan.trait.ITrait;
import edu.washington.cse.longan.trait.IsNullTrait;
import edu.washington.cse.longan.trait.TypeTrait;

public class GenericObjectTracker extends AbstractObjectTracker {

	@SuppressWarnings("unchecked")
	public GenericObjectTracker(Class clazz) {
		super(clazz);
	
		createTraits();
		
	}

	@Override
	public IObjectTracker clone() throws CloneNotSupportedException {
		
		return new GenericObjectTracker(null);
		
	}

	@Override
	public void track(Object obj) {
		for (ITrait trait : getTraits()){
			trait.track(obj);
		}
		
	}

	@Override
	public void createTraits() {
		addTrait(new IsNullTrait());
		addTrait(new TypeTrait());
	}

	@Override
	public String toString() {
		String ret = "";
		for (ITrait trait : getTraits())
			ret += trait.toString();

		return ret;
	}
}

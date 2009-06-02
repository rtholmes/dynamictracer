package edu.washington.cse.longan.tracker;

import java.util.ArrayList;
import java.util.Collection;

import edu.washington.cse.longan.trait.ITrait;

public abstract class AbstractObjectTracker implements IObjectTracker {

	private ArrayList<ITrait> _traits = new ArrayList<ITrait>();
	
	@SuppressWarnings("unchecked")
	public AbstractObjectTracker(Class clazz) {
		
	}
	
	public abstract void createTraits();
	
	public abstract void track(Object obj);
	
	public abstract IObjectTracker clone() throws CloneNotSupportedException;

	public void addTrait(ITrait trait){
		_traits.add(trait);
	}
	
	public Collection<ITrait> getTraits() {
		return _traits;
	}
}

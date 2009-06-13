package edu.washington.cse.longan.tracker;

import edu.washington.cse.longan.trait.IsNullTrait;
import edu.washington.cse.longan.trait.NumberTrait;

public class NumberTracker extends AbstractObjectTracker {

	@SuppressWarnings("unchecked")
	public NumberTracker(Class clazz, String name) {
		super(clazz, name);
	}

	@SuppressWarnings("unchecked")
	public NumberTracker(Class clazz) {
		super(clazz);
	}

	@SuppressWarnings("unchecked")
	public NumberTracker(Class clazz, int position, String name) {
		super(clazz, position, name);
	}

	
	@Override
	public IObjectTracker clone() throws CloneNotSupportedException {
		if (isField())
			return new NumberTracker(getClazz(), getName());
		if (isReturn())
			return new NumberTracker(getClazz());
		if (isParameter())
			return new NumberTracker(getClazz(), getPosition(), getName());

		_log.error("This should never happen.");
		return null;
	}

	@Override
	public void createTraits() {
		addTrait(new IsNullTrait());
		addTrait(new NumberTrait());

	}

	public String getTrackerName() {
		return "NumberTracker";
	}

}

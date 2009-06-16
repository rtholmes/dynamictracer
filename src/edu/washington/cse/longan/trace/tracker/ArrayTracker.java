package edu.washington.cse.longan.trace.tracker;

import edu.washington.cse.longan.trait.ArrayEmptyTrait;
import edu.washington.cse.longan.trait.IsNullTrait;

public class ArrayTracker extends AbstractObjectTracker {


	@SuppressWarnings("unchecked")
	public ArrayTracker(Class clazz) {
		super(clazz);
	}

	@SuppressWarnings("unchecked")
	public ArrayTracker(Class clazz, String name) {
		super(clazz, name);
	}

	@SuppressWarnings("unchecked")
	public ArrayTracker(Class clazz, int index, String name) {
		super(clazz, index, name);
	}


	@Override
	public IObjectTracker clone() throws CloneNotSupportedException {
		if (isField())
			return new ArrayTracker(getClazz(), getName());
		if (isReturn())
			return new ArrayTracker(getClazz());
		if (isParameter())
			return new ArrayTracker(getClazz(), getPosition(), getName());

		_log.error("This should never happen.");
		return null;
	}

	@Override
	public void createTraits() {
		addTrait(new IsNullTrait());
		addTrait(new ArrayEmptyTrait());
	}

	public String getTrackerName() {
		return "ArrayTracker";
	}
}

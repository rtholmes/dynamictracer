package edu.washington.cse.longan.trace.tracker;

import edu.washington.cse.longan.trait.NumberTrait;

public class PrimitiveNumberTracker extends AbstractObjectTracker {

	@SuppressWarnings("unchecked")
	public PrimitiveNumberTracker(Class clazz, String name) {
		super(clazz, name);
	}

	@SuppressWarnings("unchecked")
	public PrimitiveNumberTracker(Class clazz) {
		super(clazz);
	}

	@SuppressWarnings("unchecked")
	public PrimitiveNumberTracker(Class clazz, int position, String name) {
		super(clazz, position, name);
	}

	@Override
	public IObjectTracker clone() throws CloneNotSupportedException {
		if (isField())
			return new PrimitiveNumberTracker(getClazz(), getName());
		if (isReturn())
			return new PrimitiveNumberTracker(getClazz());
		if (isParameter())
			return new PrimitiveNumberTracker(getClazz(), getPosition(), getName());

		_log.error("This should never happen.");
		return null;
	}

	@Override
	public void createTraits() {
		addTrait(new NumberTrait());
	}

	public String getTrackerName() {
		return "PrimitiveNumberTracker";
	}

}

package edu.washington.cse.longan.trace.tracker;

import edu.washington.cse.longan.trait.BooleanTrait;

public class BooleanTracker extends AbstractObjectTracker {

	@SuppressWarnings("unchecked")
	public BooleanTracker(Class clazz, String name) {
		super(clazz, name);
	}

	@SuppressWarnings("unchecked")
	public BooleanTracker(Class clazz) {
		super(clazz);
	}

	@SuppressWarnings("unchecked")
	public BooleanTracker(Class clazz, int position, String name) {
		super(clazz, position, name);
	}

	@Override
	public IObjectTracker clone() throws CloneNotSupportedException {
		if (isField())
			return new BooleanTracker(getClazz(), getName());
		if (isReturn())
			return new BooleanTracker(getClazz());
		if (isParameter())
			return new BooleanTracker(getClazz(), getPosition(), getName());

		_log.error("This should never happen.");
		return null;
	}

	@Override
	public void createTraits() {
		addTrait(new BooleanTrait());

	}

	public String getTrackerName() {
		return "BooleanTracker";
	}

}

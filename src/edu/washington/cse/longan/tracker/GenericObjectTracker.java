package edu.washington.cse.longan.tracker;

import org.apache.log4j.Logger;

import edu.washington.cse.longan.trait.IsNullTrait;
import edu.washington.cse.longan.trait.TypeTrait;

public class GenericObjectTracker extends AbstractObjectTracker {

	@SuppressWarnings("unchecked")
	public GenericObjectTracker(Class clazz) {
		super(clazz);
	}

	@SuppressWarnings("unchecked")
	public GenericObjectTracker(Class clazz, String name) {
		super(clazz, name);
	}

	@SuppressWarnings("unchecked")
	public GenericObjectTracker(Class clazz, int index, String name) {
		super(clazz, index, name);
	}

	Logger _log = Logger.getLogger(this.getClass());

	@Override
	public IObjectTracker clone() throws CloneNotSupportedException {

		if (isField())
			return new GenericObjectTracker(getClazz(), getName());
		if (isReturn())
			return new GenericObjectTracker(getClazz());
		if (isParameter())
			return new GenericObjectTracker(getClazz(), getPosition(), getName());

		_log.error("This should never happen.");
		return null;
	}

	@Override
	public void createTraits() {
		addTrait(new IsNullTrait());
		addTrait(new TypeTrait());
	}



	public String getTrackerName() {
		return "GenericObjectTracker";
	}
}

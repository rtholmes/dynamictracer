package edu.washington.cse.longan.tracker;

import org.apache.log4j.Logger;

import edu.washington.cse.longan.trait.CollectionEmptyTrait;
import edu.washington.cse.longan.trait.IsNullTrait;
import edu.washington.cse.longan.trait.TypeTrait;

public class CollectionTracker extends AbstractObjectTracker {

	@SuppressWarnings("unchecked")
	public CollectionTracker(Class clazz) {
		super(clazz);
	}

	@SuppressWarnings("unchecked")
	public CollectionTracker(Class clazz, String name) {
		super(clazz, name);
	}

	@SuppressWarnings("unchecked")
	public CollectionTracker(Class clazz, int index, String name) {
		super(clazz, index, name);
	}

	Logger _log = Logger.getLogger(this.getClass());

	@Override
	public IObjectTracker clone() throws CloneNotSupportedException {

		if (isField())
			return new CollectionTracker(getClazz(), getName());
		if (isReturn())
			return new CollectionTracker(getClazz());
		if (isParameter())
			return new CollectionTracker(getClazz(), getPosition(), getName());

		_log.error("This should never happen.");
		return null;
	}

	@Override
	public void createTraits() {
		addTrait(new CollectionEmptyTrait());
		addTrait(new IsNullTrait());
		addTrait(new TypeTrait());
	}



	public String getTrackerName() {
		return "CollectionTracker";
	}
}

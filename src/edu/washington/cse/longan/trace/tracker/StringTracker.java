package edu.washington.cse.longan.trace.tracker;



import edu.washington.cse.longan.Logger;
import edu.washington.cse.longan.trait.IsNullTrait;
import edu.washington.cse.longan.trait.StringEmptyTrait;

public class StringTracker extends AbstractObjectTracker {

	@SuppressWarnings("unchecked")
	public StringTracker(Class clazz) {
		super(clazz);
	}

	@SuppressWarnings("unchecked")
	public StringTracker(Class clazz, String name) {
		super(clazz, name);
	}

	@SuppressWarnings("unchecked")
	public StringTracker(Class clazz, int index, String name) {
		super(clazz, index, name);
	}

	Logger _log = Logger.getLogger(this.getClass());

	@Override
	public IObjectTracker clone() throws CloneNotSupportedException {

		if (isField())
			return new StringTracker(getClazz(), getName());
		if (isReturn())
			return new StringTracker(getClazz());
		if (isParameter())
			return new StringTracker(getClazz(), getPosition(), getName());

		_log.error("This should never happen.");
		return null;
	}

	@Override
	public void createTraits() {
		addTrait(new IsNullTrait());
		addTrait(new StringEmptyTrait());
	}



	public String getTrackerName() {
		return "StringTracker";
	}
}

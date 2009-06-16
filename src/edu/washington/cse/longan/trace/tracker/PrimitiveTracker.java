package edu.washington.cse.longan.trace.tracker;

public class PrimitiveTracker extends AbstractObjectTracker {

	@SuppressWarnings("unchecked")
	public PrimitiveTracker(Class clazz) {
		super(clazz);
	}

	@SuppressWarnings("unchecked")
	public PrimitiveTracker(Class clazz, String name) {
		super(clazz, name);
	}

	@SuppressWarnings("unchecked")
	public PrimitiveTracker(Class clazz, int index, String name) {
		super(clazz, index, name);
	}

	public void track(Object obj) {
		// XXX Auto-generated method stub

	}

	@Override
	public IObjectTracker clone() throws CloneNotSupportedException {
		if (isField())
			return new PrimitiveTracker(getClazz(), getName());
		if (isReturn())
			return new PrimitiveTracker(getClazz());
		if (isParameter())
			return new PrimitiveTracker(getClazz(), getPosition(), getName());

		_log.error("This should never happen.");
		return null;
	}

	@Override
	public void createTraits() {
		// TODO Auto-generated method stub

	}

	public String getTrackerName() {
		return "PrimitiveTracker";
	}

}

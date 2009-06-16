package edu.washington.cse.longan.trace.tracker;

import edu.washington.cse.longan.trait.ValueTrait;

public class ByteTracker extends AbstractObjectTracker {

	@SuppressWarnings("unchecked")
	public ByteTracker(Class clazz, String name) {
		super(clazz, name);
	}

	@SuppressWarnings("unchecked")
	public ByteTracker(Class clazz) {
		super(clazz);
	}

	@SuppressWarnings("unchecked")
	public ByteTracker(Class clazz, int position, String name) {
		super(clazz, position, name);
	}

	@Override
	public IObjectTracker clone() throws CloneNotSupportedException {
		if (isField())
			return new ByteTracker(getClazz(), getName());
		if (isReturn())
			return new ByteTracker(getClazz());
		if (isParameter())
			return new ByteTracker(getClazz(), getPosition(), getName());

		_log.error("This should never happen.");
		return null;
	}

	@Override
	public void createTraits() {
		// TODO: select traits for bytes
//		addTrait(new LogPrinterTrait());
		addTrait(new ValueTrait());
	}

	public String getTrackerName() {
		return "ByteTracker";
	}
}
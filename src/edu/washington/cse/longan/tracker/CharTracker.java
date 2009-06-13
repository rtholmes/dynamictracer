package edu.washington.cse.longan.tracker;

import edu.washington.cse.longan.trait.ValueTrait;

public class CharTracker extends AbstractObjectTracker {

	@SuppressWarnings("unchecked")
	public CharTracker(Class clazz, String name) {
		super(clazz, name);
	}

	@SuppressWarnings("unchecked")
	public CharTracker(Class clazz) {
		super(clazz);
	}

	@SuppressWarnings("unchecked")
	public CharTracker(Class clazz, int position, String name) {
		super(clazz, position, name);
	}

	@Override
	public IObjectTracker clone() throws CloneNotSupportedException {
		if (isField())
			return new CharTracker(getClazz(), getName());
		if (isReturn())
			return new CharTracker(getClazz());
		if (isParameter())
			return new CharTracker(getClazz(), getPosition(), getName());

		_log.error("This should never happen.");
		return null;
	}

	@Override
	public void createTraits() {
		// TODO: select traits for chars
//		addTrait(new LogPrinterTrait());
		addTrait(new ValueTrait());
	
	}

	public String getTrackerName() {
		return "CharTracker";
	}
}
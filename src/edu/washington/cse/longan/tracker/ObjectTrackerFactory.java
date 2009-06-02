package edu.washington.cse.longan.tracker;

public class ObjectTrackerFactory {

	@SuppressWarnings("unchecked")
	public static IObjectTracker create(Class obj) {
		// TODO Auto-generated method stub

		return createGenericObjectTracker(obj);

		// if (obj.isArray())
		// return createArrayTracker(obj);
		//
		// if (obj.isPrimitive())
		// return createPrimitiveTracker(obj);
		//
		// return null;
	}

	@SuppressWarnings("unchecked")
	private static IObjectTracker createGenericObjectTracker(Class obj) {
		return new GenericObjectTracker(obj);
	}

	@SuppressWarnings("unchecked")
	private static IObjectTracker createArrayTracker(Class obj) {
		// TODO
		return null;
	}

	@SuppressWarnings("unchecked")
	private static IObjectTracker createPrimitiveTracker(Class returnType) {
		// TODO Auto-generated method stub
		return null;
	}

}

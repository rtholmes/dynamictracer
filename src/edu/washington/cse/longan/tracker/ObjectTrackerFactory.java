package edu.washington.cse.longan.tracker;

import java.util.Collection;

import org.apache.log4j.Logger;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;

@SuppressWarnings("unchecked")
public class ObjectTrackerFactory {

	private static Class _collectionSignature;
	private static Class _stringSignature;

	static Logger _log = Logger.getLogger(ObjectTrackerFactory.class);

	private static Multiset<String> _unhandledTypes = HashMultiset.create();

	static {
		try {
			_stringSignature = Class.forName("java.lang.String");
			_collectionSignature = Class.forName("java.util.Collection");
			_log.info("");
		} catch (ClassNotFoundException cnfe) {
			_log.error(cnfe);
		}
	}

	public static IObjectTracker create(Class clazz) {
		if (isString(clazz))
			return new StringTracker(clazz);

		if (clazz.isPrimitive()) {
			if (clazz.getName().equals("boolean"))
				return new BooleanTracker(clazz);

			if (clazz.getName().equals("int") || clazz.getName().equals("double") || clazz.getName().equals("long")
					|| clazz.getName().equals("float") || clazz.getName().equals("short")) {
				return new NumberTracker(clazz);
			}

			_log.info("Unhandled primitive type: " + clazz.getName());
			return new PrimitiveTracker(clazz);
		}

		if (clazz.isArray())
			return new ArrayTracker(clazz);

		if (isCollection(clazz))
			return new CollectionTracker(clazz);

		// This is just for tracking
		_unhandledTypes.add(clazz.getName());

		return new GenericObjectTracker(clazz);
	}

	public static IObjectTracker create(Class clazz, int index, String name) {

		if (isString(clazz))
			return new StringTracker(clazz, index, name);

		if (clazz.isPrimitive()) {
			if (clazz.getName().equals("boolean"))
				return new BooleanTracker(clazz, index, name);

			if (clazz.getName().equals("int") || clazz.getName().equals("double") || clazz.getName().equals("long")
					|| clazz.getName().equals("float") || clazz.getName().equals("short")) {
				return new NumberTracker(clazz, index, name);
			}

			_log.info("Unhandled primitive type: " + clazz.getName());
			return new PrimitiveTracker(clazz, index, name);
		}

		if (clazz.isArray())
			return new ArrayTracker(clazz, index, name);

		if (isCollection(clazz))
			return new CollectionTracker(clazz, index, name);

		// This is just for tracking
		_unhandledTypes.add(clazz.getName());

		return new GenericObjectTracker(clazz, index, name);

	}

	private static boolean isCollection(Class clazz) {

		if (_collectionSignature.isAssignableFrom(clazz))
			return true;
		else
			return false;

	}

	private static boolean isString(Class clazz) {
		if (_stringSignature.isAssignableFrom(clazz))
			return true;
		else
			return false;
	}

	// @SuppressWarnings("unchecked")
	// private static IObjectTracker createGenericObjectTracker(Class obj) {
	// return new GenericObjectTracker(obj);
	// }
	//
	// @SuppressWarnings("unchecked")
	// private static IObjectTracker createArrayTracker(Class obj) {
	// // TODO
	// return null;
	// }
	//
	// @SuppressWarnings("unchecked")
	// private static IObjectTracker createPrimitiveTracker(Class returnType) {
	// // TODO Auto-generated method stub
	// return null;
	// }

}

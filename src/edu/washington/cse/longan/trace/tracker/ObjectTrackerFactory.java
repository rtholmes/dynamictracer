package edu.washington.cse.longan.trace.tracker;

import org.apache.log4j.Logger;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

@SuppressWarnings("unchecked")
public class ObjectTrackerFactory {

	private static Class _collectionSignature;
	private static Class _stringSignature;
	private static Class _numberSignature;

	static Logger _log = Logger.getLogger(ObjectTrackerFactory.class);

	private static Multiset<String> _unhandledTypes = HashMultiset.create();

	static {
		try {
			_stringSignature = Class.forName("java.lang.String");
			_numberSignature = Class.forName("java.lang.Number");
			_collectionSignature = Class.forName("java.util.Collection");
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

			if (clazz.getName().equals("char")) {
				return new CharTracker(clazz);
			}

			if (clazz.getName().equals("byte")) {
				return new ByteTracker(clazz);
			}

			_log.info("Unhandled primitive type: " + clazz.getName());
			return new PrimitiveTracker(clazz);
		}

		if (clazz.isArray())
			return new ArrayTracker(clazz);

		if (isCollection(clazz))
			return new CollectionTracker(clazz);

		if (isNumber(clazz))
			return new NumberTracker(clazz);

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

			if (clazz.getName().equals("char")) {
				return new CharTracker(clazz, index, name);
			}

			if (clazz.getName().equals("byte")) {
				return new ByteTracker(clazz, index, name);
			}

			_log.info("Unhandled primitive type: " + clazz.getName());
			return new PrimitiveTracker(clazz, index, name);
		}

		if (clazz.isArray())
			return new ArrayTracker(clazz, index, name);

		if (isCollection(clazz))
			return new CollectionTracker(clazz, index, name);

		if (isNumber(clazz))
			return new NumberTracker(clazz, index, name);

		// This is just for tracking
		_unhandledTypes.add(clazz.getName());

		return new GenericObjectTracker(clazz, index, name);

	}

	private static boolean isNumber(Class clazz) {
		if (_numberSignature.isAssignableFrom(clazz))
			return true;
		else
			return false;
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
}

/**
 * Created on Apr 15, 2009
 * @author rtholmes
 */
package edu.washington.cse.longan;

import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import java.lang.reflect.Array;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.JoinPoint.StaticPart;
import org.aspectj.lang.reflect.CodeSignature;

import ca.lsmr.common.log.LSMRLogger;

public class Collector {
	public static final boolean OUTPUT = true;

	private static Collector _instance = null;

	private static Logger _log = Logger.getLogger(Collector.class);

	private Hashtable<Integer, MethodTracker> _methods = new Hashtable<Integer, MethodTracker>();

	/**
	 * Uses the JPS.getId() as an index; the stored element is the 'base' index
	 * for the element associated with the JPS id. (JPS.id binds every join
	 * point itself so there can be multiple points for any program element)
	 */
	private Integer[] _ids = new Integer[1024];

	/**
	 * id -> milliseconds
	 */
	private Hashtable<Integer, Long> _profile = new Hashtable<Integer, Long>();

	/**
	 * current callstack
	 */
	private Stack<Integer> _callStack = new Stack<Integer>();

	/**
	 * method enter time. updated with the callstack so popping will give you
	 * the time the current method entered.
	 */
	private Stack<Long> _timeStack = new Stack<Long>();

	/**
	 * This index is used to maintain the _ids array: in this way the names of
	 * elements are tracked and using the name the common base id can be found.
	 */
	private Hashtable<String, Integer> _nameToBaseIdMap = new Hashtable<String, Integer>();

	private Collector() {
		try {
			LSMRLogger.startLog4J(true, Level.DEBUG);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Collector getInstance() {
		if (_instance == null) {
			_instance = new Collector();
			_log.trace("New Collector instantiated");
		}
		return _instance;
	}

	public static void clearInstance() {
		_instance = null;
		_log.trace("Collector cleared");
	}

	public void beforeClassInit(JoinPoint jp) {
		try {
			if (OUTPUT) {
				String out = "";
				for (int i = _callStack.size(); i > 0; i--)
					out += "\t";
				_log.debug("|-| Before class init: " + jp.getStaticPart().getSourceLocation().getWithinType());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void afterClassInit(JoinPoint jp) {
		try {
			if (OUTPUT) {
				String out = "";
				for (int i = _callStack.size(); i > 0; i--)
					out += "\t";
				_log.debug("|-| After class init: " + jp.getStaticPart().getSourceLocation().getWithinType());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void constructorEnter(JoinPoint jp, boolean isExternal) {

		JoinPoint.StaticPart jps = jp.getStaticPart();

		if (OUTPUT) {
			String out = "";

			for (int t = _callStack.size(); t > 0; t--)
				out += "\t";

			Signature sig = jp.getSignature();
			if (!isExternal)
				_log.debug(out + "|-->| " + sig);
			else
				_log.debug(out + "|x->| " + sig);
		}

		_callStack.push(getMethodId(jps));
		_timeStack.push(System.currentTimeMillis());

	}

	public void constructorExit(JoinPoint jp, boolean isExternal) {
		long delta = System.currentTimeMillis() - _timeStack.pop();

		record(jp, delta);

		_callStack.pop();

		if (OUTPUT) {
			String out = "";

			for (int t = _callStack.size(); t > 0; t--)
				out += "\t";

			Signature sig = jp.getSignature();
			if (!isExternal)
				_log.debug(out + "|<--| " + sig);
			else
				_log.debug(out + "|<-x| " + sig);
		}

	}

	public void fieldGet(JoinPoint jp) {
		// XXX: handle field sets
		if (OUTPUT) {
			String out = "";

			for (int t = _callStack.size(); t > 0; t--)
				out += "\t";

			_log.debug(out + "Field get: " + jp.toString());
		}
	}

	public void fieldSet(JoinPoint jp, Object newValue) {
		// XXX: handle field sets
		if (OUTPUT) {
			String out = "";

			for (int t = _callStack.size(); t > 0; t--)
				out += "\t";

			_log.debug(out + "Field set: " + jp.getSignature().toString() + " to: " + newValue);
		}
	}

	public void methodEnter(JoinPoint jp, boolean isExternal) {

		int id = getMethodId(jp.getStaticPart());

		_methods.get(id).methodEnter(jp, _callStack);

		if (OUTPUT) {
			String out = "";
			for (int i = _callStack.size(); i > 0; i--)
				out += "\t";

			String sig = _methods.get(id).getName();

			if (!isExternal)
				_log.debug(out + "--> " + sig + " # args: " + jp.getArgs().length);
			else
				_log.debug(out + "-x> " + sig + " # args: " + jp.getArgs().length);

			printArgs(jp);
		}

		_callStack.push(id);

		_timeStack.push(System.currentTimeMillis());

	}

	public void methodExit(JoinPoint jp, Object retObject, boolean isExternal) {

		if (retObject != null) {
			if (OUTPUT) {
				String out = "";
				for (int i = _callStack.size(); i > 0; i--)
					out += "\t";
				_log.debug(out + "Return: " + retObject);
			}
		}
		// RFE: handle return value
		methodExit(jp, isExternal);

	}

	public void methodExit(JoinPoint jp, boolean isExternal) {
		long delta = System.currentTimeMillis() - _timeStack.pop();

		record(jp, delta);
		// record(jp, jp.getStaticPart(), end - start);

		_callStack.pop();

		if (OUTPUT) {
			String out = "";
			for (int i = _callStack.size(); i > 0; i--)
				out += "\t";

			Signature sig = jp.getSignature();

			if (!isExternal)
				out += "<-- " + sig + " time: " + delta;
			else
				out += "<x- " + sig + " time: " + delta;

			_log.debug(out);
		}

	}

	public void beforeObjectInit(JoinPoint jp) {
		if (OUTPUT) {
			String out = "";

			for (int t = _callStack.size(); t > 0; t--)
				out += "\t";

			out += "|-| Before obj init: " + jp.getTarget().getClass().getName();

			_log.debug(out);
		}
	}

	public void afterObjectInit(JoinPoint jp) {
		if (OUTPUT) {
			String out = "";

			for (int t = _callStack.size(); t > 0; t--)
				out += "\t";

			out += "|-| After obj init: " + jp.getTarget().getClass().getName();

			_log.debug(out);
		}
	}

	public void writeToScreen() {
		try {
			if (true) {
				_log.info("after last test case");

				long total = 0;
				for (Integer i : _methods.keySet()) {
					long time = _profile.get(i);
					total += time;
					_log.info(i + "\t " + time + " \t-> " + _methods.get(i).getName());
				}
				_log.info("Total time (with double counting): " + total);

				for (MethodTracker mt : _methods.values()) {
					String methodName = mt.getName();

					if (methodName != null)
						_log.info(methodName + " called by:");
					else
						_log.error("unknown id: " + mt.getId());

					Vector<String> calledBys = new Vector<String>();
					for (Integer calledById : mt.getCalledBy()) {
						MethodTracker calledByMethod = _methods.get(calledById);

						if (calledByMethod != null) {
							String calledByName = calledByMethod.getName();

							calledByName += " id: " + calledById + " -> " + _ids[calledById];

							if (!calledBys.contains(calledByName)) {
								calledBys.add(calledByName);
							} else {
								_log.error("multiple copies of? " + calledByName);
							}

						} else
							calledBys.add(calledById + "");
					}

					Collections.sort(calledBys);
					for (String cbn : calledBys)
						_log.info("\t" + cbn);

				}
			}
		} catch (Exception e) {
			e.fillInStackTrace();
			_log.error(e);
		}
	}

	/**
	 * This has a _HUGE_ problem; ids are only unique PER CLASS, meaing an id of
	 * 0 will conflict with every single class. We can use pertypewithin on the
	 * aspect description but that will violate what we have happening here.
	 * 
	 * @param jps
	 * @return
	 */
	private int methodidcounter = 0;

	private Integer getMethodId(StaticPart jps) {

		String name = "";
		int id = -1;

		boolean avoidDuplicateBug = true;
		if (avoidDuplicateBug) {
			name = jps.getSignature().toString();

			if (!_nameToBaseIdMap.containsKey(name)) {
				_nameToBaseIdMap.put(name, methodidcounter++);
			}

			id = _nameToBaseIdMap.get(name);
			if (!_methods.containsKey(id)) {
				_methods.put(id, new MethodTracker(id, name));
			}

			// _log.trace("id: "+id+" name: "+name);
			return id;

		} else {
			// PERFORMANCE: fix getMethodId
			// RFE: fix this
			id = jps.getId();

			if (_ids[id] != null) {
				// id has already been mapped to the base id so return it
				return _ids[id];
			} else {
				// haven't encountered this id before, create one
				name = jps.getSignature().toString();

				// multiple ids can exist for the same methods (e.g., from
				// different
				// call sites); merge them
				Integer baseId = _nameToBaseIdMap.get(name);
				if (baseId == null) {
					// there is no base id yet; use this one
					_nameToBaseIdMap.put(name, id);
					_ids[id] = id;
					baseId = id;
				} else {
					// there is a base id, choose it instead and
					// add a new mapping for this one
					_ids[id] = baseId;
				}

				id = baseId;

				if (!_methods.containsKey(id)) {
					// update the method map. this map only uses the base id
					_methods.put(id, new MethodTracker(id, name));
				}

			}
			return id;
		}
	}

	@SuppressWarnings("unchecked")
	private void printArgs(JoinPoint jp) {
		// if (false) {
		try {

			CodeSignature sig = (CodeSignature) jp.getSignature();

			String[] names = sig.getParameterNames();
			Class[] types = sig.getParameterTypes();

			Object[] args = jp.getArgs();
			for (int i = 0; i < args.length; i++) {
				Object arg = args[i];
				Class argType = types[i];
				@SuppressWarnings("unused")
				String argName = names[i];

				@SuppressWarnings("unused")
				String argV = "-[null]-";

				String out = "";
				if (OUTPUT)
					for (int t = _callStack.size(); t > 0; t--)
						out += "\t";

				if (arg != null) {
					if (arg instanceof String) {

						if (OUTPUT)
							_log.debug(out + "\tArg " + i + ", String, length: " + ((String) arg).length());

					} else if (arg.getClass().isArray()) {

						if (OUTPUT)
							_log.debug(out + "\tArg " + i + ", Array, length: " + Array.getLength(arg) + " class: "
									+ arg.getClass().getComponentType());

					} else if (arg instanceof Collection) {

						if (OUTPUT)
							_log.debug(out + "\tArg " + i + ", Collection, size: " + ((Collection) arg).size() + " ( "
									+ arg.getClass() + " )");

					} else if (arg instanceof Set) {

						if (OUTPUT)
							_log.debug(out + "\tArg " + i + ", Set, size: " + ((Set) arg).size() + " ( "
									+ arg.getClass() + " )");

					} else if (arg instanceof List) {

						if (OUTPUT)
							_log.debug(out + "\tArg " + i + ", List, size: " + ((List) arg).size() + " ( "
									+ arg.getClass() + " )");

					} else {

						// RFE: test this with subclasses (e.g., if arg is a
						// Vector but is declared as a List)

						if (OUTPUT)
							_log.debug(out + "\tArg " + i + ", unhandled type: " + arg.getClass().getName());

					}
				} else {
					if (OUTPUT)
						_log.debug(out + "\tArg " + i + ", null, supposed to be type: " + argType.getName());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// }
	}

	private void record(JoinPoint jp, long delta) {
		int id = getMethodId(jp.getStaticPart());

		Long val = _profile.get(id);

		if (val == null) {
			_profile.put(id, delta);
			// idToSignatureMap.put(id, jp.getSignature());
		} else
			_profile.put(id, val + delta);

	}

	public void handleException(JoinPoint jp, Object instance, Object exception) {

		if (OUTPUT) {
			String out = "";
			for (int i = _callStack.size(); i > 0; i--)
				out += "\t";

			MethodTracker mt = _methods.get(_callStack.peek());

			_log.debug(out + "Handle exception: " + exception + " in: " + mt.getName());
		}
	}

	public void beforeCreateException(JoinPoint jp) {

		if (OUTPUT) {
			String out = "";
			for (int i = _callStack.size(); i > 0; i--)
				out += "\t";
			_log.debug(out + "Before create exception: " + jp.getSignature().toString());
		}
		constructorEnter(jp, true);
	}

	public void afterCreateException(JoinPoint jp) {
		constructorExit(jp, true);
		if (OUTPUT) {
			String out = "";
			for (int i = _callStack.size(); i > 0; i--)
				out += "\t";
			_log.debug(out + "After create exception: " + jp.getSignature().toString());
		}
	}
}

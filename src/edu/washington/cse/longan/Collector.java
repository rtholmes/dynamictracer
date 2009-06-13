/**
 * Created on Apr 15, 2009
 * @author rtholmes
 */
package edu.washington.cse.longan;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.CodeSignature;

import ca.lsmr.common.log.LSMRLogger;

import com.google.common.collect.Multiset;

import edu.washington.cse.longan.tracker.IObjectTracker;

public class Collector {
	public static final boolean OUTPUT = false;
	public static final boolean SUMMARY_OUTPUT = false;

	private static final String UNKNOWN_CALLER = "Unknown";

	private static Collector _instance = null;

	private static Logger _log = Logger.getLogger(Collector.class);

	public static void clearInstance() {
		_instance = null;
		_log.warn("Collector cleared");
	}

	public static Collector getInstance() {
		if (_instance == null) {
			_instance = new Collector();
			_log.trace("New Collector instantiated");
		}
		return _instance;
	}

	private Hashtable<Integer, MethodAgent> _methods = new Hashtable<Integer, MethodAgent>();
	private Hashtable<Integer, FieldAgent> _fields = new Hashtable<Integer, FieldAgent>();

	/**
	 * Uses the JPS.getId() as an index; the stored element is the 'base' index for the element associated with the JPS
	 * id. (JPS.id binds every join point itself so there can be multiple points for any program element)
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
	 * method enter time. updated with the callstack so popping will give you the time the current method entered.
	 */
	private Stack<Long> _timeStack = new Stack<Long>();

	/**
	 * This index is used to maintain the _ids array: in this way the names of elements are tracked and using the name
	 * the common base id can be found.
	 */
	private Hashtable<String, Integer> _nameToBaseIdMap = new Hashtable<String, Integer>();

	/**
	 * This has a _HUGE_ problem; ids are only unique PER CLASS, meaing an id of 0 will conflict with every single
	 * class. We can use pertypewithin on the aspect description but that will violate what we have happening here.
	 * 
	 * @param jps
	 * @return
	 */
	private int methodidcounter = 0;

	private Collector() {
		try {
			LSMRLogger.startLog4J(true, Level.DEBUG);
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

	public void afterCreateException(JoinPoint jp) {
		constructorExit(jp, true);
		if (OUTPUT) {
			String out = "";
			for (int i = _callStack.size(); i > 0; i--)
				out += "\t";
			_log.debug(out + "After create exception: " + jp.getSignature().toString());
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

	public void beforeCreateException(JoinPoint jp) {

		if (OUTPUT) {
			String out = "";
			for (int i = _callStack.size(); i > 0; i--)
				out += "\t";
			_log.debug(out + "Before create exception: " + jp.getSignature().toString());
		}
		constructorEnter(jp, true);
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

	public void constructorEnter(JoinPoint jp, boolean isExternal) {

		// parent class & interfaces
		// jp.getSignature().getDeclaringType().getSuperclass();
		// jp.getSignature().getDeclaringType().getInterfaces();

		JoinPoint.StaticPart jps = jp.getStaticPart();

		int id = getMethodId(jp);

		_methods.get(id).methodEnter(jp, _callStack);

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

		_callStack.push(getMethodId(jp));
		_timeStack.push(System.currentTimeMillis());

	}

	public void constructorExit(JoinPoint jp, boolean isExternal) {
		long delta = System.currentTimeMillis() - _timeStack.pop();

		recordProfileData(jp, delta);

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

	public void exceptionHandled(JoinPoint jp, Object instance, Object exception) {

		if (OUTPUT) {
			String out = "";
			for (int i = _callStack.size(); i > 0; i--)
				out += "\t";

			MethodAgent mt = _methods.get(_callStack.peek());

			_log.debug(out + "|-| Exception handled: " + exception + " in: " + mt.getName());
		}
	}

	public void exceptionThrown(JoinPoint jp, Throwable exception, boolean isExternal) {

		if (OUTPUT) {
			String out = "";
			for (int i = _callStack.size(); i > 0; i--)
				out += "\t";

			// MethodTracker mt = _methods.get(_callStack.peek());

			if (!isExternal)
				_log.debug(out + "|-| Exception thrown: " + exception + " in: " + jp.getSignature().toString());
			else
				_log.debug(out + "|x| Exception thrown: " + exception + " in: " + jp.getSignature().toString());
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

	private Integer getMethodId(JoinPoint jp) {

		String name = "";
		int id = -1;

		boolean avoidDuplicateBug = true;
		if (avoidDuplicateBug) {
			name = jp.getSignature().toString();

			if (!_nameToBaseIdMap.containsKey(name)) {
				_nameToBaseIdMap.put(name, methodidcounter++);
			}

			id = _nameToBaseIdMap.get(name);
			if (!_methods.containsKey(id)) {
				_methods.put(id, new MethodAgent(id, jp));
			}

			// _log.trace("id: "+id+" name: "+name);
			return id;

		} else {
			// PERFORMANCE: fix getMethodId
			// RFE: fix this
			id = jp.getStaticPart().getId();

			if (_ids[id] != null) {
				// id has already been mapped to the base id so return it
				return _ids[id];
			} else {
				// haven't encountered this id before, create one
				name = jp.getSignature().toString();

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
					_methods.put(id, new MethodAgent(id, jp));
				}

			}
			return id;
		}
	}

	Collection<Integer> getUniqueCallers(int methodId) {
		HashSet<Integer> callers = new HashSet<Integer>();

		Multiset<Integer> calledBys = _methods.get(methodId).getCalledBy();

		for (Integer calledBy : calledBys)
			callers.add(calledBy);

		return callers;
	}

	public void methodEnter(JoinPoint jp, boolean isExternal) {

		int id = getMethodId(jp);

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

		long delta = System.currentTimeMillis() - _timeStack.pop();

		_callStack.pop();
		
		recordProfileData(jp, delta);
		getMethodTracker(jp).methodExit(jp, retObject, _callStack);

		

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

				String argName = names[i];

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

					} else if (arg instanceof Set) {

						if (OUTPUT)
							_log.debug(out + "\tArg " + i + ", Set, size: " + ((Set) arg).size() + " ( "
									+ arg.getClass() + " )");

					} else if (arg instanceof List) {

						if (OUTPUT)
							_log.debug(out + "\tArg " + i + ", List, size: " + ((List) arg).size() + " ( "
									+ arg.getClass() + " )");

					} else if (arg instanceof Collection) {

						if (OUTPUT)
							_log.debug(out + "\tArg " + i + ", Collection, size: " + ((Collection) arg).size() + " ( "
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

	private void recordProfileData(JoinPoint jp, long delta) {
		int id = getMethodId(jp);

		Long val = _profile.get(id);

		if (val == null) {
			_profile.put(id, delta);
			// idToSignatureMap.put(id, jp.getSignature());
		} else
			_profile.put(id, val + delta);

	}

	public void writeToScreen() {
		try {
			if (SUMMARY_OUTPUT) {
				_log.info("Writing Statistics");

				Vector<String> sortedNames = new Vector<String>(_nameToBaseIdMap.keySet());
				Collections.sort(sortedNames);

				long total = 0;
				for (String name : sortedNames) {
					int elementId = _nameToBaseIdMap.get(name);
					MethodAgent methodAgent = _methods.get(elementId);

					_log.info(name);
					
					if (!methodAgent.getName().equals(name)) {
						// this should never happen
						_log.error("Method names don't match.");
					}

					Collection<Integer> uniqueCallers = getUniqueCallers(elementId);

					for (Integer caller : uniqueCallers) {

						MethodAgent calledBy = _methods.get(caller);
						String calledByName = "";
						
						if (calledBy != null)
							calledByName = calledBy.getName();
						else
							calledByName = UNKNOWN_CALLER;

						
						int calledByCount = _methods.get(elementId).getCalledBy().count(caller);
						_log.info("\t<-- id: " + caller + "; # calls: " + calledByCount + "; name: " + calledByName);
						
						IObjectTracker[] paramTracker = methodAgent.getParameterTrackers().get(caller);
						IObjectTracker returnTracker = methodAgent.getReturnTrackers().get(caller);

						// XXX: param trackers only seem to be hit once (confusingly not consistently), even if a method is called N times.
						// XXX: return trackers buggered as well
						if (paramTracker.length > 0) {
							for (IObjectTracker tracker : paramTracker) {
								_log.info("\t\tParam: "+tracker.getTrackerName()+" - [ idx: " + tracker.getPosition() + " ] name: " + tracker.getName()+ " static type: "+tracker.getStaticTypeName());
								_log.info("\t\t\t" + tracker.toString());
							}
						}

						if (returnTracker != null) {
							_log.info("\t\tReturn: "+returnTracker.getTrackerName()+" static type: "+returnTracker.getStaticTypeName());
							_log.info("\t\t\t" + returnTracker.toString());
						}
					}

				}
				_log.info("Total time (with double counting): " + total);

				for (MethodAgent mt : _methods.values()) {
					String methodName = mt.getName();

					_log.info("Time: " + _profile.get(mt.getId()) + " element: " + methodName);
					// if (methodName != null)
					// _log.info(methodName + " called by:");
					// else
					// _log.error("unknown id: " + mt.getId());

					// Vector<String> calledBys = new Vector<String>();
					// for (Integer calledById : mt.getCalledBy()) {
					// MethodTracker calledByMethod = _methods.get(calledById);
					//
					// if (calledByMethod != null) {
					// String calledByName = calledByMethod.getName();
					//
					// calledByName += " id: " + calledById + " -> " +
					// _ids[calledById];
					//
					// if (!calledBys.contains(calledByName)) {
					// calledBys.add(calledByName);
					// } else {
					// _log.error("multiple copies of? " + calledByName);
					// }
					//
					// } else
					// calledBys.add(calledById + "");
					// }

					// Collections.sort(calledBys);
					// for (String cbn : calledBys)
					// _log.info("\t" + cbn);

				}
			}
		} catch (Exception e) {
			e.fillInStackTrace();
			_log.error(e);
		}
	}

	private MethodAgent getMethodTracker(JoinPoint jp) {
		return _methods.get(getMethodId(jp));
	}

}

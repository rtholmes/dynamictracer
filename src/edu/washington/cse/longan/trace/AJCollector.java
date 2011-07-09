/**
 * Created on Apr 15, 2009
 * @author rtholmes
 */
package edu.washington.cse.longan.trace;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;

import ca.lsmr.common.util.TimeUtility;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multiset;

import edu.washington.cse.longan.Logger;
import edu.washington.cse.longan.io.SessionXMLWriter;
import edu.washington.cse.longan.model.FieldElement;
import edu.washington.cse.longan.model.ILonganConstants;
import edu.washington.cse.longan.model.MethodElement;
import edu.washington.cse.longan.model.Session;
import edu.washington.cse.longan.trace.tracker.IObjectTracker;

/**
 * This is the main data collector used by the Aspect information collector. The _session field contains all of the collected data.
 * 
 * @author rtholmes
 * 
 */
public class AJCollector {
	private static AJCollector _instance = null;
	private static Logger _log = Logger.getLogger(AJCollector.class);

	public static boolean OUTPUT = false; // ILonganConstants.OUTPUT_SCREEN;
	public static final boolean SUMMARY_OUTPUT = false; // ILonganConstants.OUTPUT_SUMMARY;

	// public static final String UNKNOWN_CALLER = "Unknown";

	public static void clearInstance() {
		if (_instance != null)
			_log.warn("AJCollector cleared");

		_instance = null;
	}

	public static AJCollector getInstance() {
		if (_instance == null) {
			_instance = new AJCollector();

		}
		return _instance;
	}

	/**
	 * current callstack
	 */
	// private Stack<Integer> getCurrentCallstack() = new Stack<Integer>();

	private ThreadLocal<Stack<Integer>> _callStack = new ThreadLocal<Stack<Integer>>() {
		protected java.util.Stack<Integer> initialValue() {
			return new Stack<Integer>();
		};
	};

	//
	// private Hashtable<Integer, AJFieldAgent> _fields = new Hashtable<Integer, AJFieldAgent>();
	// /**
	// * Uses the JPS.getId() as an index; the stored element is the 'base' index for the element associated with the
	// JPS
	// * id. (JPS.id binds every join point itself so there can be multiple points for any program element)
	// */
	// private Integer[] _ids = new Integer[1024];
	//
	// private Hashtable<Integer, AJMethodAgent> _methods = new Hashtable<Integer, AJMethodAgent>();
	//
	// /**
	// * This index is used to maintain the _ids array: in this way the names of elements are tracked and using the name
	// * the common base id can be found.
	// */
	// private Hashtable<String, Integer> _nameToBaseIdMap = new Hashtable<String, Integer>();
	//
	// /**
	// * id -> milliseconds
	// */
	// private Hashtable<Integer, Long> _profile = new Hashtable<Integer, Long>();

	private Session _session;

	/**
	 * method enter time. updated with the callstack so popping will give you the time the current method entered.
	 */
	private Stack<Long> _timeStack = new Stack<Long>();

	private Stack<Integer> _exceptionStack = null;
	/**
	 * This has a _HUGE_ problem; ids are only unique PER CLASS, meaing an id of 0 will conflict with every single class. We can use pertypewithin on the aspect description but
	 * that will violate what we have happening here.
	 * 
	 * @param jps
	 * @return
	 */
	private int _masterCounter = 0;

	private AJCollector() {
		try {
			// LOGGING
			// LSMRLogger.startLog4J(true, ILonganConstants.LOGGING_LEVEL);

			_session = new Session(TimeUtility.getCurrentLSMRDateString());
			_log.info("New AJCollector instantiated");
			// _log.info("Tracing started");

			Runtime.getRuntime().addShutdownHook(new Thread() {

				public void run() {
					_log.info("Shtudown is happening now");
					writeToScreen();
					writeToDisk();
				}
			});

		} catch (Exception e) {
			_log.error(e);
		}
	}

	private Stack<Integer> getCurrentCallstack() {
		return _callStack.get();
	}

	/**
	 * Not currently used.
	 * 
	 * @param jp
	 */
	public void afterClassInit(JoinPoint jp) {
		try {
			methodExit(jp, null, false);
			if (OUTPUT) {
				String out = "";
				for (int i = getCurrentCallstack().size(); i > 0; i--)
					out += "\t";
				// _log.debug("|-| After class init: " + jp.getStaticPart().getSourceLocation().getWithinType());
				_log.debug("|-| After class init: " + jp.getSignature());
			}
		} catch (Exception e) {
			_log.error(e);
		}
	}

	/**
	 * 
	 * 
	 * @param jp
	 */
	@SuppressWarnings("unchecked")
	public void afterCreateException(JoinPoint jp) {

		// NOTE: if exception tracking seems screwed up, check to see if this is
		// the culprit; I don't really like this solution at all, but for some reason
		// the exceptionThrown method isn't being triggered by exceptions arising
		// from constructors.

		// Needed to capture call stack for exceptions arising in constructors
		_exceptionStack = (Stack<Integer>) getCurrentCallstack().clone();

		constructorExit(jp, true);
		if (OUTPUT) {
			String out = "";
			for (int i = getCurrentCallstack().size(); i > 0; i--)
				out += "\t";
			_log.debug(out + "After create exception: " + jp.getSignature().toString());
		}
	}

	/**
	 * Not currently used.
	 * 
	 * @param jp
	 */
	public void afterObjectInit(JoinPoint jp) {
		methodExit(jp, null, false);
		if (OUTPUT) {
			String out = "";

			for (int t = getCurrentCallstack().size(); t > 0; t--)
				out += "\t";

			// out += "|-| After obj init: " + jp.getTarget().getClass().getName();
			out += "|-| After obj init: " + jp.getSignature();

			_log.debug(out);
		}
	}

	/**
	 * Not currently used.
	 * 
	 * @param jp
	 */
	public void beforeClassInit(JoinPoint jp) {
		try {
			methodEnter(jp, false);
			if (OUTPUT) {
				String out = "";
				for (int i = getCurrentCallstack().size(); i > 0; i--)
					out += "\t";
				// _log.debug("|-| Before class init: " + jp.getStaticPart().getSourceLocation().getWithinType());
				_log.debug("|-| Before class init: " + jp.getSignature());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Not currently used; it just passes through to constructorEnter. Mainly exists for future extension.
	 * 
	 * @param jp
	 */
	public void beforeCreateException(JoinPoint jp) {

		if (OUTPUT) {
			String out = "";
			for (int i = getCurrentCallstack().size(); i > 0; i--)
				out += "\t";
			_log.debug(out + "Before create exception: " + jp.getSignature().toString());
		}
		constructorEnter(jp, true);
	}

	@SuppressWarnings("unchecked")
	public void exceptionHandled(JoinPoint jp, Object instance, Object exception) {
		if (getCurrentCallstack().isEmpty()) {
			// RFE: handle the case where not everything is instrumented.
			_log.warn("Exception handled but the call stack is empty: " + jp.getSignature());
		} else {
			MethodElement mt = _session.getMethod(getCurrentCallstack().peek());
			// NOTE: had to remove these to make Log4j.FileAppenderTest.testDirectoryCreation work

			Preconditions.checkArgument(exception instanceof Throwable);

			// XXX: handle case where exception comes from somewhere we don't instrument
			if (_exceptionStack == null) {
				_log.warn("Exception handled but the exception stack is empty: " + jp.getSignature());

				// Preconditions.checkNotNull(_exceptionStack,
				// "There should be a current exception stack if one is to be caught. "
				// + "Null exception stack: %s; ex type: %s; ex msg: %s;", mt.getName(), exception.getClass().getName(),
				// ((Throwable) exception)
				// .getMessage());

				_exceptionStack = (Stack<Integer>) getCurrentCallstack().clone();
				mt.throwException(_exceptionStack, exception.getClass().getName(), ((Throwable) exception).getMessage());

			}

			mt.handleException(_exceptionStack, exception.getClass().getName(), ((Throwable) exception).getMessage());

			// this is a new exception
			_exceptionStack = null;

			if (OUTPUT) {
				_log.debug("handling current exception, exception stack cleared. " + mt.getName() + " ex type: " + exception.getClass().getName() + " ex msg: "
						+ ((Throwable) exception).getMessage());
			}

			if (OUTPUT) {
				String out = "";
				for (int i = getCurrentCallstack().size(); i > 0; i--)
					out += "\t";

				_log.debug(out + "|-| Exception handled: " + exception + " in: " + mt.getName());
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void exceptionThrown(JoinPoint jp, Throwable exception, boolean isExternal) {
		if (getCurrentCallstack().isEmpty()) {
			// RFE: handle the case where not everything is instrumented.
		} else {
			MethodElement mt = _session.getMethod(getCurrentCallstack().peek());

			if (_exceptionStack == null) {
				// this is a new exception
				_exceptionStack = (Stack<Integer>) getCurrentCallstack().clone();
				mt.throwException(_exceptionStack, exception.getClass().getName(), ((Throwable) exception).getMessage());
				if (OUTPUT) {
					_log.debug("Rew exception encountered, new exception stack created. " + mt.getName() + " ex type: " + exception.getClass().getName() + " ex msg: "
							+ ((Throwable) exception).getMessage());
				}
			} else {
				mt.reThrowException(_exceptionStack, exception.getClass().getName(), ((Throwable) exception).getMessage());
				if (OUTPUT) {
					_log.debug("Rethrowing existing exception. " + mt.getName() + " ex type: " + exception.getClass().getName() + " ex msg: "
							+ ((Throwable) exception).getMessage());
				}
			}

			if (OUTPUT) {
				String out = "";
				for (int i = getCurrentCallstack().size(); i > 0; i--)
					out += "\t";

				if (!isExternal)
					_log.debug(out + "|-| Exception thrown: " + exception + " in: " + jp.getSignature().toString());
				else
					_log.debug(out + "|x| Exception thrown: " + exception + " in: " + jp.getSignature().toString());
			}
		}
	}

	public void beforeObjectInit(JoinPoint jp) {
		methodEnter(jp, false);
		if (OUTPUT) {
			String out = "";

			for (int t = getCurrentCallstack().size(); t > 0; t--)
				out += "\t";

			// out += "|-| Before obj init: " + jp.getTarget().getClass().getName();
			out += "|-| Before obj init: " + jp.getSignature();

			_log.debug(out);
		}
	}

	public void constructorEnter(JoinPoint jp, boolean isExternal) {

		// parent class & interfaces
		// jp.getSignature().getDeclaringType().getSuperclass();
		// jp.getSignature().getDeclaringType().getInterfaces();

		JoinPoint.StaticPart jps = jp.getStaticPart();

		int id = getMethodId(jp, isExternal, true);

		((AJMethodAgent) _session.getMethod(id)).methodEnter(jp, getCurrentCallstack());
		// _methods.get(id).methodEnter(jp, getCurrentCallstack());

		if (OUTPUT) {
			String out = "";

			for (int t = getCurrentCallstack().size(); t > 0; t--)
				out += "\t";

			Signature sig = jp.getSignature();
			if (!isExternal)
				_log.debug(out + "|-->| " + sig);
			else
				_log.debug(out + "|x->| " + sig);
		}

		getCurrentCallstack().push(getMethodId(jp, isExternal, true));
		_timeStack.push(System.currentTimeMillis());

	}

	public void constructorExit(JoinPoint jp, boolean isExternal) {
		long delta = System.currentTimeMillis() - _timeStack.pop();

		recordProfileData(jp, delta);

		getCurrentCallstack().pop();

		if (OUTPUT) {
			String out = "";

			for (int t = getCurrentCallstack().size(); t > 0; t--)
				out += "\t";

			Signature sig = jp.getSignature();
			if (!isExternal)
				_log.debug(out + "|<--| " + sig);
			else
				_log.debug(out + "|<-x| " + sig);
		}

	}

	public void fieldGet(JoinPoint jp, Object fieldValue) {

		int id = getFieldID(jp);
		((AJFieldAgent) _session.getField(id)).fieldGet(jp, getCurrentCallstack(), fieldValue);

		if (OUTPUT) {
			String out = "";

			for (int t = getCurrentCallstack().size(); t > 0; t--)
				out += "\t";

			_log.debug(out + "Field get: " + jp.getSignature() + " value: " + fieldValue);
		}
	}

	public void fieldSet(JoinPoint jp, Object newValue) {
		int id = getFieldID(jp);
		((AJFieldAgent) _session.getField(id)).fieldSet(jp, getCurrentCallstack(), newValue);

		if (OUTPUT) {
			String out = "";

			for (int t = getCurrentCallstack().size(); t > 0; t--)
				out += "\t";

			_log.debug(out + "Field set: " + jp.getSignature().toString() + " to: " + newValue);
		}
	}

	private Integer getFieldID(JoinPoint jp) {
		// MethodElement mt = _session.getMethod(getCurrentCallstack().peek());
		String name = "";
		int id = -1;

		name = jp.getSignature().toString();

		if (!_session.hasIDForElement(name)) {
			// _session.addIDForElement(name, _masterCounter++);
			id = _masterCounter++;
		} else {
			id = _session.getIdForElement(name);
		}

		if (!_session.fieldExists(id)) {
			_session.addField(id, new AJFieldAgent(id, jp));
		}

		// _log.trace("id: "+id+" name: "+name);
		return id;
	}

	private Integer getMethodId(JoinPoint jp, boolean isExternal, boolean isExternalKnown) {

		String name = "";
		int id = -1;

		boolean avoidDuplicateBug = true;
		if (avoidDuplicateBug) {

			// this seems complicated but allows us to resolve what is actually going on.
			// e.g., Collections.add(Object) -> ArrayList.add(Object)
			name = AJMethodAgent.getMethodName(jp);

			// RFE: Could detect isConstructor using: jp.getSignature().getDeclaringTypeName()
			// and comparing that to the position of the first (

			if (!_session.hasIDForElement(name)) {
				// _session.addIDForElement(name, _masterCounter++);
				// _nameToBaseIdMap.put(name, methodidcounter++);
				id = _masterCounter++;
			} else {
				id = _session.getIdForElement(name);
			}

			if (!_session.methodExists(id)) {
				if (!isExternalKnown) {
					throw new AssertionError("Can't create a new methodagent if without being sure that it is external or internal.");
				}

				// BUG: isExternal is always true for exceptions
				_session.addMethod(id, new AJMethodAgent(id, jp, isExternal));

				// Put a 0 entry into the profile table for new elements
				// this just avoids a warning for the last test method
				// which won't have exited (to get a profile value) before
				// we output the profile table.
				recordProfileData(jp, 0);
			}

			// _log.trace("id: "+id+" name: "+name);
			return id;

		} else {
			// // PERF: fix getMethodId
			// id = jp.getStaticPart().getId();
			//
			// if (_ids[id] != null) {
			// // id has already been mapped to the base id so return it
			// return _ids[id];
			// } else {
			// // haven't encountered this id before, create one
			// name = jp.getSignature().toString();
			//
			// // multiple ids can exist for the same methods (e.g., from
			// // different
			// // call sites); merge them
			// Integer baseId = _nameToBaseIdMap.get(name);
			// if (baseId == null) {
			// // there is no base id yet; use this one
			// _nameToBaseIdMap.put(name, id);
			// _ids[id] = id;
			// baseId = id;
			// } else {
			// // there is a base id, choose it instead and
			// // add a new mapping for this one
			// _ids[id] = baseId;
			// }
			//
			// id = baseId;
			//
			// if (!_methods.containsKey(id)) {
			// // update the method map. this map only uses the base id
			// _methods.put(id, new AJMethodAgent(id, jp));
			// }
			//
			// }
			// return id;
			return -1;
		}
	}

	private AJMethodAgent getMethodTracker(JoinPoint jp) {
		return (AJMethodAgent) _session.getMethod(getMethodId(jp, false, false));
	}

	Collection<Integer> getUniqueCallers(int methodId) {
		HashSet<Integer> callers = new HashSet<Integer>();

		Multiset<Integer> calledBys = _session.getMethod(methodId).getCalledBy();

		for (Integer calledBy : calledBys)
			callers.add(calledBy);

		return callers;
	}

	public void methodEnter(JoinPoint jp, boolean isExternal) {

		int id = getMethodId(jp, isExternal, true);

		((AJMethodAgent) _session.getMethod(id)).methodEnter(jp, getCurrentCallstack());

		if (OUTPUT) {
			String out = "";
			for (int i = getCurrentCallstack().size(); i > 0; i--)
				out += "\t";

			String sig = _session.getMethod(id).getName();

			if (sig.equals("boolean java.util.Collection.add(Object)")) {
				Object thisObj = jp.getThis();
				Object targetObj = jp.getTarget();
				String targetName = targetObj.getClass().getName();
				String targetPkg = targetObj.getClass().getPackage().getName();
				String kind = jp.getKind();
				Signature sign = jp.getSignature();

				String mName = "";
				if (sign instanceof MethodSignature) {
					mName = ((MethodSignature) sign).getMethod().getName();

				}
				_log.info("lets' take a break here");
			}

			if (!isExternal)
				_log.debug(out + "--> " + sig + " # args: " + jp.getArgs().length);
			else
				_log.debug(out + "-x> " + sig + " # args: " + jp.getArgs().length);

			printArgs(jp);
		}

		getCurrentCallstack().push(id);

		_timeStack.push(System.currentTimeMillis());

	}

	public void methodExit(JoinPoint jp, Object retObject, boolean isExternal) {

		if (retObject != null) {
			if (OUTPUT) {
				String out = "";
				for (int i = getCurrentCallstack().size(); i > 0; i--)
					out += "\t";
				_log.debug(out + "Return: " + retObject);
			}
		}

		long delta = System.currentTimeMillis() - _timeStack.pop();

		getCurrentCallstack().pop();

		recordProfileData(jp, delta);

		AJMethodAgent methodTracker = getMethodTracker(jp);

		if (!ILonganConstants.CALLSTACK_ONLY)
			methodTracker.methodExit(jp, retObject, getCurrentCallstack());

		if (OUTPUT) {
			String out = "";
			for (int i = getCurrentCallstack().size(); i > 0; i--)
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
					for (int t = getCurrentCallstack().size(); t > 0; t--)
						out += "\t";

				if (arg != null) {
					if (arg instanceof String) {

						if (OUTPUT)
							_log.debug(out + "\tArg " + i + ", String, length: " + ((String) arg).length());

					} else if (arg.getClass().isArray()) {

						if (OUTPUT)
							_log.debug(out + "\tArg " + i + ", Array, length: " + Array.getLength(arg) + " class: " + arg.getClass().getComponentType());

					} else if (arg instanceof Set) {

						if (OUTPUT)
							_log.debug(out + "\tArg " + i + ", Set, size: " + ((Set) arg).size() + " ( " + arg.getClass() + " )");

					} else if (arg instanceof List) {

						if (OUTPUT)
							_log.debug(out + "\tArg " + i + ", List, size: " + ((List) arg).size() + " ( " + arg.getClass() + " )");

					} else if (arg instanceof Collection) {

						if (OUTPUT)
							_log.debug(out + "\tArg " + i + ", Collection, size: " + ((Collection) arg).size() + " ( " + arg.getClass() + " )");

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
		int id = getMethodId(jp, false, false);

		Long val = _session.getProfile().get(id);

		if (val == null) {
			_session.getProfile().put(id, delta);
			// idToSignatureMap.put(id, jp.getSignature());
		} else
			_session.getProfile().put(id, val + delta);

	}

	public void writeToDisk() {
		if (ILonganConstants.OUTPUT_XML) {
			try {
				// String folder = "/Users/rtholmes/Documents/workspaces/workspace/longAn/tmp/";
				String folder = ILonganConstants.OUTPUT_PATH;
				String fName = folder + TimeUtility.getCurrentLSMRDateString() + ".xml";
				SessionXMLWriter sxmlw = new SessionXMLWriter();
				sxmlw.write(fName, _session);
			} catch (Exception e) {
				_log.error(e);
			}
		}
	}

	public void writeToScreen() {

		try {
			if (SUMMARY_OUTPUT) {
				_log.info("Writing Statistics");

				Vector<String> sortedNames = new Vector<String>(_session.getElementNames());
				Collections.sort(sortedNames);

				long total = 0;
				for (String name : sortedNames) {

					int elementId = _session.getIdForElement(name);

					MethodElement methodAgent = _session.getMethod(elementId);
					FieldElement fieldAgent = _session.getField(elementId);

					_log.info(name);

					if (methodAgent != null) {
						if (!methodAgent.getName().equals(name)) {
							// this should never happen
							_log.error("Method names don't match.");
						}

						Collection<Integer> uniqueCallers = getUniqueCallers(elementId);

						for (Integer caller : uniqueCallers) {

							MethodElement calledBy = _session.getMethod(caller);
							String calledByName = "";

							if (calledBy != null)
								calledByName = calledBy.getName();
							else
								calledByName = ILonganConstants.UNKNOWN_METHOD_NAME; // UNKNOWN_CALLER;

							int calledByCount = _session.getMethod(elementId).getCalledBy().count(caller);
							_log.info("\t<-- id: " + caller + "; # calls: " + calledByCount + "; name: " + calledByName);

							IObjectTracker[] paramTracker = new IObjectTracker[0];
							IObjectTracker returnTracker = null;

							if (methodAgent instanceof AJMethodAgent) {
								returnTracker = ((AJMethodAgent) methodAgent).getReturnTrackers().get(caller);
								paramTracker = ((AJMethodAgent) methodAgent).getParameterTrackers().get(caller);
							}

							if (paramTracker != null && paramTracker.length > 0) {
								for (IObjectTracker tracker : paramTracker) {
									_log.info("\t\tParam: " + tracker.getTrackerName() + " - [ idx: " + tracker.getPosition() + " ] name: " + tracker.getName() + " static type: "
											+ tracker.getStaticTypeName());
									_log.info("\t\t\t" + tracker.toString());
								}
							}

							if (returnTracker != null) {
								_log.info("\t\tReturn: " + returnTracker.getTrackerName() + " static type: " + returnTracker.getStaticTypeName());
								_log.info("\t\t\t" + returnTracker.toString());
							}
						}

					}
				}
				_log.info("Total time (with double counting): " + total);

				for (MethodElement mt : _session.getMethods()) {
					String methodName = mt.getName();

					_log.info("Time: " + _session.getProfile().get(mt.getId()) + " element: " + methodName);
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

	public void beforePreObjectInit(JoinPoint jp) {
		if (OUTPUT) {
			String out = "";

			for (int t = getCurrentCallstack().size(); t > 0; t--)
				out += "\t";

			out += "|-| Before obj preinit: " + jp.getSignature();

			_log.debug(out);
		}
	}

	public void afterObjectPreInit(JoinPoint jp) {
		if (OUTPUT) {
			String out = "";

			for (int t = getCurrentCallstack().size(); t > 0; t--)
				out += "\t";

			out += "|-| After obj preinit: " + jp.getSignature();

			_log.debug(out);
		}

	}

	public void setVerbose(boolean b) {
		OUTPUT = b;
	}
}

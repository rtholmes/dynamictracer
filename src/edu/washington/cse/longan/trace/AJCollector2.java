/**
 * Created on Apr 15, 2009
 * @author rtholmes
 */
package edu.washington.cse.longan.trace;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Stack;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;

import ca.lsmr.common.util.TimeUtility;
import ca.uwaterloo.cs.se.inconsistency.core.model2.ClassElement;

import com.google.common.base.Preconditions;

import edu.washington.cse.longan.Logger;
import edu.washington.cse.longan.io.SessionXMLWriter;
import edu.washington.cse.longan.model.ILonganConstants;
import edu.washington.cse.longan.model.MethodElement;
import edu.washington.cse.longan.model.Session;

/**
 * This is the main data collector used by the Aspect information collector. The _session field contains all of the collected data.
 * 
 * @author rtholmes
 * 
 */
public class AJCollector2 {
	private static AJCollector2 _instance = null;
	private static Logger _log = Logger.getLogger(AJCollector2.class);

	public static boolean OUTPUT = false;

	public static void clearInstance() {
		if (_instance != null)
			_log.warn("AJCollector cleared");

		_instance = null;
	}

	public static AJCollector2 getInstance() {
		if (_instance == null) {
			_instance = new AJCollector2();

		}
		return _instance;
	}

	private ThreadLocal<Stack<Integer>> _callStack = new ThreadLocal<Stack<Integer>>() {
		protected java.util.Stack<Integer> initialValue() {
			return new Stack<Integer>();
		};
	};

	private Stack<Integer> _exceptionStack = null;

	/**
	 * This has a _HUGE_ problem; ids are only unique PER CLASS, meaing an id of 0 will conflict with every single class. We can use pertypewithin on
	 * the aspect description but that will violate what we have happening here.
	 * 
	 * @param jps
	 * @return
	 */
	private int _masterCounter = 0;

	private Session _session;

	private AJCollector2() {
		try {
			// LOGGING
			// LSMRLogger.startLog4J(true, ILonganConstants.LOGGING_LEVEL);

			_session = new Session(TimeUtility.getCurrentLSMRDateString());
			_log.info("New AJCollector instantiated");
			// _log.info("Tracing started");

			Runtime.getRuntime().addShutdownHook(new Thread() {

				public void run() {
					_log.info("Shtudown is happening now");
					writeToDisk();
				}
			});

		} catch (Exception e) {
			_log.error(e);
		}
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
				_log.debug(out + "|-| After class init: " + jp.getSignature());
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

	public void afterObjectInit(JoinPoint jp) {

		methodExit(jp, null, false);

		if (OUTPUT) {
			String out = "";

			for (int t = getCurrentCallstack().size(); t > 0; t--)
				out += "\t";

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
				_log.debug(out + "|-| Before class init: " + jp.getSignature());
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

	public void beforePreObjectInit(JoinPoint jp) {
		if (OUTPUT) {
			String out = "";

			for (int t = getCurrentCallstack().size(); t > 0; t--)
				out += "\t";

			out += "|-| Before obj preinit: " + jp.getSignature();

			_log.debug(out);
		}
	}

	public synchronized void constructorEnter(JoinPoint jp, boolean isExternal) {

		int id = getMethodId(jp, isExternal, true);

		((AJMethodAgent) _session.getMethod(id)).methodEnter(jp, getCurrentCallstack());

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

	}

	public synchronized void constructorExit(JoinPoint jp, boolean isExternal) {

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

	@SuppressWarnings("unchecked")
	public void exceptionHandled(JoinPoint jp, Object instance, Object exception) {
		if (getCurrentCallstack().isEmpty()) {
			// RFE: handle the case where not everything is instrumented.
			_log.warn("Exception handled but the call stack is empty: " + jp.getSignature());
		} else {
			MethodElement mt = _session.getMethod(getCurrentCallstack().peek());
			Preconditions.checkArgument(exception instanceof Throwable);

			if (_exceptionStack == null) {
				// RFE: handle case where exception comes from somewhere we don't instrument
				_log.warn("Exception handled but the exception stack is empty: " + jp.getSignature());

				_exceptionStack = (Stack<Integer>) getCurrentCallstack().clone();
				mt.throwException(_exceptionStack, exception.getClass().getName(), ((Throwable) exception).getMessage());

			}

			mt.handleException(_exceptionStack, exception.getClass().getName(), ((Throwable) exception).getMessage());

			// this is a new exception
			_exceptionStack = null;

			if (OUTPUT) {
				_log.debug("handling current exception, exception stack cleared. " + mt.getName() + " ex type: " + exception.getClass().getName()
						+ " ex msg: " + ((Throwable) exception).getMessage());
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
			_log.warn("Exception thrown with an empty callstack");
		} else {
			MethodElement mt = _session.getMethod(getCurrentCallstack().peek());

			if (_exceptionStack == null) {
				// this is a new exception
				_exceptionStack = (Stack<Integer>) getCurrentCallstack().clone();
				mt.throwException(_exceptionStack, exception.getClass().getName(), ((Throwable) exception).getMessage());
				if (OUTPUT) {
					_log.debug("Rew exception encountered, new exception stack created. " + mt.getName() + " ex type: "
							+ exception.getClass().getName() + " ex msg: " + ((Throwable) exception).getMessage());
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

	private Stack<Integer> getCurrentCallstack() {
		return _callStack.get();
	}

	private Integer getMethodId(JoinPoint jp, boolean isExternal, boolean isExternalKnown) {

		String name = "";
		int id = -1;

		// this seems complicated but allows us to resolve what is actually going on.
		// e.g., Collections.add(Object) -> ArrayList.add(Object)
		name = AJMethodAgent.getMethodName(jp);

		if (!_session.hasIDForElement(name)) {
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
		}

		return id;
	}

	public synchronized void methodEnter(JoinPoint jp, boolean isExternal) {

		int id = getMethodId(jp, isExternal, true);

		((AJMethodAgent) _session.getMethod(id)).methodEnter(jp, getCurrentCallstack());

		if (OUTPUT) {
			String out = "";
			for (int i = getCurrentCallstack().size(); i > 0; i--)
				out += "\t";

			String sig = _session.getMethod(id).getName();

			if (!isExternal)
				_log.debug(out + "--> " + sig);
			else
				_log.debug(out + "-x> " + sig);

		}

		getCurrentCallstack().push(id);

	}

	public synchronized void methodExit(JoinPoint jp, Object retObject, boolean isExternal) {

		if (getCurrentCallstack().empty()) {
			// RFE: this is a bug, it shouldn't happen but it seems to be.
			_log.error("Trying to methodExit on an empty callstack. Exiting call: " + jp.getSignature());

			return;
		}

		getCurrentCallstack().pop();

		if (OUTPUT) {
			String out = "";
			for (int i = getCurrentCallstack().size(); i > 0; i--)
				out += "\t";

			Signature sig = jp.getSignature();

			if (!isExternal)
				out += "<-- " + sig;
			else
				out += "<x- " + sig;

			_log.debug(out);
		}
	}

	public void writeToDisk() {
		if (ILonganConstants.OUTPUT_XML) {
			try {
				String folder = ILonganConstants.OUTPUT_PATH;
				String fName = folder + TimeUtility.getCurrentLSMRDateString() + ".xml";
				SessionXMLWriter sxmlw = new SessionXMLWriter();
				sxmlw.write(fName, _session);
			} catch (Exception e) {
				_log.error(e);
			}
		}
	}

	private Hashtable<String, ClassElement> _classes = new Hashtable<String, ClassElement>();
	private Hashtable<JoinPoint.StaticPart, MethodElement> _methods = new Hashtable<JoinPoint.StaticPart, MethodElement>();

	private MethodElement getMethod(JoinPoint jp, boolean isExternal) {

		if (!_methods.containsKey(jp.getStaticPart())) {
			// this only happens the first time
			
			String methodName = AJMethodAgent.getMethodName(jp);
			ca.uwaterloo.cs.se.inconsistency.core.model2.MethodElement me = new ca.uwaterloo.cs.se.inconsistency.core.model2.MethodElement(methodName);

			String className = AJMethodAgent.getClassName(jp);
			if (!_classes.containsKey(className)) {
				ca.uwaterloo.cs.se.inconsistency.core.model2.ClassElement ce = new ca.uwaterloo.cs.se.inconsistency.core.model2.ClassElement(
						className, isExternal);
				_classes.put(className, ce);
			}
		}

		// check if method is in class

		// add it to class, if needed

		// map JoinPoint to class?

		// String className = AJMethodAgent.getClassName(jp);

		return _methods.get(jp.getStaticPart());
	}
}

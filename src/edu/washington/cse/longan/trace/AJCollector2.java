/**
 * Created on Apr 15, 2009
 * @author rtholmes
 */
package edu.washington.cse.longan.trace;

import java.util.Date;
import java.util.Hashtable;
import java.util.Stack;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.CatchClauseSignature;
import org.aspectj.lang.reflect.ConstructorSignature;
import org.aspectj.lang.reflect.InitializerSignature;
import org.aspectj.lang.reflect.MethodSignature;

import ca.lsmr.common.log.LSMRLogger;
import ca.lsmr.common.util.TimeUtility;
import ca.uwaterloo.cs.se.inconsistency.core.model2.ClassElement;
import ca.uwaterloo.cs.se.inconsistency.core.model2.MethodElement;
import ca.uwaterloo.cs.se.inconsistency.core.model2.Model;
import ca.uwaterloo.cs.se.inconsistency.core.model2.io.Model2XMLWriter;
import edu.washington.cse.longan.Logger;
import edu.washington.cse.longan.model.ILonganConstants;
import edu.washington.cse.longan.trace.fromAJ.StringMaker;

//import edu.washington.cse.longan.model.Session;

/**
 * This is the main data collector used by the Aspect information collector. The _session field contains all of the collected data.
 * 
 * @author rtholmes
 * 
 */
public class AJCollector2 {

	private static AJCollector2 _instance = null;

	private static Logger _log = Logger.getLogger(AJCollector2.class);

	/**
	 * Controls debug output; this can be very helpful for diagnosing tracer problems.
	 */
	public static boolean OUTPUT = false;

	public static final String XML_DESCRIPTION = "tracing system execution";

	public static final String XML_KIND = "dynamic";

	public static final String XML_ORIGIN = "dynamictracer_uw";

	static {
		LSMRLogger.startLog4J();
	}

	public static void clearInstance() {
		if (_instance != null)
			_log.warn("AJCollector cleared");

		_instance = null;
	}

	public static String getClassName(JoinPoint jp) {
		return jp.getSignature().getDeclaringTypeName();
	}

	public static AJCollector2 getInstance() {
		if (_instance == null) {
			_instance = new AJCollector2();

		}
		return _instance;
	}

	/**
	 * This code helps to translate from a method call like Collections.add(Object) to something more accurate like ArrayList.add(Object).
	 * 
	 * It is currently disabled.
	 * 
	 * @param jp
	 * @return
	 */
	private static String getMethodName(JoinPoint jp) {
		String name = jp.getSignature().toString();

		if (jp.getTarget() != null && jp.getTarget().getClass() != null) {
			// try to specialize the name (e.g., Class java.lang.Object.getClass() -> Class ca.uwaterloo.cs.se.bench.simple.NestedClass.getClass())
			name = specializeName(jp);
		}

		Signature signature = jp.getSignature();
		if (signature instanceof ConstructorSignature) {
			ConstructorSignature sig = (ConstructorSignature) signature;

			StringBuffer buf = new StringBuffer();
			StringMaker sm = StringMaker.longStringMaker;

			buf.append(sm.makePrimaryTypeName(sig.getDeclaringType(), sig.getDeclaringTypeName()));
			buf.append(".");
			buf.append(sig.getName());
			sm.addSignature(buf, sig.getParameterTypes());

			name = buf.toString();
		} else if (signature instanceof MethodSignature) {
			MethodSignature sig = (MethodSignature) signature;

			StringBuffer buf = new StringBuffer();

			StringMaker sm = StringMaker.longStringMaker;
			buf.append(sm.makePrimaryTypeName(sig.getDeclaringType(), sig.getDeclaringTypeName()));
			buf.append(".");
			buf.append(sig.getName());
			sm.addSignature(buf, sig.getParameterTypes());

			name = buf.toString();
		} else if (signature instanceof InitializerSignature) {
			InitializerSignature sig = (InitializerSignature) signature;

			StringBuffer buf = new StringBuffer();
			StringMaker sm = StringMaker.longStringMaker;
			buf.append(sm.makePrimaryTypeName(sig.getDeclaringType(), sig.getDeclaringTypeName()));
			buf.append(".");
			buf.append(sig.getName());
			name = buf.toString();
		} else if (signature instanceof CatchClauseSignature) {
			CatchClauseSignature sig = (CatchClauseSignature) signature;

			StringBuffer buf = new StringBuffer();

			StringMaker sm = StringMaker.longStringMaker;
			buf.append(sm.makePrimaryTypeName(sig.getDeclaringType(), sig.getDeclaringTypeName()));
			buf.append(".");
			buf.append(sig.getName());
			buf.append(sm.makeTypeName(sig.getParameterType()));

			try {
				throw new RuntimeException();
			} catch (Exception e) {
				e.printStackTrace();
			}

			System.out.println("CatchClauseSignature - evaluates to: " + name);
		} else {
			String msg = "AJCollector2::getMethodName(..) - Unknown signature type: " + signature.getClass() + " ( " + signature + " )";
			_log.warn(msg);
			throw new RuntimeException(msg);
		}

		return name;
	}

	private static String specializeName(JoinPoint jp) {
		String name = jp.getSignature().toString();

		Object myThis = jp.getThis();
		Object myTarget = jp.getTarget();

		if (myThis != myTarget) {

			String rootName = jp.getSignature().getDeclaringTypeName();
			String targetTypeName = jp.getTarget().getClass().getName();
			if (!rootName.equals(targetTypeName)) {
				int offsetIndex = 0;
				boolean isConstructor = false;

				if (jp.getSignature() instanceof ConstructorSignature)
					isConstructor = true;

				if (name.indexOf(" ") < name.indexOf("(")) {
					offsetIndex = name.indexOf(" ");
					isConstructor = false;
				}

				String retType = name.substring(0, offsetIndex);

				int padding = 2;
				if (isConstructor)
					padding = 1;

				String methodName = name.substring(rootName.length() + retType.length() + padding);
				name = retType + " " + targetTypeName + "." + methodName;
				// _log.debug("Specialize name from: " + oldName + " -> " + name);
			}
		}
		return name;
	}

	private ThreadLocal<Stack<MethodElement>> _callStack = new ThreadLocal<Stack<MethodElement>>() {
		protected java.util.Stack<MethodElement> initialValue() {
			return new Stack<MethodElement>();
		};
	};

	private Hashtable<Signature, MethodElement> _methods = new Hashtable<Signature, MethodElement>();

	Model _model = new Model();

	private AJCollector2() {
		try {
			_log.trace("New AJCollector instantiated");

			Runtime.getRuntime().addShutdownHook(new Thread() {

				public void run() {
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
			methodExit(jp, false);

			if (OUTPUT) {
				String out = "";
				for (int i = getCurrentCallstack().size(); i > 0; i--)
					out += "\t";
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
	public void afterCreateException(JoinPoint jp) {

		// NOTE: if exception tracking seems screwed up, check to see if this is
		// the culprit; I don't really like this solution at all, but for some reason
		// the exceptionThrown method isn't being triggered by exceptions arising
		// from constructors.

		// Needed to capture call stack for exceptions arising in constructors
		constructorExit(jp, true);

		if (OUTPUT) {
			String out = "";
			for (int i = getCurrentCallstack().size(); i > 0; i--)
				out += "\t";
			_log.debug(out + "After create exception: " + jp.getSignature().toString());
		}
	}

	public void afterObjectInit(JoinPoint jp) {

		methodExit(jp, false);

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
		methodEnter(jp, isExternal);
	}

	public synchronized void constructorExit(JoinPoint jp, boolean isExternal) {
		methodExit(jp, isExternal);
	}

	@SuppressWarnings("rawtypes")
	private void createTypeHierarchy(ClassElement subClass, Class parentClass) {
		if (parentClass != null) {
			String parentClassName = parentClass.getName();
			ClassElement parentClassElement;

			if (!_model.hasClass(parentClassName)) {

				// NOTE: what is c.isLocalClass?
				ClassElement ce = new ClassElement(parentClassName, true, parentClass.isInterface(), false);
				_model.addElement(ce);

				// superclass
				createTypeHierarchy(ce, parentClass.getSuperclass());

				// interfaces
				for (Class sc : parentClass.getInterfaces()) {
					createTypeHierarchy(ce, sc);
				}
			}

			parentClassElement = _model.getClass(parentClassName);

			// everything seems to subclass Class so just ignore these (uninteresting) relationships
			if (!subClass.getId().equals("java.lang.Class") && !parentClassName.equals("java.lang.Object")) {
				subClass.getParents().add(parentClassElement);
			}

			// NOTE: we can extract annotations here too, if we wanted to
		}
	}

	@SuppressWarnings("rawtypes")
	private ClassElement createTypeHierarchy(JoinPoint jp, boolean isExternal) {

		String className = AJMethodAgent.getClassName(jp);

		if (!_model.hasClass(className)) {
			ClassElement ce = new ClassElement(className, isExternal);
			_model.addElement(ce);

			Class ceClass = jp.getSignature().getDeclaringType();
			for (Class c : ceClass.getInterfaces()) {
				createTypeHierarchy(ce, c);
			}

			createTypeHierarchy(ce, ceClass.getSuperclass());
		}

		return _model.getClass(className);
	}

	public void exceptionHandled(JoinPoint jp, Object instance, Object exception) {
		// This block is mainly for debugging
		// Ultimately the after methodExit() calls are handling the unwinding of the stack

		if (getCurrentCallstack().isEmpty()) {
			// RFE: handle the case where not everything is instrumented.
			_log.warn("Exception handled but the call stack is empty: " + jp.getSignature());
		}

		if (OUTPUT) {
			_log.debug("handling current exception, exception stack cleared. " + jp.getSignature() + " ex type: " + exception.getClass().getName()
					+ " ex msg: " + ((Throwable) exception).getMessage());
		}

		if (OUTPUT) {
			String out = "";
			for (int i = getCurrentCallstack().size(); i > 0; i--)
				out += "\t";

			_log.debug(out + "|-| Exception handled: " + exception + " in: " + jp.getSignature());
		}

	}

	public void exceptionThrown(JoinPoint jp, Throwable exception, boolean isExternal) {

		// This block is mainly for debugging
		// Ultimately the after methodExit() calls are handling the unwinding of the stack

		if (getCurrentCallstack().isEmpty()) {
			// RFE: handle the case where not everything is instrumented.
			_log.warn("Exception thrown with an empty callstack");
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

	private Stack<MethodElement> getCurrentCallstack() {
		return _callStack.get();
	}

	private MethodElement getMethod(JoinPoint jp, boolean isExternal) {

		if (!_methods.containsKey(jp.getSignature())) {
			// this only happens the first time

			String methodName = getMethodName(jp);
			MethodElement me = new MethodElement(methodName);

			if (!_model.hasMethod(methodName)) {
				_model.addElement(me);
			}

			_methods.put(jp.getSignature(), me);

			ClassElement ce = createTypeHierarchy(jp, isExternal);

			// check if method is in class
			if (!ce.getMethods().contains(me)) {
				// add it to class, if needed
				ce.getMethods().add(me);
			}

		}

		return _methods.get(jp.getSignature());
	}

	public synchronized void methodEnter(JoinPoint jp, boolean isExternal) {

		MethodElement me = getMethod(jp, isExternal);

		if (me == null) {
			System.err.println("Null method element: " + jp.getSignature());
		}
		if (!getCurrentCallstack().empty()) {
			// NOTE: this could be replaced with a single local variable _lastPush to make this a simle:
			// if (_lastPush != null) prev.getCalls().add(_lastPush);
			MethodElement prev = getCurrentCallstack().peek();
			prev.getCalls().add(me);
		} else {
			// _log.warn("Empty call stack; target: " + me);
		}

		if (OUTPUT) {
			String out = "";
			for (int i = getCurrentCallstack().size(); i > 0; i--)
				out += "\t";

			// Signature sig = jp.getSignature();

			if (!isExternal)
				out += "--> " + me;
			else
				out += "-x-> " + me;

			_log.debug(out);
		}

		getCurrentCallstack().push(me);

	}

	public synchronized void methodExit(JoinPoint jp, boolean isExternal) {

		if (getCurrentCallstack().empty()) {
			// RFE: this is a bug, it shouldn't happen but it seems to be.
			_log.error("Trying to methodExit on an empty callstack. Exiting call: " + jp.getSignature());

			return;
		}

		getCurrentCallstack().pop();

		// REMOVE THIS
		// make this a bit better...
		// pop until the popped element is the current method
		// MethodElement mexit = getMethod(jp, isExternal);
		// System.out.println("Exiting - looking for: " + mexit);
		// while (true) {
		// MethodElement popMe = getCurrentCallstack().pop();
		// System.out.println("\tExiting - considering: " + popMe + " match? " + mexit.equals(popMe));
		// if (mexit.equals(popMe)) {
		// break;
		// } else {
		// System.out.println("Popping extra method: " + popMe + " loking for: " + mexit);
		// }
		// }

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
				// SessionXMLWriter sxmlw = new SessionXMLWriter();
				// sxmlw.write(fName, _session);

				Model2XMLWriter mxmlw = new Model2XMLWriter(fName);
				mxmlw.write(_model, "dynamictracer_uw", "dynamic", "tracing system execution", new Date());

				// String latestFName = ILonganConstants.OUTPUT_PATH + "dynamic_latest.xml";
				//
				// try {
				// // Create channel on the source
				// FileChannel srcChannel = new FileInputStream(fName).getChannel();
				//
				// // Create channel on the destination
				// FileChannel dstChannel = new FileOutputStream(latestFName).getChannel();
				//
				// // Copy file contents from source to destination
				// dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
				//
				// // Close the channels
				// srcChannel.close();
				// dstChannel.close();
				// System.out.println("Model file copied to: " + new File(latestFName).getAbsolutePath());
				// } catch (IOException ioe) {
				// System.err.println(ioe);
				// }

			} catch (Exception e) {
				_log.error("Error writing to disk: " + e);
			}
		}
	}
}

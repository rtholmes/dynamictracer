/**
 * Created on Apr 15, 2009
 * @author rtholmes
 */
package edu.washington.cse.longan;

import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
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
	public static final boolean OUTPUT = false;

	private static Collector _instance = null;

	private Logger _log = Logger.getLogger(this.getClass());

	private Hashtable<Integer, MethodTracker> _methods = new Hashtable<Integer, MethodTracker>();

	// point from an index -> the 'base' id that will be used
	Integer[] _ids = new Integer[1024];

	Hashtable<Integer, Long> _profile = new Hashtable<Integer, Long>();

	Stack<Integer> _callStack = new Stack<Integer>();

	Stack<Long> _timeStack = new Stack<Long>();

	//	Hashtable<Integer, Signature> idToSignatureMap = new Hashtable<Integer, Signature>();

	Hashtable<String, Integer> _nameToBaseIdMap = new Hashtable<String, Integer>();

	private Collector() {
		try {
			LSMRLogger.startLog4J(true, Level.INFO);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Collector getInstance() {
		if (_instance == null)
			_instance = new Collector();
		return _instance;
	}

	public void classInit(JoinPoint jp) {
		try {
			if (OUTPUT)
				System.out.println("Class init: " + jp.getStaticPart().getSourceLocation().getWithinType());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void constructorEnter(JoinPoint jp) {

		JoinPoint.StaticPart jps = jp.getStaticPart();

		_callStack.push(getMethodId(jps));
		_timeStack.push(System.currentTimeMillis());

		for (int t = _callStack.size(); t > 0; t--)
			if (OUTPUT)
				System.out.print("\t");

		if (OUTPUT) {
			Signature sig = jp.getSignature();
			System.out.println("|->| " + sig);
		}

	}

	public void constructorExit(JoinPoint jp) {
		long delta = System.currentTimeMillis() - _timeStack.pop();

		record(jp, delta);

		_callStack.pop();
	}

	public void methodEnter(JoinPoint jp) {

		int id = getMethodId(jp.getStaticPart());

		_methods.get(id).methodEnter(jp, _callStack);

		if (OUTPUT) {
			String outString = "";
			for (int i = _callStack.size(); i > 0; i--)
				outString += "\t";

			String sig = _methods.get(id).getName();

			_log.debug("->" + sig + " # args: " + jp.getArgs().length);

			printArgs(jp);
		}

		_callStack.push(id);

		_timeStack.push(System.currentTimeMillis());
	}

	public void methodExit(JoinPoint jp) {
		long delta = System.currentTimeMillis() - _timeStack.pop();

		record(jp, delta);
		// record(jp, jp.getStaticPart(), end - start);

		_callStack.pop();

		for (int i = _callStack.size(); i > 0; i--)
			if (OUTPUT)
				System.out.print("\t");

		if (OUTPUT) {
			Signature sig = jp.getSignature();
			System.out.println("<-" + sig + " time: " + delta);
			// System.out.println("<-" + sig + " time: " + (end - start));
		}

	}

	public void objectInit(JoinPoint jp) {
		if (OUTPUT)
			System.out.println("Obj init: " + jp.getTarget().getClass().getName());
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

	private Integer getMethodId(StaticPart jps) {
		int id = jps.getId();

		if (_ids[id] != null) {
			// id has already been mapped to the base id so return it
			return _ids[id];
		} else {
			String name = jps.getSignature().getName();

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

				for (int t = _callStack.size(); t > 0; t--)
					if (OUTPUT)
						System.out.print("\t");

				if (arg != null) {
					if (arg instanceof String) {

						if (OUTPUT)
							System.out.println("\tString length: " + ((String) arg).length());

					} else if (arg.getClass().isArray()) {

						if (OUTPUT)
							System.out.println("\tArray length: " + Array.getLength(arg) + " class: "
									+ arg.getClass().getComponentType());

					} else if (arg instanceof Collection) {

						if (OUTPUT)
							System.out.println("\tCollection size: " + ((Collection) arg).size());

					} else {

						// RFE: test this with subclasses (e.g., if arg is a
						// Vector but is declared as a List)

						if (OUTPUT)
							System.out.println("\tArg type: " + arg.getClass().getName());

					}
				} else {
					if (OUTPUT)
						System.out.println("\tNull arg, type: " + argType.getName());
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
			//			idToSignatureMap.put(id, jp.getSignature());
		} else
			_profile.put(id, val + delta);

	}

}

/**
 * Created on Apr 15, 2009
 * @author rtholmes
 */
package edu.washington.cse.longan;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
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
	private Logger _log = Logger.getLogger(this.getClass());

	Hashtable<Integer, Signature> idToSignatureMap = new Hashtable<Integer, Signature>();

	Stack<Integer> _stack = new Stack<Integer>();
	Stack<Long> _times = new Stack<Long>();

	Hashtable<Integer, Long> _profile = new Hashtable<Integer, Long>();

	private Hashtable<Integer, MethodTracker> _methods = new Hashtable<Integer, MethodTracker>();

	public static final boolean OUTPUT = false;

	private Collector() {
		try {
			LSMRLogger.startLog4J(true, Level.INFO);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static Collector _instance = null;

	public static Collector getInstance() {
		if (_instance == null)
			_instance = new Collector();
		return _instance;
	}

	public void methodEnter(JoinPoint jp) {

		int id = getId(jp.getStaticPart());

		if (!_methods.containsKey(id))
			_methods.put(id, new MethodTracker(id));

		_methods.get(id).methodEnter(jp, _stack);

		for (int i = _stack.size(); i > 0; i--)
			if (OUTPUT)
				System.out.print("\t");

		if (OUTPUT) {
			Signature sig = jp.getSignature();
			System.out.println("->" + sig + " # args: " + jp.getArgs().length);
		}

		_stack.push(id);
		printArgs(jp);

		_times.push(System.currentTimeMillis());
	}

	public void methodExit(JoinPoint jp) {
		long delta = System.currentTimeMillis() - _times.pop();

		record(jp, delta);
		// record(jp, jp.getStaticPart(), end - start);

		_stack.pop();

		for (int i = _stack.size(); i > 0; i--)
			if (OUTPUT)
				System.out.print("\t");

		if (OUTPUT) {
			Signature sig = jp.getSignature();
			System.out.println("<-" + sig + " time: " + delta);
			// System.out.println("<-" + sig + " time: " + (end - start));
		}

	}

	public void constructorEnter(JoinPoint jp) {

		JoinPoint.StaticPart jps = jp.getStaticPart();

		_stack.push(getId(jps));
		_times.push(System.currentTimeMillis());

		for (int t = _stack.size(); t > 0; t--)
			if (OUTPUT)
				System.out.print("\t");

		if (OUTPUT) {
			Signature sig = jp.getSignature();
			System.out.println("|->| " + sig);
		}

	}

	private Integer getId(StaticPart jps) {
		int id = jps.getId();
		if (_ids[id] != null) {
			return _ids[id];
		} else {
			String name = jps.getSignature().getName();

			Integer baseId = nameToBaseIdMap.get(name);
			if (baseId == null) {
				// there is no base id yet; use this one
				nameToBaseIdMap.put(name, id);
				_ids[id] = id;
				baseId = id;
			} else {
				// there is a base id, choose it instead and
				// add a new mapping for this one
				_ids[id] = baseId;
			}
			id = baseId;
		} 
		return id;
	}

	public void constructorExit(JoinPoint jp) {
		long delta = System.currentTimeMillis() - _times.pop();

		record(jp, delta);

		_stack.pop();
	}

	public void objectInit(JoinPoint jp) {
		if (OUTPUT)
			System.out.println("Obj init: "
					+ jp.getTarget().getClass().getName());
	}

	public void classInit(JoinPoint jp) {
		try {
			if (OUTPUT)
				System.out.println("Class init: "
						+ jp.getStaticPart().getSourceLocation()
								.getWithinType());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// point from an index -> the 'base' id that will be used
	Integer[] _ids = new Integer[1024];

	Hashtable<String, Integer> nameToBaseIdMap = new Hashtable<String, Integer>();

	private void record(JoinPoint jp, long delta) {
		int id = getId(jp.getStaticPart());

		Long val = _profile.get(id);

		if (val == null) {
			_profile.put(id, delta);
			idToSignatureMap.put(id, jp.getSignature());
		} else
			_profile.put(id, val + delta);

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

				for (int t = _stack.size(); t > 0; t--)
					if (OUTPUT)
						System.out.print("\t");

				if (arg != null) {
					if (arg instanceof String) {

						if (OUTPUT)
							System.out.println("\tString length: "
									+ ((String) arg).length());

					} else if (arg.getClass().isArray()) {

						if (OUTPUT)
							System.out.println("\tArray length: "
									+ Array.getLength(arg) + " class: "
									+ arg.getClass().getComponentType());

					} else if (arg instanceof Collection) {

						if (OUTPUT)
							System.out.println("\tCollection size: "
									+ ((Collection) arg).size());

					} else {

						// RFE: test this with subclasses (e.g., if arg is a
						// Vector but is declared as a List)

						if (OUTPUT)
							System.out.println("\tArg type: "
									+ arg.getClass().getName());

					}
				} else {
					if (OUTPUT)
						System.out.println("\tNull arg, type: "
								+ argType.getName());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// }
	}

	public void writeToScreen() {
		try {
			if (true) {
				_log.info("after last test case");

				long total = 0;
				for (Integer i : idToSignatureMap.keySet()) {
					long time = _profile.get(i);
					total += time;
					_log.info(i + "\t " + time + " \t-> "
							+ idToSignatureMap.get(i));
				}
				_log.info("Total time (with double counting): " + total);

				for (MethodTracker mt : _methods.values()) {
					Signature sig = idToSignatureMap.get(mt.getId());
					if (sig != null)
						_log.info(sig + " called by:");
					else
						_log.error("unknown id: " + mt.getId());

					Vector<String> calledBys = new Vector<String>();
					for (Integer i : mt.getCalledBy()) {
						Signature s2 = idToSignatureMap.get(i);
						if (s2 != null) {
							String n = s2.toLongString();
							n += " id: " + i + " -> " + _ids[i];
							if (!calledBys.contains(n)) {
								calledBys.add(n);
							} else {
								_log.error("multiple copies of? " + n);
							}

						} else
							calledBys.add(i + "");
					}

					Collections.sort(calledBys);
					for (String s : calledBys)
						_log.info("\t" + s);

				}
			}
		} catch (Exception e) {
			e.fillInStackTrace();
			_log.error(e);
		}
	}

}

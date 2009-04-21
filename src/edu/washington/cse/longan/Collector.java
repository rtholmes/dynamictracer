/**
 * Created on Apr 15, 2009
 * @author rtholmes
 */
package edu.washington.cse.longan;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Stack;

import java.lang.reflect.Array;

import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.CodeSignature;

public class Collector {
	private Logger _log = Logger.getLogger(this.getClass());

	Hashtable<Integer, Signature> idToSignatureMap = new Hashtable<Integer, Signature>();

	Stack<Integer> _stack = new Stack<Integer>();
	Stack<Long> _times = new Stack<Long>();

	Hashtable<Integer, Long> _profile = new Hashtable<Integer, Long>();

	public static final boolean OUTPUT = false;

	private Collector() {

	}

	private static Collector _instance = null;

	public static Collector getInstance() {
		if (_instance == null)
			_instance = new Collector();
		return _instance;
	}

	public void methodEnter(JoinPoint jp) {

		for (int i = _stack.size(); i > 0; i--)
			if (OUTPUT)
				System.out.print("\t");

		if (OUTPUT) {
			Signature sig = jp.getSignature();
			System.out.println("->" + sig + " # args: " + jp.getArgs().length);
		}

		_stack.push(jp.getStaticPart().getId());
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

		_stack.push(jps.getId());
_times.push(System.currentTimeMillis());

		for (int t = _stack.size(); t > 0; t--)
			if (OUTPUT)
				System.out.print("\t");

		if (OUTPUT) {
			Signature sig = jp.getSignature();
			System.out.println("|->| " + sig);
		}

	}

	public void constructorExit(JoinPoint jp) {
		_stack.pop();
		_times.pop();
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

	private void record(JoinPoint jp, long delta) {
		int id = jp.getStaticPart().getId();

		Long val = _profile.get(id);

		if (val == null) {
			_profile.put(id, delta);
			idToSignatureMap.put(id, jp.getSignature());
		} else
			_profile.put(id, val + delta);

	}

	@SuppressWarnings("unchecked")
	private void printArgs(JoinPoint jp) {
//		if (false) {
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
//		}
	}

	public void writeToScreen() {
		if (OUTPUT)
			System.out.println("after last test case");

		long total = 0;
		for (Integer i : idToSignatureMap.keySet()) {
			long time = _profile.get(i);
			total += time;
			System.out.println(i + "\t " + time + " \t-> "
					+ idToSignatureMap.get(i));
		}
		System.out.println("Total time (with double counting): " + total);
	}

}

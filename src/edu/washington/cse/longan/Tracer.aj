package edu.washington.cse.longan;
/**
 * Created on Mar 16, 2009
 * @author rtholmes
 */

import java.util.Collection;
import java.util.Hashtable;
import java.util.Stack;

import java.lang.reflect.Array;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.CodeSignature;

privileged aspect Tracer {

	Hashtable<Integer, Signature> idToSignatureMap = new Hashtable<Integer, Signature>();

	public static final boolean OUTPUT = false;

	pointcut withinTests() : within(junit.framework.TestCase+);

	pointcut methodEntry() : execution(public * org.joda.time..*.* (..));

	pointcut methodEntryNoTests() : methodEntry() && ! withinTests();

	pointcut constructor() :  call(org.joda.time.*.new(..));

	pointcut objectInitialization() : initialization(Xorg.joda.time.*.new(..));

	pointcut classInitialization() : staticinitialization(Xorg.joda.time.*);

	// /**
	// * This is more of a hack just so we know when we're done
	// */
	 pointcut lastTestCase() : execution(public *
			 org.joda.time.TestIllegalFieldValueException.testOtherConstructors());
	//
	Stack<Signature> globalStack = new Stack<Signature>();

	Object around() : methodEntryNoTests() 
	{
		long start = System.currentTimeMillis();
		JoinPoint jp = thisJoinPoint;
		Signature sig = jp.getSignature();

		for (int i = globalStack.size(); i > 0; i--)
			if (OUTPUT)
				System.out.print("\t");

		if (OUTPUT)
			System.out.println("->" + sig + " # args: " + jp.getArgs().length);

		globalStack.push(sig);
		printArgs(jp);

		try {
			return proceed();
		} finally {
			long end = System.currentTimeMillis();

			record(thisJoinPoint, thisJoinPointStaticPart, end - start);

			globalStack.pop();
			
			 for (int i = globalStack.size(); i > 0; i--)
			 if (OUTPUT)
			 System.out.print("\t");
			
			 if (OUTPUT)
			 System.out.println("<-" + sig + " time: " + (end - start));
		}

	}

	@SuppressWarnings("unchecked")
	private void printArgs(JoinPoint jp) {
		if (false) {
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

					for (int t = globalStack.size(); t > 0; t--)
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
		}
	}

	Hashtable<Integer, Long> _profile = new Hashtable<Integer, Long>();

	private void record(JoinPoint jp, JoinPoint.StaticPart jps, long delta) {
		int id = jps.getId();

		Long val = _profile.get(id);

		if (val == null) {
			_profile.put(id, delta);
			idToSignatureMap.put(id, jp.getSignature());
		} else
			_profile.put(id, val + delta);

	}

	void around() : lastTestCase() {
		try {
			proceed();
		} finally {
			if (OUTPUT)
				System.out.println("after last test case");

			long total = 0;
			for (Integer i : idToSignatureMap.keySet()) {
				long time = _profile.get(i);
				total += time;
				System.out.println(i + "\t "+time+" \t-> " + idToSignatureMap.get(i));
			}
			System.out.println("Total time (with double counting): "+total);
		}
	}

	before() : constructor() {
		JoinPoint.StaticPart jp = thisJoinPointStaticPart;

		Signature sig = jp.getSignature();
		globalStack.push(sig);

		for (int t = globalStack.size(); t > 0; t--)
			if (OUTPUT)
				System.out.print("\t");

		if (OUTPUT)
			System.out.println("|->| " + sig);
	}

	after() : constructor() {

		globalStack.pop();

	}

	before() : objectInitialization() {
		JoinPoint jp = thisJoinPoint;

		if (OUTPUT)
			System.out.println("Obj init: "
					+ jp.getTarget().getClass().getName());
	}

	before() : classInitialization() {
		JoinPoint jp = thisJoinPoint;
		try {
			if (OUTPUT)
				System.out.println("Class init: "
						+ jp.getStaticPart().getSourceLocation()
								.getWithinType());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//
	// pointcut methodCall() : call(* Foo.*(..)); // doesn't include
	// constructors
	//
	// pointcut constructorCall() : call(Foo.new(..));
	//
	// pointcut methodExecution() : execution(* Foo.*(..)); // again, no ctors
	//
	// pointcut constructorExecution() : execution(Foo.new(..));
	//
	// pointcut fieldGet() : get(* Foo.*);
	//
	// pointcut fieldSet() : set(* Foo.*);
	//
	// pointcut objectInitialization() : initialization(Foo.new(..));
	//
	// pointcut classInitialization() : staticinitialization(Foo);
	//
	// pointcut exceptionHandler() : handler(Foo);
	//
	// before() : methodCall() {
	// System.out.println("A method call to Foo is about to occur");
	// }
	//
	// before() : constructorCall() {
	// System.out.println("A constructor call to Foo is about to occur");
	// }
	//
	// before() : methodExecution() {
	// System.out.println("A method execution in Foo is about to occur");
	// }
	//
	// before() : constructorExecution() {
	// System.out.println("A constructor execution in Foo is about to occur");
	// }
	//
	// before() : fieldGet() {
	// System.out.println("Someone is about to get a field from Foo");
	// }
	//
	// before() : fieldSet() {
	// System.out.println("Someone is about to set a field in Foo");
	// }
	//
	// before() : objectInitialization() {
	// System.out.println("Foo is about to undergo instance initialization");
	// }
	//
	// before() : classInitialization() {
	// System.out.println("Foo is about to undergo class initialization");
	// }
	//
	// before() : exceptionHandler() {
	// System.out.println("A exception of type Foo is about to be handled");
	// }
	//
	// after() : methodCall() {
	// System.out.println("A method call to Foo just occurred");
	// }
	//
	// after() : constructorCall() {
	// System.out.println("A constructor call to Foo just occurred");
	// }
	//
	// after() : methodExecution() {
	// System.out.println("A method execution in Foo just occurred");
	// }
	//
	// after() : constructorExecution() {
	// System.out.println("A constructor execution in Foo just occurred");
	// }
	//
	// after() : fieldGet() {
	// System.out.println("Someone just got a field from Foo");
	// }
	//
	// after() : fieldSet() {
	// System.out.println("Someone just set a field in Foo");
	// }
	//
	// after() : objectInitialization() {
	// System.out.println("Foo has just undergone instance initialization");
	// }
	//
	// after() : classInitialization() {
	// System.out.println("Foo has just undergone class initialization");
	// }
	//
	// after() : exceptionHandler() {
	// System.out.println("A exception of type Foo has just been handled");
	// }
}

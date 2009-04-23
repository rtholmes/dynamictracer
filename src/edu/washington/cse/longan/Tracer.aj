package edu.washington.cse.longan;

/**
 * Created on Mar 16, 2009
 * @author rtholmes
 */

import org.aspectj.lang.JoinPoint;

privileged aspect Tracer {

	//	Hashtable<Integer, Signaturex> idToSignatureMap = new Hashtable<Integer, Signature>();

	//	public static final boolean OUTPUT = false;

	Collector _collector = Collector.getInstance();

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


//	Object around() : methodEntryNoTests()
	Object around() : methodEntry()
	{
		JoinPoint jp = thisJoinPoint;
		_collector.methodEnter(jp);

		try {

			return proceed();
			
		} finally {

			_collector.methodExit(jp);
			
		}

	}

	void around() : lastTestCase() {

		try {
			
			proceed();
			
		} finally {
			
			_collector.writeToScreen();
			
		}
	}

	before() : constructor() {

		_collector.constructorEnter(thisJoinPoint);
		
	}

	after() : constructor() {
		
		_collector.constructorExit(thisJoinPoint);

	}

	before() : objectInitialization() {

		JoinPoint jp = thisJoinPoint;

		_collector.objectInit(jp);
		
	}

	before() : classInitialization() {
		
		JoinPoint jp = thisJoinPoint;

		_collector.classInit(jp);
		
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

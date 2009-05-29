package edu.washington.cse.longan;

/**
 * Created on Mar 16, 2009
 * @author rtholmes
 */

import org.aspectj.lang.JoinPoint;

import edu.washington.cse.longan.Collector;




           class  Tracer {

	Collector _collector = Collector.getInstance();

	// pointcut withinTests() : within(junit.framework.TestCase+);

	// pointcut methodEntry() : execution(public * org.joda.time..*.* (..));
	// RT pointcut methodEntry() : execution(* edu.washington.cse..*.* (..));

	// works
	// RT pointcut constructor() :
	// call(edu.washington.cse.longanTest.Inheritance.new(..));
	// //call(edu.washington.cse..*.new(..));

	// works
	// pointcut objectInitialization() :
	// initialization(edu.washington.cse.longanTest.Inheritance.new(..));
	// matches but doesn't work for some reason
	// RT pointcut objectInitialization() :
	// initialization(edu.washington.cse.*.new(..));

	// org.joda.time.*
	// RT pointcut classInitialization() :
	// staticinitialization(edu.washington.cse.*);

	// works in jodatime
	// pointcut constructor() : call(org.joda.time..*.new(..));
	// pointcut objectInitialization() :
	// initialization(org.joda.time.*.new(..));

	pointcut methodEntry()                                            ;

	pointcut constructor()                                       ;

	pointcut objectInitialization()                                               ;

	pointcut classInitialization()                                             ;

	// capture library calls and initializations
	pointcut libEntry()                   ; // || call (*.new(..));

	// captures calls but not instantiations
	pointcut libraryEntry()                                ; // && ! methodEntry();

	pointcut libraryConstructor()                                                          ;

	// XXX: field set seems to work but field get doesn't
	
	pointcut fieldSet()                                ;
	
	before()              {
		_collector.fieldSet(thisJoinPoint);
	}
	
	before()                  {
		_collector.methodEnter(thisJoinPoint, true);
	}

//	after() : libraryEntry() {
	after(             Object o)                 {
		
		_collector.methodExit(thisJoinPoint,o, true);
	}

	// Object around() : methodEntryNoTests()
	Object around()                  {
		JoinPoint jp = thisJoinPoint;
		_collector.methodEnter(jp, false);

		Object retObject = null;
		try {

			retObject = proceed();
			
			return retObject;
			
		} finally {

			_collector.methodExit(jp, retObject, false);

		}

	}

	before()                 {

		_collector.constructorEnter(thisJoinPoint, false);

	}

	after()                 {

		_collector.constructorExit(thisJoinPoint, false);

	}

	before()                        {
		// XXX: external
		_collector.constructorEnter(thisJoinPoint, true);

	}

	after()                        {
		// XXX: external
		_collector.constructorExit(thisJoinPoint, true);

	}

	before()                          {

		JoinPoint jp = thisJoinPoint;

		_collector.objectInit(jp);

	}

	before()                         {

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

Tracer x1;}

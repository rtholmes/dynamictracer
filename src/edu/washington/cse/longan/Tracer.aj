package edu.washington.cse.longan;

/**
 * Created on Mar 16, 2009
 * @author rtholmes
 */

import org.aspectj.lang.JoinPoint;

import edu.washington.cse.longan.Collector;

           class  Tracer {

	Collector _collector = Collector.getInstance();

	// all scoped method calls (super can't be captured)
	// pointcut methodEntry() : execution(* edu.washington.cse..*.* (..));
	pointcut methodEntry()                                                                             ;

	// all scoped constructors
	// pointcut constructor() : call(edu.washington.cse..*.new(..));
	pointcut constructor()                                                                       ;

	// all scoped object initializers [not sure how to use these yet]
	// pointcut objectInitialization() :
	// initialization(edu.washington.cse..*.new(..)) && !within(Tracer);
	pointcut objectInitialization()                                                                          ;

	// all scoped class initializers [not sure how to use these yet]
	// pointcut classInitialization() :
	// staticinitialization(edu.washington.cse..*.*) && !within(Tracer);
	pointcut classInitialization()                                                                          ;

	// all library method calls (e.g., all non-scoped calls)
	// 2nd clause loses us static calls to instrumenter suite for some reason,
	// but we don't want these anyways.
	// pointcut libraryEntry() : call(* *.* (..)) && ! call(*
	// edu.washington.cse..*.* (..)) && !within(Tracer);
	pointcut libraryEntry()                                                                                                                                                                                                                 ;

	// captures calls but not instantiations (the above entry used to be
	// libEntry)
	// pointcut libraryEntry() : libEntry() && !within(Tracer);

	// all non-scoped constructor calls
	pointcut libraryConstructor()                                                                                         ;

	// all field accesses
	// Note: Cannot capture references to static final fields (they are inlined)
	pointcut fieldGet()                                ;

	// all field sets
	// Cannot capture initializations of static final fields (they are inlined)
	pointcut fieldSet()                                ;

	pointcut exceptionHandler(Object instance)                               ;

	pointcut throwableCreation()                                     ;

	pointcut lastThing()                                                                                                ;
	
	after()               {
//		System.err.println("adsf");
//		_collector.point();
		_collector.writeToScreen();
	}
	
	// //////////////////////////
	// //////// ADVICE //////////
	// //////////////////////////
	before(Object instance, Object exception)                                                 {
		_collector.exceptionHandled(thisJoinPoint, instance, exception);
	}

	before()              {
		_collector.fieldGet(thisJoinPoint);
	}

	before(Object newValue)                                {
		_collector.fieldSet(thisJoinPoint, newValue);
	}

	// before() : libraryEntry() {
	// _collector.methodEnter(thisJoinPoint, true);
	// }
	//
	// after() returning (Object o): libraryEntry() {
	//
	// _collector.methodExit(thisJoinPoint, o, true);
	// }

	// TODO: should we be using around or before/after?
	Object around()                   {
		JoinPoint jp = thisJoinPoint;
		_collector.methodEnter(jp, true);

		Object retObject = null;
		try {

			retObject = proceed();

			return retObject;

		} finally {

			_collector.methodExit(jp, retObject, true);

		}

	}

	after(            Throwable e)                 {
		_collector.exceptionThrown(thisJoinPoint, e, true);
	}

	after(            Throwable e)                {
		_collector.exceptionThrown(thisJoinPoint, e, false);
	}

	before()                       {
		_collector.beforeCreateException(thisJoinPoint);
	}

	after()                       {
		_collector.afterCreateException(thisJoinPoint);
	}

	// before() : methodEntry() {
	// _collector.methodEnter(thisJoinPoint, false);
	// }
	//	
	// after() returning (Object returnObject):methodEntry() {
	// _collector.methodExit(thisJoinPoint,returnObject, false);
	// }

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

		_collector.beforeObjectInit(jp);

	}

	after()                          {

		JoinPoint jp = thisJoinPoint;

		_collector.afterObjectInit(jp);

	}

	before()                         {

		// RFE: can we check to see what classes / interfaces are extended /
		// implemented here?

		JoinPoint jp = thisJoinPoint;

		_collector.beforeClassInit(jp);

	}

	after()                         {

		// RFE: can we check to see what classes / interfaces are extended /
		// implemented here?

		JoinPoint jp = thisJoinPoint;

		_collector.afterClassInit(jp);

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

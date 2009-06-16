package edu.washington.cse.longan.trace;
//package edu.washington.cse.longan;
//
///**
// * Created on Mar 16, 2009
// * @author rtholmes
// */
//
//import org.aspectj.lang.JoinPoint;
//
//import edu.washington.cse.longan.Collector;
//
//privileged aspect Tracer {
//
//	AJCollector _collector = AJCollector.getInstance();
//
//	// all scoped method calls (super can't be captured)
//	// pointcut methodEntry() : execution(* edu.washington.cse..*.* (..));
//	pointcut methodEntry() : execution(* org.ulti.dev.dynamic.test..*.* (..)) &&
//		!throwableCreation();
//
//	// all scoped constructors
//	// pointcut constructor() : call(edu.washington.cse..*.new(..));
//	pointcut constructor() : call(org.ulti.dev.dynamic.test..*.new(..)) &&
//		!throwableCreation();
//
//	// all scoped object initializers [not sure how to use these yet]
//	// pointcut objectInitialization() :
//	// initialization(edu.washington.cse..*.new(..)) && !within(Tracer);
//	pointcut objectInitialization() : initialization(org.ulti.dev.dynamic.test..*.new(..)) && !within(Tracer);
//
//	// all scoped class initializers [not sure how to use these yet]
//	// pointcut classInitialization() :
//	// staticinitialization(edu.washington.cse..*.*) && !within(Tracer);
//	pointcut classInitialization() : staticinitialization(org.ulti.dev.dynamic.test..*.*) && !within(Tracer);
//
//	// all library method calls (e.g., all non-scoped calls)
//	// 2nd clause loses us static calls to instrumenter suite for some reason,
//	// but we don't want these anyways.
//	// pointcut libraryEntry() : call(* *.* (..)) && ! call(*
//	// edu.washington.cse..*.* (..)) && !within(Tracer);
//	pointcut libraryEntry() : call(* *.* (..)) &&
//		!call(* org.ulti.dev.dynamic.test..*.* (..)) && // ignore regular methods
//		!call(* edu.washington.cse..*.* (..)) && 		// ignore tracer code
//		!throwableCreation() &&
//		!within(Tracer);
//
//	// captures calls but not instantiations (the above entry used to be
//	// libEntry)
//	// pointcut libraryEntry() : libEntry() && !within(Tracer);
//
//	// all non-scoped constructor calls
//	pointcut libraryConstructor() : call(*..*.new(..)) && 
//		!constructor() &&
//		!throwableCreation() &&
//		!within(Tracer);
//
//	// all field accesses
//	// Note: Cannot capture references to static final fields (they are inlined)
//	pointcut fieldGet() : get(* *.*) && !within(Tracer);
//
//	// all field sets
//	// Cannot capture initializations of static final fields (they are inlined)
//	pointcut fieldSet() : set(* *.*) && !within(Tracer);
//
//	pointcut exceptionHandler(Object instance) : handler(*) && this(instance);
//
//	pointcut throwableCreation() : call(java.lang.Exception+.new(..));
//
//	pointcut lastThing() : execution(void org.ulti.dev.dynamic.test.test.InheritanceTest.lastThing()) && !within(Tracer);
//	
//	after() : lastThing() {
////		System.err.println("adsf");
////		_collector.point();
//		_collector.writeToScreen();
//	}
//	
//	// //////////////////////////
//	// //////// ADVICE //////////
//	// //////////////////////////
//	before(Object instance, Object exception) : exceptionHandler(instance) && args(exception) {
//		_collector.exceptionHandled(thisJoinPoint, instance, exception);
//	}
//
//	before() : fieldGet() {
//		_collector.fieldGet(thisJoinPoint);
//	}
//
//	before(Object newValue) : fieldSet() && args(newValue) {
//		_collector.fieldSet(thisJoinPoint, newValue);
//	}
//
//	// before() : libraryEntry() {
//	// _collector.methodEnter(thisJoinPoint, true);
//	// }
//	//
//	// after() returning (Object o): libraryEntry() {
//	//
//	// _collector.methodExit(thisJoinPoint, o, true);
//	// }
//
//	// TODO: should we be using around or before/after?
//	Object around() : libraryEntry()
//	{
//		JoinPoint jp = thisJoinPoint;
//		_collector.methodEnter(jp, true);
//
//		Object retObject = null;
//		try {
//
//			retObject = proceed();
//
//			return retObject;
//
//		} finally {
//
//			_collector.methodExit(jp, retObject, true);
//
//		}
//
//	}
//
//	after() throwing (Throwable e): libraryEntry() {
//		_collector.exceptionThrown(thisJoinPoint, e, true);
//	}
//
//	after() throwing (Throwable e): methodEntry() {
//		_collector.exceptionThrown(thisJoinPoint, e, false);
//	}
//
//	before() : throwableCreation() {
//		_collector.beforeCreateException(thisJoinPoint);
//	}
//
//	after() : throwableCreation() {
//		_collector.afterCreateException(thisJoinPoint);
//	}
//
//	// before() : methodEntry() {
//	// _collector.methodEnter(thisJoinPoint, false);
//	// }
//	//	
//	// after() returning (Object returnObject):methodEntry() {
//	// _collector.methodExit(thisJoinPoint,returnObject, false);
//	// }
//
//	// Object around() : methodEntryNoTests()
//	Object around() : methodEntry()
//	{
//		JoinPoint jp = thisJoinPoint;
//		_collector.methodEnter(jp, false);
//
//		Object retObject = null;
//		try {
//
//			retObject = proceed();
//
//			return retObject;
//
//		} finally {
//
//			_collector.methodExit(jp, retObject, false);
//
//		}
//
//	}
//
//	before() : constructor() {
//
//		_collector.constructorEnter(thisJoinPoint, false);
//
//	}
//
//	after() : constructor() {
//
//		_collector.constructorExit(thisJoinPoint, false);
//
//	}
//
//	before() : libraryConstructor() {
//		// XXX: external
//		_collector.constructorEnter(thisJoinPoint, true);
//
//	}
//
//	after() : libraryConstructor() {
//		// XXX: external
//		_collector.constructorExit(thisJoinPoint, true);
//
//	}
//
//	before() : objectInitialization() {
//
//		JoinPoint jp = thisJoinPoint;
//
//		_collector.beforeObjectInit(jp);
//
//	}
//
//	after() : objectInitialization() {
//
//		JoinPoint jp = thisJoinPoint;
//
//		_collector.afterObjectInit(jp);
//
//	}
//
//	before() : classInitialization() {
//
//		// RFE: can we check to see what classes / interfaces are extended /
//		// implemented here?
//
//		JoinPoint jp = thisJoinPoint;
//
//		_collector.beforeClassInit(jp);
//
//	}
//
//	after() : classInitialization() {
//
//		// RFE: can we check to see what classes / interfaces are extended /
//		// implemented here?
//
//		JoinPoint jp = thisJoinPoint;
//
//		_collector.afterClassInit(jp);
//
//	}
//
//	//
//	// pointcut methodCall() : call(* Foo.*(..)); // doesn't include
//	// constructors
//	//
//	// pointcut constructorCall() : call(Foo.new(..));
//	//
//	// pointcut methodExecution() : execution(* Foo.*(..)); // again, no ctors
//	//
//	// pointcut constructorExecution() : execution(Foo.new(..));
//	//
//	// pointcut fieldGet() : get(* Foo.*);
//	//
//	// pointcut fieldSet() : set(* Foo.*);
//	//
//	// pointcut objectInitialization() : initialization(Foo.new(..));
//	//
//	// pointcut classInitialization() : staticinitialization(Foo);
//	//
//	// pointcut exceptionHandler() : handler(Foo);
//	//
//	// before() : methodCall() {
//	// System.out.println("A method call to Foo is about to occur");
//	// }
//	//
//	// before() : constructorCall() {
//	// System.out.println("A constructor call to Foo is about to occur");
//	// }
//	//
//	// before() : methodExecution() {
//	// System.out.println("A method execution in Foo is about to occur");
//	// }
//	//
//	// before() : constructorExecution() {
//	// System.out.println("A constructor execution in Foo is about to occur");
//	// }
//	//
//	// before() : fieldGet() {
//	// System.out.println("Someone is about to get a field from Foo");
//	// }
//	//
//	// before() : fieldSet() {
//	// System.out.println("Someone is about to set a field in Foo");
//	// }
//	//
//	// before() : objectInitialization() {
//	// System.out.println("Foo is about to undergo instance initialization");
//	// }
//	//
//	// before() : classInitialization() {
//	// System.out.println("Foo is about to undergo class initialization");
//	// }
//	//
//	// before() : exceptionHandler() {
//	// System.out.println("A exception of type Foo is about to be handled");
//	// }
//	//
//	// after() : methodCall() {
//	// System.out.println("A method call to Foo just occurred");
//	// }
//	//
//	// after() : constructorCall() {
//	// System.out.println("A constructor call to Foo just occurred");
//	// }
//	//
//	// after() : methodExecution() {
//	// System.out.println("A method execution in Foo just occurred");
//	// }
//	//
//	// after() : constructorExecution() {
//	// System.out.println("A constructor execution in Foo just occurred");
//	// }
//	//
//	// after() : fieldGet() {
//	// System.out.println("Someone just got a field from Foo");
//	// }
//	//
//	// after() : fieldSet() {
//	// System.out.println("Someone just set a field in Foo");
//	// }
//	//
//	// after() : objectInitialization() {
//	// System.out.println("Foo has just undergone instance initialization");
//	// }
//	//
//	// after() : classInitialization() {
//	// System.out.println("Foo has just undergone class initialization");
//	// }
//	//
//	// after() : exceptionHandler() {
//	// System.out.println("A exception of type Foo has just been handled");
//	// }
//
//}

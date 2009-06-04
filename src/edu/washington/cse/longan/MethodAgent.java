/**

 * Created on Apr 22, 2009
 * @author rtholmes
 */
package edu.washington.cse.longan;

import java.util.Hashtable;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.ConstructorSignature;
import org.aspectj.lang.reflect.MethodSignature;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import edu.washington.cse.longan.tracker.IObjectTracker;
import edu.washington.cse.longan.tracker.ObjectTrackerFactory;

public class MethodAgent {
	private int _id;

	private Logger _log = Logger.getLogger(this.getClass());

	private String _name;

	private static final String VOID_RETURN = "VOID";

	private static final int UNKNOWN_CALLER_ID = -1;

	private IObjectTracker[] _parameterTrackerDefinitions;

	private IObjectTracker _returnTrackerDefinition;

	Multiset<Integer> _calledBy = HashMultiset.create();

	/**
	 * Track parameter attributes per caller; these can be aggregated later, if required.
	 * 
	 * key: caller id value: array of trackers for each parameter of the method
	 */
	Hashtable<Integer, IObjectTracker[]> _parameterTrackers = new Hashtable<Integer, IObjectTracker[]>();

	boolean _hasVoidReturn = false;
	/**
	 * Track return value attributes per caller.
	 * 
	 * key: caller id value: tracker for the return value
	 */
	private Hashtable<Integer, IObjectTracker> _returnObjectTrackers = new Hashtable<Integer, IObjectTracker>();

	public MethodAgent(int id, JoinPoint jp) {

		_id = id;

		_name = jp.getSignature().toString();

		prepareTrackers(jp);

	}

	@SuppressWarnings("unchecked")
	private void prepareTrackers(JoinPoint jp) {

		Signature sig = jp.getSignature();

		if (sig instanceof MethodSignature) {
			MethodSignature methodSig = (MethodSignature) sig;

			Class returnType = methodSig.getReturnType();

			if (returnType.getName().equals("void")) {
				// null return types require no analysis at runtime
				_hasVoidReturn = true;
			} else {
				_returnTrackerDefinition = ObjectTrackerFactory.create(returnType);
			}

			Class[] paramTypes = methodSig.getParameterTypes();
			String[] paramNames = methodSig.getParameterNames();

			_parameterTrackerDefinitions = new IObjectTracker[paramTypes.length];

			for (int i = 0; i < paramTypes.length; i++) {
				_parameterTrackerDefinitions[i] = ObjectTrackerFactory.create(paramTypes[i], i, paramNames[i]);
			}

		} else if (sig instanceof ConstructorSignature) {

			ConstructorSignature constructorSig = (ConstructorSignature) sig;

			Class[] paramTypes = constructorSig.getParameterTypes();
			String[] paramNames = constructorSig.getParameterNames();

			_parameterTrackerDefinitions = new IObjectTracker[paramTypes.length];

			for (int i = 0; i < paramTypes.length; i++) {
				_parameterTrackerDefinitions[i] = ObjectTrackerFactory.create(paramTypes[i], i, paramNames[i]);
			}

		} else {
			_log.error("Signature associated with: " + jp.getSignature() + " is of type: "
					+ jp.getSignature().getClass());
		}

	}

	public Multiset<Integer> getCalledBy() {
		return _calledBy;
	}

	public Integer getId() {
		return _id;
	}

	public String getName() {
		return _name;
	}

	public void methodEnter(JoinPoint jp, Stack<Integer> callStack) {
		updateCallers(callStack);
		updateArguments(jp, callStack);
	}

	public void methodExit(JoinPoint jp, Object returnObject, Stack<Integer> callStack) {

		if (!_hasVoidReturn) {
			int caller = -1;
			if (!callStack.isEmpty())
				caller = callStack.peek();
			try {
				if (!_returnObjectTrackers.contains(caller))
					_returnObjectTrackers.put(caller, _returnTrackerDefinition.clone());
			} catch (CloneNotSupportedException cnse) {
				_log.error(cnse);
			}

			IObjectTracker tracker = _returnObjectTrackers.get(caller);

			tracker.track(returnObject);
		}

	}

	private void updateArguments(JoinPoint jp, Stack<Integer> callStack) {

		Object args[] = jp.getArgs();
		if (args.length == _parameterTrackerDefinitions.length) {

			int caller = -1;
			if (!callStack.isEmpty())
				caller = callStack.peek();

			if (!_parameterTrackers.containsKey(caller)) {

				// This is a mess, but calling _paramTraDef.clone() doesn't seem to do a deep copy
				try {
					IObjectTracker[] pTrackers = new IObjectTracker[_parameterTrackerDefinitions.length];
					for (int i = 0; i < _parameterTrackerDefinitions.length; i++) {
						IObjectTracker ot = _parameterTrackerDefinitions[i];

						pTrackers[i] = ot.clone();
					}
					_parameterTrackers.put(caller, pTrackers);
					
				} catch (CloneNotSupportedException e) {
					_log.error(e);
					e.printStackTrace();
				}
				
			}

			IObjectTracker[] paramaterTrackers = _parameterTrackers.get(caller);

			for (int i = 0; i < args.length; i++) {
				paramaterTrackers[i].track(args[i]);
			}

		} else {
			_log.error("Different # of params in method compared to initial parse ( " + _name + " ).");
		}

	}

	private void updateCallers(Stack<Integer> callStack) {

		if (!callStack.isEmpty()) {

			_calledBy.add(callStack.peek());

		} else {
			// unknown caller, could be from non-instrumented code (e.g., junit core

			_log.trace("Unknown caller for: " + _name);

			_calledBy.add(UNKNOWN_CALLER_ID);

		}

	}

	Hashtable<Integer, IObjectTracker[]> getParameterTrackers() {
		return _parameterTrackers;
	}

	Hashtable<Integer, IObjectTracker> getReturnTrackers() {
		return _returnObjectTrackers;
	}

	@Override
	public String toString() {

		return getName();
	}
}

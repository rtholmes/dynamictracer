/**
 * Created on Apr 22, 2009
 * @author rtholmes
 */
package edu.washington.cse.longan;

import java.util.Stack;

import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public class MethodTracker {
	private int _id;

	private Logger _log = Logger.getLogger(this.getClass());

	private String _name;

	private static final String VOID_RETURN = "VOID";

	private static final int UNKNOWN_CALLER = -1;

	private ObjectTracker returnObjectTracker = null;

	private ObjectTracker[] parameterTrackers;

	// HashSet<Integer> _calledBy = new HashSet<Integer>();

	Multiset<Integer> _calledBy = HashMultiset.create();

	public MethodTracker(int id, JoinPoint jp) {

		_id = id;

		_name = jp.getSignature().toString();

		prepareTrackers(jp);

		// RFE: initial parameter analysis

	}

	@SuppressWarnings("unchecked")
	private void prepareTrackers(JoinPoint jp) {
		// TODO Auto-generated method stub

		Signature sig = jp.getSignature();

		if (sig instanceof MethodSignature) {
			MethodSignature methodSig = (MethodSignature) sig;

			Class returnType = methodSig.getReturnType();

			if (returnType.getName().equals("void")) {
				// null return types require no analysis at runtime
				returnObjectTracker = null;
			} else {
				returnObjectTracker = ObjectTrackerFactory.create(returnType);
			}

			Class[] paramTypes = methodSig.getParameterTypes();

			parameterTrackers = new ObjectTracker[paramTypes.length];

			for (int i = 0; i < paramTypes.length; i++) {
				parameterTrackers[i] = ObjectTrackerFactory.create(paramTypes[i]);
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
		updateArguments(jp);
	}

	public void methodExit(JoinPoint jp, Object returnObject) {
		if (returnObjectTracker != null)
			returnObjectTracker.track(returnObject);
	}

	private void updateArguments(JoinPoint jp) {

		Object args[] = jp.getArgs();
		if (args.length == parameterTrackers.length) {

			for (int i = 0; i < args.length; i++) {
				parameterTrackers[i].track(args[i]);
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

			_calledBy.add(UNKNOWN_CALLER);

		}

	}
}

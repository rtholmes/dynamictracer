/**
 * Created on Apr 22, 2009
 * @author rtholmes
 */
package edu.washington.cse.longan;

import java.util.HashSet;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;

public class MethodTracker {
	private Logger _log = Logger.getLogger(this.getClass());

	HashSet<Integer> _calledBy = new HashSet<Integer>();

	private int _id;

	public MethodTracker(int id) {
//		_id = jp.getStaticPart().getId();
_id = id;
		// RFE: initial parameter analysis

	}

	public HashSet<Integer> getCalledBy() {
		return _calledBy;
	}

	public void methodEnter(JoinPoint jp, Stack<Integer> callStack) {
		if (!callStack.isEmpty()) {

			int caller = callStack.peek();

			_calledBy.add(caller);

		} else {
			// unknown caller, could be from non-instrumented code (e.g., junit
			// core)
		}
		// RFE: RT param analysis

	}

	public Integer getId() {
		return _id;
	}
}

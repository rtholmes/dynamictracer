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
	private int _id;

	private Logger _log = Logger.getLogger(this.getClass());

	private String _name;

	HashSet<Integer> _calledBy = new HashSet<Integer>();

	public MethodTracker(int id, String name) {

		_id = id;

		_name = name;

		// RFE: initial parameter analysis

	}

	public HashSet<Integer> getCalledBy() {
		return _calledBy;
	}

	public Integer getId() {
		return _id;
	}

	public String getName() {
		return _name;
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
}

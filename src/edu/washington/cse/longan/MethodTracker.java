/**
 * Created on Apr 22, 2009
 * @author rtholmes
 */
package edu.washington.cse.longan;

import java.util.HashSet;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public class MethodTracker {
	private int _id;

	private Logger _log = Logger.getLogger(this.getClass());

	private String _name;

//	HashSet<Integer> _calledBy = new HashSet<Integer>();

	Multiset<Integer> _calledBy = HashMultiset.create();
	
	public MethodTracker(int id, String name) {

		_id = id;

		_name = name;

		// RFE: initial parameter analysis

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
		if (!callStack.isEmpty()) {

			int caller = callStack.peek();

			_calledBy.add(caller);

		} else {
			// unknown caller, could be from non-instrumented code (e.g., junit
			// core)
			_log.trace("Unknown caller for: "+jp.getSignature());
			
			//RFE: track unknown callers (-1?)
		}

		// RFE: RT param analysis

	}
}

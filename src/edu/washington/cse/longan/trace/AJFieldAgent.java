package edu.washington.cse.longan.trace;

import java.util.Hashtable;
import java.util.Stack;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.FieldSignature;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multiset;

import edu.washington.cse.longan.Logger;
import edu.washington.cse.longan.model.FieldElement;
import edu.washington.cse.longan.model.FieldTraitContainer;
import edu.washington.cse.longan.model.ILonganConstants;
import edu.washington.cse.longan.trace.tracker.IObjectTracker;
import edu.washington.cse.longan.trace.tracker.ObjectTrackerFactory;
import edu.washington.cse.longan.trait.ITrait;

public class AJFieldAgent extends FieldElement {

	private Logger _log = Logger.getLogger(this.getClass());

	protected IObjectTracker _fieldTrackerDefinitions;

	/**
	 * Track parameter attributes per getter; these can be aggregated later, if required.
	 * 
	 * key: caller id value: array of trackers for the field
	 */
	Hashtable<Integer, IObjectTracker> _fieldGetTrackers = new Hashtable<Integer, IObjectTracker>();

	/**
	 * Track parameter attributes per setter; these can be aggregated later, if required.
	 * 
	 * key: caller id value: array of trackers for the field
	 */
	Hashtable<Integer, IObjectTracker> _fieldSetTrackers = new Hashtable<Integer, IObjectTracker>();

	public AJFieldAgent(int id, JoinPoint jp) {
		super(id, jp.getSignature().toString());

		prepareTrackers(jp);
	}

	@SuppressWarnings("unchecked")
	private void prepareTrackers(JoinPoint jp) {

		Signature sig = jp.getSignature();

		if (sig instanceof FieldSignature) {
			FieldSignature fieldSig = (FieldSignature) sig;

			Class fieldType = fieldSig.getFieldType();
			String fieldName = fieldSig.getName();
			// String declaringTypeName = fieldSig.getDeclaringTypeName();

			if (fieldType.getName().equals(ILonganConstants.VOID_RETURN)) {
				Preconditions.checkArgument(false, ILonganConstants.NOT_POSSIBLE);
			} else {
				_fieldTrackerDefinitions = ObjectTrackerFactory.create(fieldType);
			}

		} else {
			_log.error("Signature associated with: " + jp.getSignature() + " is of type: " + jp.getSignature().getClass());
		}

	}

	public void fieldGet(JoinPoint jp, Stack<Integer> callStack, Object fieldValue) {
		fieldGetSet(jp, callStack, fieldValue, _getBy, _fieldGetTrackers);
	}

	public void fieldSet(JoinPoint jp, Stack<Integer> callStack, Object newValue) {
		fieldGetSet(jp, callStack, newValue, _setBy, _fieldSetTrackers);
	}

	// this is really more confusing than it needs to be to support get and set
	public void fieldGetSet(JoinPoint jp, Stack<Integer> callStack, Object fieldValue, Multiset<Integer> byMultiset,
			Hashtable<Integer, IObjectTracker> trackerTable) {

		if (!callStack.isEmpty()) {
			byMultiset.add(callStack.peek());
		} else {
			// unknown caller, could be from non-instrumented code (e.g., junit core
			_log.trace("Unknown caller for: " + getName());
			byMultiset.add(ILonganConstants.UNKNOWN_METHOD_ID);
		}

		int caller = -1;
		if (!callStack.isEmpty())
			caller = callStack.peek();
		try {

			if (!trackerTable.containsKey(caller)) {
				IObjectTracker tracker = _fieldTrackerDefinitions.clone();
				trackerTable.put(caller, tracker);

				// _log.debug("putting new rtc in for caller: " + caller + " for method: " + getName());

				// this may seem unnecessary in the AJ tracker (and it is really)
				// but it keeps things consistent with the parent types
				// which is what we're really after anyways for the analysis
				FieldTraitContainer ftc = null;

				if (byMultiset == _getBy)
					ftc = getFieldGetTraitContainer();
				else if (byMultiset == _setBy)
					ftc = getFieldSetTraitContainer();
				else
					_log.error("unknown multiset kind");

				ITrait[] traits = new ITrait[0];
				traits = tracker.getTraits().toArray(traits);
				if (ftc == null) {
					ftc = new FieldTraitContainer(tracker.getStaticTypeName());

					if (byMultiset == _getBy)
						setFieldGetTraitContainer(ftc);
					else if (byMultiset == _setBy)
						setFieldSetTraitContainer(ftc);
					else
						_log.error("unknown multiset kind");

				}
				ftc.addTraits(caller, traits);

			}

		} catch (CloneNotSupportedException cnse) {
			_log.error(cnse);
		}

		IObjectTracker tracker = trackerTable.get(caller);

		tracker.track(fieldValue);

	}

}

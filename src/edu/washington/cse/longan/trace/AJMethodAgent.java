/**

 * Created on Apr 22, 2009
 * @author rtholmes
 */
package edu.washington.cse.longan.trace;

import java.util.Hashtable;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.ConstructorSignature;
import org.aspectj.lang.reflect.MethodSignature;

import com.google.common.collect.Multiset;

import edu.washington.cse.longan.model.ILonganConstants;
import edu.washington.cse.longan.model.MethodElement;
import edu.washington.cse.longan.model.ParamTraitContainer;
import edu.washington.cse.longan.model.ReturnTraitContainer;
import edu.washington.cse.longan.trace.tracker.IObjectTracker;
import edu.washington.cse.longan.trace.tracker.ObjectTrackerFactory;
import edu.washington.cse.longan.trait.ITrait;

//RFE: refactor out all aspectJ references
public class AJMethodAgent extends MethodElement {

	private Logger _log = Logger.getLogger(this.getClass());

	protected IObjectTracker[] _parameterTrackerDefinitions;

	protected IObjectTracker _returnTrackerDefinition;

	/**
	 * Track parameter attributes per caller; these can be aggregated later, if required.
	 * 
	 * key: caller id value: array of trackers for each parameter of the method
	 */
	Hashtable<Integer, IObjectTracker[]> _parameterTrackers = new Hashtable<Integer, IObjectTracker[]>();

	/**
	 * Track return value attributes per caller.
	 * 
	 * key: caller id value: tracker for the return value
	 */
	private Hashtable<Integer, IObjectTracker> _returnObjectTrackers = new Hashtable<Integer, IObjectTracker>();

	public AJMethodAgent(int id, JoinPoint jp) {
		super(id, jp.getSignature().toString());

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
			_isConstructor = true;

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
				if (!_returnObjectTrackers.contains(caller)){
					IObjectTracker tracker = _returnTrackerDefinition.clone();
					_returnObjectTrackers.put(caller, tracker);
					
					// this may seem unnecessary in the AJ tracker (and it is really)
					// but it keeps things consistent with the parent types
					// which is what we're really after anyways for the analysis
					ITrait[] traits = new ITrait[0];
					traits = tracker.getTraits().toArray(traits);
					ReturnTraitContainer ptc = new ReturnTraitContainer(tracker.getStaticTypeName());
					ptc.addTraits(caller, traits);
				}
				
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

						// this may seem unnecessary in the AJ tracker (and it is really)
						// but it keeps things consistent with the parent types
						// which is what we're really after anyways for the analysis
						ITrait[] traits = new ITrait[0];
						traits = pTrackers[i].getTraits().toArray(traits);
						ParamTraitContainer ptc = new ParamTraitContainer(ot.getName(), ot.getStaticTypeName(), ot
								.getPosition());
						ptc.addTraits(caller, traits);
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

			_calledBy.add(ILonganConstants.UNKNOWN_CALLER_ID);

		}

	}

	public Hashtable<Integer, IObjectTracker[]> getParameterTrackers() {
		return _parameterTrackers;
	}

	public Hashtable<Integer, IObjectTracker> getReturnTrackers() {
		return _returnObjectTrackers;
	}

	public IObjectTracker[] getParameterTrackerDefinitions() {
		return _parameterTrackerDefinitions;
	}

	public IObjectTracker getReturnTrackerDefinition() {
		return _returnTrackerDefinition;
	}

	@Override
	public String toString() {

		return getName();
	}

	public boolean hasVoidReturn() {
		return _hasVoidReturn;
	}
}

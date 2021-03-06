/**

 * Created on Apr 22, 2009
 * @author rtholmes
 */
package edu.washington.cse.longan.trace;

import java.util.Hashtable;
import java.util.Stack;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.ConstructorSignature;
import org.aspectj.lang.reflect.InitializerSignature;
import org.aspectj.lang.reflect.MethodSignature;

import edu.washington.cse.longan.Logger;
import edu.washington.cse.longan.model.ILonganConstants;
import edu.washington.cse.longan.model.MethodElement;
import edu.washington.cse.longan.model.ParamTraitContainer;
import edu.washington.cse.longan.model.ReturnTraitContainer;
import edu.washington.cse.longan.trace.fromAJ.StringMaker;
import edu.washington.cse.longan.trace.tracker.IObjectTracker;
import edu.washington.cse.longan.trace.tracker.ObjectTrackerFactory;
import edu.washington.cse.longan.trait.ITrait;

//RFE: refactor out all aspectJ references
public class AJMethodAgent extends MethodElement {

	private static Logger _log = Logger.getLogger(AJMethodAgent.class);

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

	public AJMethodAgent(int id, JoinPoint jp, boolean isExternal) {
		super(id, getMethodName(jp), isExternal);

		if (!ILonganConstants.CALLSTACK_ONLY)
			prepareTrackers(jp);
	}

	@SuppressWarnings("unchecked")
	private void prepareTrackers(JoinPoint jp) {

		Signature sig = jp.getSignature();

		if (sig instanceof MethodSignature) {
			MethodSignature methodSig = (MethodSignature) sig;

			Class returnType = methodSig.getReturnType();

			if (returnType.getName().equals(ILonganConstants.VOID_RETURN)) {
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

		} else if (sig instanceof InitializerSignature) {
			InitializerSignature initSig = (InitializerSignature) sig;

			_parameterTrackerDefinitions = new IObjectTracker[0];

		} else {
			_log.error("Signature associated with: " + jp.getSignature() + " is of type: " + jp.getSignature().getClass());
		}

	}

	public void methodEnter(JoinPoint jp, Stack<Integer> callStack) {
		updateCallers(callStack);

		if (!ILonganConstants.CALLSTACK_ONLY)
			updateArguments(jp, callStack);
	}

	public void methodExit(JoinPoint jp, Object returnObject, Stack<Integer> callStack) {

		if (!_hasVoidReturn) {
			int caller = -1;
			if (!callStack.isEmpty())
				caller = callStack.peek();
			try {

				if (!_returnObjectTrackers.containsKey(caller)) {
					IObjectTracker tracker = _returnTrackerDefinition.clone();
					_returnObjectTrackers.put(caller, tracker);

					// _log.debug("putting new rtc in for caller: " + caller + " for method: " + getName());

					// this may seem unnecessary in the AJ tracker (and it is really)
					// but it keeps things consistent with the parent types
					// which is what we're really after anyways for the analysis
					ReturnTraitContainer rtc = getReturnTraitContainer();
					ITrait[] traits = new ITrait[0];
					traits = tracker.getTraits().toArray(traits);
					if (rtc == null) {
						rtc = new ReturnTraitContainer(tracker.getStaticTypeName());
						setReturnTraitContainer(rtc);
					}
					rtc.addTraits(caller, traits);

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
						_parameterTrackers.put(caller, pTrackers);

						// this may seem unnecessary in the AJ tracker (and it is really)
						// but it keeps things consistent with the parent types
						// which is what we're really after anyways for the analysis
						ParamTraitContainer ptc = getParamTraitContainer(i);
						ITrait[] traits = new ITrait[0];
						traits = pTrackers[i].getTraits().toArray(traits);
						if (ptc == null) {
							ptc = new ParamTraitContainer(ot.getName(), ot.getStaticTypeName(), ot.getPosition());
							addParamTraitContainer(ptc, i);
						}
						ptc.addTraits(caller, traits);

					}

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

			if (ILonganConstants.OUTPUT) {
				_log.trace("Unknown caller for: " + _name);
			}

			_calledBy.add(ILonganConstants.UNKNOWN_METHOD_ID);

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

	/**
	 * This code helps to translate from a method call like Collections.add(Object) to something more accurate like ArrayList.add(Object).
	 * 
	 * It is currently disabled.
	 * 
	 * @param jp
	 * @return
	 */
	public static String getMethodName(JoinPoint jp) {
		String name = jp.getSignature().toString();

		if (jp.getTarget() != null && jp.getTarget().getClass() != null) {
			// try to specialize the name (e.g., Class java.lang.Object.getClass() -> Class ca.uwaterloo.cs.se.bench.simple.NestedClass.getClass())
			name = specializeName(jp);
		}

		Signature signature = jp.getSignature();
		if (signature instanceof ConstructorSignature) {
			ConstructorSignature sig = (ConstructorSignature) signature;

			StringBuffer buf = new StringBuffer();
			StringMaker sm = StringMaker.longStringMaker;

			buf.append(sm.makePrimaryTypeName(sig.getDeclaringType(), sig.getDeclaringTypeName()));
			buf.append(".");
			buf.append(sig.getName());
			sm.addSignature(buf, sig.getParameterTypes());

			name = buf.toString();
		} else if (signature instanceof MethodSignature) {
			MethodSignature sig = (MethodSignature) signature;

			StringBuffer buf = new StringBuffer();

			StringMaker sm = StringMaker.longStringMaker;
			buf.append(sm.makePrimaryTypeName(sig.getDeclaringType(), sig.getDeclaringTypeName()));
			buf.append(".");
			buf.append(sig.getName());
			sm.addSignature(buf, sig.getParameterTypes());

			name = buf.toString();
		} else if (signature instanceof InitializerSignature) {
			InitializerSignature sig = (InitializerSignature) signature;

			StringBuffer buf = new StringBuffer();
			StringMaker sm = StringMaker.longStringMaker;
			buf.append(sm.makePrimaryTypeName(sig.getDeclaringType(), sig.getDeclaringTypeName()));
			buf.append(".");
			buf.append(sig.getName());
			name = buf.toString();
		} else {
			String msg = "AJMethodAgent::getMethodName(..) - Unknown signature type: " + signature.getClass() + " ( " + signature + " )";
			_log.warn(msg);
			throw new RuntimeException(msg);
		}

		return name;
	}

	private static String specializeName(JoinPoint jp) {
		String name = jp.getSignature().toString();

		String oldName = name;
		Object myThis = jp.getThis();
		Object myTarget = jp.getTarget();

		if (myThis != myTarget) {

			String rootName = jp.getSignature().getDeclaringTypeName();
			String targetTypeName = jp.getTarget().getClass().getName();
			if (!rootName.equals(targetTypeName)) {
				int offsetIndex = 0;
				boolean isConstructor = false;

				if (jp.getSignature() instanceof ConstructorSignature)
					isConstructor = true;

				if (name.indexOf(" ") < name.indexOf("(")) {
					offsetIndex = name.indexOf(" ");
					isConstructor = false;
				}

				String retType = name.substring(0, offsetIndex);

				int padding = 2;
				if (isConstructor)
					padding = 1;

				String methodName = name.substring(rootName.length() + retType.length() + padding);
				name = retType + " " + targetTypeName + "." + methodName;
				// _log.debug("Specialize name from: " + oldName + " -> " + name);
			}
		}
		return name;
	}

	public static String getClassName(JoinPoint jp) {
		return jp.getSignature().getDeclaringTypeName();
	}

}

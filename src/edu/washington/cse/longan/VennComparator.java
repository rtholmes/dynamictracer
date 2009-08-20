package edu.washington.cse.longan;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import ca.lsmr.common.log.LSMRLogger;
import ca.lsmr.common.util.TimeUtility;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;

import edu.washington.cse.longan.io.SessionXMLReader;
import edu.washington.cse.longan.io.SessionXMLWriter;
import edu.washington.cse.longan.model.AbstractElement;
import edu.washington.cse.longan.model.FieldElement;
import edu.washington.cse.longan.model.ILonganConstants;
import edu.washington.cse.longan.model.MethodElement;
import edu.washington.cse.longan.model.Session;

public class VennComparator {
	Logger _log = Logger.getLogger(this.getClass());
	private long _start;

	// private boolean _outputInvocationDifferences = false;
	// private boolean _outputCountDifferences = false;
	// private boolean _outputMissing = true;
	// private boolean _outputAdded = true;
	// private boolean _outputCombined = true;
	// private boolean _outputIndividual = false;
	// private boolean _outputPaths = true;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// LOGGING
		LSMRLogger.startLog4J(false, ILonganConstants.LOGGING_LEVEL);

		long start = System.currentTimeMillis();

		String path = ILonganConstants.OUTPUT_PATH;

		
		 String staticAPath = path + "longAnTestC-1_static.xml";
		 String staticBPath = path + "longAnTestC-2_static.xml";
		 String dynamicAPath = path + "longAnTestC-1_dynamic-4.xml";
		 String dynamicBPath = path + "longAnTestC-2_dynamic-4.xml";

		
//		String staticAPath = path + "longAnTestObserver-1a_static.xml";
//		String staticBPath = path + "longAnTestObserver-2a_static.xml";
//		String dynamicAPath = path + "longAnTestObserver-1b_dynamic.xml";
//		String dynamicBPath = path + "longAnTestObserver-2b_dynamic.xml";

		DataProvider provider = new DataProvider(staticAPath, staticBPath, dynamicAPath, dynamicBPath);

		VennComparator ec = new VennComparator();
		ec.start();
		ec.run(provider);
		ec.done(start);
	}

	private void run(DataProvider provider) {
		_log.info("DPC.run()");

		ExecutionDelta staticAed = convertSessionToExecutionDelta(provider.getStaticA());
		ExecutionDelta dynamicAed = convertSessionToExecutionDelta(provider.getDynamicA());
		ExecutionDelta staticBed = convertSessionToExecutionDelta(provider.getStaticB());
		ExecutionDelta dynamicBed = convertSessionToExecutionDelta(provider.getDynamicB());

		_log.info("static1; elements: " + staticAed.getElements().size() + " paths: " + staticAed.getPaths().size());
		_log.info("static2; elements: " + staticBed.getElements().size() + " paths: " + staticBed.getPaths().size());
		_log.info("dyanmic1; elements: " + dynamicAed.getElements().size() + " paths: " + dynamicAed.getPaths().size());
		_log.info("dyanmic2; elements: " + dynamicBed.getElements().size() + " paths: " + dynamicBed.getPaths().size());

		ExecutionDelta tmp = null;

		ExecutionDelta v1s;
		ExecutionDelta v2s;
		ExecutionDelta v1d;
		ExecutionDelta v2d;

		// V1s
		tmp = difference(staticAed, staticBed);
		tmp = difference(tmp, dynamicAed);
		v1s = difference(tmp, dynamicBed);
		_log.info("V1S; elements: " + v1s.getElements().size() + " paths: " + v1s.getPaths().size());

		tmp = null;
		tmp = difference(staticBed, staticAed);
		tmp = difference(tmp, dynamicAed);
		v2s = difference(tmp, dynamicBed);
		_log.info("V2S; elements: " + v2s.getElements().size() + " paths: " + v2s.getPaths().size());

		tmp = null;
		tmp = difference(dynamicAed, dynamicBed);
		tmp = difference(tmp, staticAed);
		v1d = difference(tmp, staticBed);
		_log.info("V1D; elements: " + v1d.getElements().size() + " paths: " + v1d.getPaths().size());

		tmp = null;
		tmp = difference(dynamicBed, dynamicAed);
		tmp = difference(tmp, staticAed);
		v2d = difference(tmp, staticBed);
		_log.info("V2D; elements: " + v2d.getElements().size() + " paths: " + v2d.getPaths().size());

		_log.info("V1S");
		for (String element : v1s.getElements())
			_log.info("\telement: " + element);
		for (Path path : v1s.getPaths())
			_log.info("\tpath: " + path);

		_log.info("V2S");
		for (String element : v2s.getElements())
			_log.info("\telement: " + element);
		for (Path path : v2s.getPaths())
			_log.info("\tpath: " + path);

		_log.info("V1D");
		for (String element : v1d.getElements())
			_log.info("\telement: " + element);
		for (Path path : v1d.getPaths())
			_log.info("\tpath: " + path);

		_log.info("V2D");
		for (String element : v2d.getElements())
			_log.info("\telement: " + element);
		for (Path path : v2d.getPaths())
			_log.info("\tpath: " + path);

		_log.info("ruminate");
		// // tmp - v1d
		// tmp = difference(tmp)
		//		
		// ExecutionDelta v1s= difference(compare(provider.getStaticA(), provider.getStaticB()), ;

		// _log.info("dynamicA vs. dynamicB -> DYN1");
		// ExecutionDelta dynamicDelta = compare(provider.getDynamicA(), provider.getDynamicB());
		//
		// _log.info("STAT1 vs. DYN1");
		// // ExecutionDelta overallDelta1 = compare(staticDelta, dynamicDelta);
		//
		// _log.info("staticA vs. dynamicA -> RUN_A");
		// ExecutionDelta sessionADelta = compare(provider.getStaticA(), provider.getDynamicA());
		//
		// _log.info("staticB vs. dynamicB -> RUN_B");
		// ExecutionDelta sessionBDelta = compare(provider.getStaticB(), provider.getDynamicB());
		//
		// _log.info("RUN_A vs. RUN_B");
		// ExecutionDelta overallDelta2 = compare(sessionADelta, sessionBDelta);
	}

	private ExecutionDelta difference(ExecutionDelta delta1, ExecutionDelta delta2) {
		Set<String> elements = Sets.difference(delta1.getElements(), delta2.getElements());
		Set<Path> paths = Sets.difference(delta1.getPaths(), delta2.getPaths());

		ExecutionDelta ed = new ExecutionDelta(elements, paths);
		return ed;
	}

	// private ExecutionDelta compare(ExecutionDelta delta1, ExecutionDelta delta2) {
	//
	// _log.info("Comparing deltas");
	//
	// Set<String> addedElements = Sets.difference(Sets.union(delta1.get_addedElements(), delta2.get_addedElements()),
	// Sets.intersection(delta1
	// .get_addedElements(), delta2.get_addedElements()));
	// Set<String> removedElements = Sets.difference(Sets.union(delta1.get_removedElements(),
	// delta2.get_removedElements()), Sets.intersection(
	// delta1.get_removedElements(), delta2.get_removedElements()));
	//
	// Set<Path> addedPaths = Sets.difference(Sets.union(delta1.get_addedPaths(), delta2.get_addedPaths()),
	// Sets.intersection(delta1
	// .get_addedPaths(), delta2.get_addedPaths()));
	// Set<Path> removedPaths = Sets.difference(Sets.union(delta1.get_removedPaths(), delta2.get_removedPaths()),
	// Sets.intersection(delta1
	// .get_removedPaths(), delta2.get_removedPaths()));
	//
	// for (String addedElement : addedElements) {
	// boolean in1 = delta1.get_sessionA().hasIDForElement(addedElement);
	// boolean in2 = delta2.get_sessionA().hasIDForElement(addedElement);
	// if (!in1 && !in2) {
	// _log.info("Added elem (bth): " + addedElement);
	// } else if (!in1) {
	// _log.info("Added elem (1st): " + addedElement);
	// } else if (!in2) {
	// _log.info("Added elem (2nd): " + addedElement);
	// } else {
	// Preconditions.checkNotNull(null, ILonganConstants.NOT_POSSIBLE);
	// }
	// }
	//
	// for (String removedElement : removedElements) {
	// boolean in1 = delta1.get_sessionB().hasIDForElement(removedElement);
	// boolean in2 = delta2.get_sessionB().hasIDForElement(removedElement);
	//
	// if (!in1 && !in2)
	// _log.info("Removed e (bth): " + removedElement);
	// else if (!in1)
	// _log.info("Removed e (1st): " + removedElement);
	// else if (!in2)
	// _log.info("Removed e (2nd): " + removedElement);
	// else
	// Preconditions.checkNotNull(null, ILonganConstants.NOT_POSSIBLE);
	// // _log.info("Removed element: " + removedElement);
	// }
	//
	// for (Path addedPath : addedPaths) {
	// // _log.info("Added path: " + addedPath);
	//
	// boolean in1 = true;
	// boolean in2 = true;
	//
	// if (!addedPath.existsInSession(delta1.get_sessionA()) && addedPath.existsInSession(delta1.get_sessionB()))
	// in1 = false;
	//
	// if (!addedPath.existsInSession(delta2.get_sessionA()) && addedPath.existsInSession(delta2.get_sessionB()))
	// in2 = false;
	//
	// if (!in1 && !in2)
	// _log.info("Added path (bth): " + addedPath);
	// else if (!in1)
	// _log.info("Added path (1st): " + addedPath);
	// else if (!in2)
	// _log.info("Added path (2nd): " + addedPath);
	// else
	// Preconditions.checkNotNull(null, ILonganConstants.NOT_POSSIBLE);
	//
	// }
	//
	// for (Path removedPath : removedPaths) {
	// _log.info("Removed path: " + removedPath);
	// }
	//
	// return null;
	// }

	private void start() {
		_start = System.currentTimeMillis();

	}

	private void done(long start) {
		_log.info("Done in: " + TimeUtility.msToHumanReadableDelta(start));
	}

	private void readAndWrite(String fName) {
		_log.info("Read and Write");
		SessionXMLReader sxmlr = new SessionXMLReader();
		Session sess = sxmlr.readXML(fName);
		SessionXMLWriter sxmlw = new SessionXMLWriter();
		sxmlw.write(fName + "_out.xml", sess);
	}

	// private void compare(Vector<String> executionFiles) {
	//
	// Vector<Session> sessions = new Vector<Session>();
	// for (String fName : executionFiles) {
	//
	// SessionXMLReader sxmlr = new SessionXMLReader();
	// Session sess = sxmlr.readXML(fName);
	// sessions.add(sess);
	// }
	//
	// Session previousSession = null;
	// for (Session currentSession : sessions) {
	//
	// if (previousSession != null) {
	// convertSessionToExecutionDelta(previousSession, currentSession);
	// }
	//
	// previousSession = currentSession;
	// }
	//
	// }

	private ExecutionDelta convertSessionToExecutionDelta(Session sA) {
		Preconditions.checkNotNull(sA, "Session A is null");

		_log.info("Extracting: " + sA.getSessionName());// + " to: " + sB.getSessionName());

		ExecutionDelta ed = new ExecutionDelta(new HashSet<String>(), new HashSet<Path>());

		extractElements(sA, ed);
		extractPaths(sA, ed);

		return ed;
	}

	// private void checkFieldDifferences(Session sA, Session sB) {
	// ImmutableSet<String> eAnames = ImmutableSet.copyOf(sA.getFieldNames());
	// ImmutableSet<String> eBnames = ImmutableSet.copyOf(sB.getFieldNames());
	//
	// if (_outputIndividual) {
	// _log.info("CHECK FIELD GET DIFFERENCES");
	// for (String eName : Sets.intersection(eAnames, eBnames)) {
	//
	// _log.debug("common field: " + eName);
	//
	// FieldElement fA = sA.getField(sA.getIdForElement(eName));
	// FieldElement fB = sB.getField(sB.getIdForElement(eName));
	//
	// FieldTraitContainer ftgA = fA.getFieldGetTraitContainer();
	// FieldTraitContainer ftgB = fB.getFieldGetTraitContainer();
	//
	// _log.debug("\tA gets: " + fA.getGetBy().size() + "\tsets: " + fA.getSetBy().size());
	// _log.debug("\tB gets: " + fB.getGetBy().size() + "\tsets: " + fB.getSetBy().size());
	//
	// for (int getById : fA.getGetBy().elementSet()) {
	// String elemName = sA.getElementNameForID(getById);
	//
	// ITrait traitsA[] = null;
	// ITrait traitsB[] = null;
	//
	// traitsA = ftgA.getTraitsForCaller(getById);
	// if (sB.hasIDForElement(elemName))
	// traitsB = ftgB.getTraitsForCaller(sB.getIdForElement(elemName));
	//
	// // compare trait arrays
	//
	// compareTraitDifferences(traitsA, traitsB, fA.getName() + " referenced by: " + elemName);
	// }
	//
	// for (int getById : fB.getGetBy().elementSet()) {
	// String elemName = sB.getElementNameForID(getById);
	//
	// ITrait traitsB[] = null;
	// ITrait traitsA[] = null;
	//
	// traitsB = ftgB.getTraitsForCaller(getById);
	// if (sA.hasIDForElement(elemName))
	// traitsA = ftgA.getTraitsForCaller(sA.getIdForElement(elemName));
	//
	// // compare trait arrays
	// compareTraitDifferences(traitsA, traitsB, fB.getName() + " referenced by: " + elemName);
	// }
	//
	// }
	//
	// // Iterates through elements that exist in A but not in B
	// // e.g., this gives an indication of new elements in B
	// for (String eName : Sets.difference(eAnames, eBnames)) {
	//
	// }
	//
	// // Iterates through elements that exist in B but not in C
	// // e.g., this gives an indication of elements in A that
	// // are no longer present in B
	// for (String eName : Sets.difference(eBnames, eAnames)) {
	//
	// }
	// }
	//
	// if (_outputIndividual) {
	// _log.info("CHECK FIELD SET DIFFERENCES");
	// for (String eName : Sets.intersection(eAnames, eBnames)) {
	//
	// _log.debug("common field: " + eName);
	//
	// FieldElement fA = sA.getField(sA.getIdForElement(eName));
	// FieldElement fB = sB.getField(sB.getIdForElement(eName));
	//
	// FieldTraitContainer ftgA = fA.getFieldSetTraitContainer();
	// FieldTraitContainer ftgB = fB.getFieldSetTraitContainer();
	//
	// _log.debug("\tgets: " + fA.getGetBy().size() + "\tsets: " + fA.getSetBy().size());
	// for (int setById : fA.getSetBy().elementSet()) {
	// String elemName = sA.getElementNameForID(setById);
	//
	// ITrait traitsA[] = null;
	// ITrait traitsB[] = null;
	//
	// traitsA = ftgA.getTraitsForCaller(setById);
	// if (sB.hasIDForElement(elemName))
	// traitsB = ftgB.getTraitsForCaller(sB.getIdForElement(elemName));
	//
	// // compare trait arrays
	// compareTraitDifferences(traitsA, traitsB, fA.getName() + " referenced by: " + elemName);
	// }
	//
	// for (int setById : fB.getSetBy().elementSet()) {
	// String elemName = sB.getElementNameForID(setById);
	//
	// ITrait traitsB[] = null;
	// ITrait traitsA[] = null;
	//
	// traitsB = ftgB.getTraitsForCaller(setById);
	// if (sA.hasIDForElement(elemName))
	// traitsA = ftgA.getTraitsForCaller(sA.getIdForElement(elemName));
	//
	// // compare trait arrays
	// compareTraitDifferences(traitsA, traitsB, fB.getName() + " referenced by: " + elemName);
	// }
	//
	// }
	// }
	//
	// if (_outputCombined) {
	// _log.info("CHECK COMBINED FIELD GET DIFFERENCES");
	// for (String eName : Sets.intersection(eAnames, eBnames)) {
	// _log.debug("common field: " + eName);
	//
	// FieldElement fA = sA.getField(sA.getIdForElement(eName));
	// FieldElement fB = sB.getField(sB.getIdForElement(eName));
	//
	// FieldTraitContainer ftgA = fA.getFieldGetTraitContainer();
	// FieldTraitContainer ftgB = fB.getFieldGetTraitContainer();
	//
	// compareTraitDifferences(ftgA.getTraitsCollapsed(), ftgB.getTraitsCollapsed(), fA.getName());
	// }
	// }
	//
	// if (_outputCombined) {
	// _log.info("CHECK COMBINED FIELD SET DIFFERENCES");
	// for (String eName : Sets.intersection(eAnames, eBnames)) {
	// _log.debug("common field: " + eName);
	//
	// FieldElement fA = sA.getField(sA.getIdForElement(eName));
	// FieldElement fB = sB.getField(sB.getIdForElement(eName));
	//
	// FieldTraitContainer ftgA = fA.getFieldSetTraitContainer();
	// FieldTraitContainer ftgB = fB.getFieldSetTraitContainer();
	//
	// compareTraitDifferences(ftgA.getTraitsCollapsed(), ftgB.getTraitsCollapsed(), fA.getName());
	// }
	// }
	// }
	//
	// private void checkExceptionDifferences(Session sA, Session sB) {
	// _log.info("CHECK EXCEPTION DIFFERENCES");
	//
	// ImmutableSet<String> eAnames = ImmutableSet.copyOf(sA.getMethodNames());
	// ImmutableSet<String> eBnames = ImmutableSet.copyOf(sB.getMethodNames());
	//
	// // we can be a lot more precise here; eg., we could note exceptions that
	// // have different sources (rather than whole paths) or are caught in different locations
	// for (String eName : Sets.intersection(eAnames, eBnames)) {
	//
	// MethodElement mA = sA.getMethod(sA.getIdForElement(eName));
	// MethodElement mB = sB.getMethod(sB.getIdForElement(eName));
	//
	// for (ExceptionTrait etA : mA.getExceptions()) {
	// boolean matched = false;
	// for (ExceptionTrait etB : mB.getExceptions()) {
	// if (ExceptionTrait.equals(etA, etB, sA, sB)) {
	// matched = true;
	// }
	// }
	// if (matched) {
	//
	// } else {
	// if (_outputMissing)
	// _log.info("Exception disappeared from: " + mA + " ex: " + etA);
	// }
	// }
	//
	// for (ExceptionTrait etB : mB.getExceptions()) {
	// boolean matched = false;
	// for (ExceptionTrait etA : mA.getExceptions()) {
	// if (ExceptionTrait.equals(etA, etB, sA, sB)) {
	// matched = true;
	// }
	// }
	// if (matched) {
	//
	// } else {
	// if (_outputAdded)
	// _log.info("Exception added to: " + mB + " ex: " + etB);
	// }
	// }
	//
	// }
	//
	// // Iterates through elements that exist in A but not in B
	// // e.g., this gives an indication of new elements in B
	// for (String eName : Sets.difference(eAnames, eBnames)) {
	//
	// }
	//
	// // Iterates through elements that exist in B but not in C
	// // e.g., this gives an indication of elements in A that
	// // are no longer present in B
	// for (String eName : Sets.difference(eBnames, eAnames)) {
	//
	// }
	//
	// }
	//
	private void checkTotalMethodInvocationCounts(Session sA, Session sB) {
		_log.info("CHECK INVOCATION COUNTS");

		ImmutableSet<String> eAnames = ImmutableSet.copyOf(sA.getMethodNames());
		ImmutableSet<String> eBnames = ImmutableSet.copyOf(sB.getMethodNames());

		for (String eName : Sets.intersection(eAnames, eBnames)) {

			MethodElement mA = sA.getMethod(sA.getIdForElement(eName));
			MethodElement mB = sB.getMethod(sB.getIdForElement(eName));

			int aInvokationTotal = 0;
			int bInvokationTotal = 0;

			Multiset<Integer> calledBys;

			calledBys = mA.getCalledBy();
			for (int calledBy : calledBys.elementSet()) {
				aInvokationTotal += calledBys.count(calledBy);
			}

			calledBys = mB.getCalledBy();
			for (int calledBy : calledBys.elementSet()) {
				bInvokationTotal += calledBys.count(calledBy);
			}

			if (aInvokationTotal != bInvokationTotal) {
				// if (_outputInvocationDifferences)
				_log.warn("Invocation differences ( " + aInvokationTotal + " -> " + bInvokationTotal + " ) for: " + mA.getName());
			}
		}

		// Iterates through elements that exist in A but not in B
		// e.g., this gives an indication of new elements in B
		for (String eName : Sets.difference(eAnames, eBnames)) {

		}

		// Iterates through elements that exist in B but not in C
		// e.g., this gives an indication of elements in A that
		// are no longer present in B
		for (String eName : Sets.difference(eBnames, eAnames)) {

		}

	}

	private Set<String> extractNames(Set<AbstractElement> elements) {
		HashSet<String> set = new HashSet<String>();
		for (AbstractElement ae : elements)
			set.add(ae.getName());

		return set;
	}

	private void checkForMissingPaths(Session sA, Session sB, ExecutionDelta ed) {

		_log.info("CHECK FOR MISSING PATHS");

		ImmutableSet<String> eAnames = ImmutableSet.copyOf(sA.getMethodNames());
		ImmutableSet<String> eBnames = ImmutableSet.copyOf(sB.getMethodNames());

		ImmutableSet<String> interNames = Sets.intersection(eAnames, eBnames).immutableCopy();
		ImmutableSet<String> missingNames = Sets.difference(eAnames, eBnames).immutableCopy();
		ImmutableSet<String> newNames = Sets.difference(eBnames, eAnames).immutableCopy();

		for (String eName : interNames) {

			MethodElement mA = sA.getMethodForName(eName);
			MethodElement mB = sB.getMethodForName(eName);

			Set<AbstractElement> mACBs = sA.getElementSet(mA.getCalledBy().elementSet());
			Set<AbstractElement> mBCBs = sB.getElementSet(mB.getCalledBy().elementSet());

			Set<String> mACBnames = extractNames(mACBs);
			Set<String> mBCBnames = extractNames(mBCBs);

			ImmutableSet<String> interCBNames = Sets.intersection(mACBnames, mBCBnames).immutableCopy();
			ImmutableSet<String> missingCBNames = Sets.difference(mACBnames, mBCBnames).immutableCopy();
			ImmutableSet<String> newCBNames = Sets.difference(mBCBnames, mACBnames).immutableCopy();

			for (String cbName : interCBNames) {
				// common paths
			}

			for (String cbName : missingCBNames) {

				boolean missingTarget = _missingElementNames.contains(eName);
				boolean missingSource = _missingElementNames.contains(cbName);

				if (missingSource && missingTarget) {
					// if the source and target are missing nothing can happen
					_log.info("Path missing ( non-exersized source & target ) to: " + eName + " from: " + cbName);
				} else if (missingSource) {
					// if a source is missing, it isn't call is gone
					_log.info("Path missing ( non-exersized source ) to: " + eName + " from: " + cbName);
				} else if (missingTarget) {
					// if a target is missing, it isn't surprising that it can't be called
					_log.info("Path missing ( non-exersized target ) to: " + eName + " from: " + cbName);
				} else {
					Preconditions.checkArgument(!missingSource && !missingTarget);
					_log.warn("Path removed to: " + eName + " from: " + cbName);
				}
				// ed.removedPath(cbName, eName);
			}

			for (String cbName : newCBNames) {
				// RFE: report path counts for new paths?
			}

		}

		for (String eName : missingNames) {

			MethodElement mA = sA.getMethodForName(eName);
			// mB doesn't exist because these are new names
			MethodElement mB = null;

			Set<AbstractElement> mACBs = sA.getElementSet(mA.getCalledBy().elementSet());
			// Set<AbstractElement> mBCBs = sB.getElementSet(mB.getCalledBy().elementSet());

			Set<String> mACBnames = extractNames(mACBs);
			// Set<String> mBCBnames = extractNames(mBCBs);

			for (String cbName : mACBnames) {
				if (sB.hasIDForElement(cbName)) {
					_log.info("Path missing (with existing source) from: " + cbName + " to: " + eName);
				} else {
					_log.info("Path missing (with missing source and target) from: " + cbName + " to: " + eName);
				}
				// ed.removedPath(cbName, eName);
			}

		}

	}

	ImmutableSet<String> _newElementNames = null;
	ImmutableSet<String> _missingElementNames = null;

	/**
	 * Retunrs all of the paths that are in sA that aren't in sB. Mainly used initially to compare against an empty
	 * session.
	 * 
	 * @param sA
	 * @param sB
	 * @param ed
	 */
	private void extractPaths(Session sA, ExecutionDelta ed) {

		_log.info("CHECK FOR PATH DIFFERENCES");

		// ImmutableSet<Path> sAPaths = sA.getPaths();

		ImmutableSet<String> eAnames = ImmutableSet.copyOf(sA.getMethodNames());

		for (String eName : eAnames) {

			MethodElement mA = sA.getMethodForName(eName);
			Set<AbstractElement> mACBs = sA.getElementSet(mA.getCalledBy().elementSet());
			Set<String> mACBnames = extractNames(mACBs);

			for (String cbName : mACBnames) {
				String target = eName;
				String source = cbName;

				ed.addPath(source, target);
			}
		}
	}

	/**
	 * Essentially find any element in sessionA that isn't in sessionB. This is used mainly at the beginning comparing
	 * to an empty set, so it should be pretty easy.
	 * 
	 * @param sessionA
	 * @param sessionB
	 * @param ed
	 */
	private void extractElements(Session sessionA, ExecutionDelta ed) {

		_log.info("CHECK FOR ELEMENT DIFFERENCES");

		Set<String> missingElements = sessionA.getElementNames();

		Vector<String> names = new Vector<String>();
		for (String elemName : missingElements) {
			names.add(elemName);

			ed.addElement(elemName);
		}
	}

	private void checkForNewElements(Session sessionA, Session sessionB, ExecutionDelta ed) {

		_log.info("CHECK FOR NEW ELEMENTS");

		Vector<String> newElements = new Vector<String>();

		ImmutableSet<String> newNames = Sets.difference(sessionB.getElementNames(), sessionA.getElementNames()).immutableCopy();
		_newElementNames = newNames;

		for (String elemName : _newElementNames) {
			newElements.add(elemName);
		}

		Collections.sort(newElements);
		for (String elem : newElements) {
			_log.warn("Session B adds new element: " + elem);
			// ed.addedElementhat'(elem);
		}
	}

}

class DataProvider {
	Logger _log = Logger.getLogger(this.getClass());

	Session _staticA;
	Session _staticB;
	Session _dynamicA;
	Session _dynamicB;

	DataProvider(String staticAPath, String staticBPath, String dynamicAPath, String dynamicBPath) {
		try {
			_staticA = loadSession(staticAPath);
			_staticB = loadSession(staticBPath);
			_dynamicA = loadSession(dynamicAPath);
			_dynamicB = loadSession(dynamicBPath);
		} catch (Exception e) {
			_log.fatal(e);
		}
	}

	private Session loadSession(String path) {
		try {
			SessionXMLReader sxmlr = new SessionXMLReader();
			Session sess = sxmlr.readXML(path);
			return sess;
		} catch (Exception e) {
			_log.error("Error loading session file: " + path, e);
		}
		return null;
	}

	public Session getStaticA() {
		return _staticA;
	}

	public Session getStaticB() {
		return _staticB;
	}

	public Session getDynamicA() {
		return _dynamicA;
	}

	public Session getDynamicB() {
		return _dynamicB;
	}
}

class ExecutionDelta {

	private Set<String> _elements = new HashSet<String>();
	private Set<Path> _paths = new HashSet<Path>();

	//
	// private Session _sessionA;
	// private Session _sessionB;
	//
	// ExecutionDelta(Session sessionA, Session sessionB) {
	// _sessionA = sessionA;
	// _sessionB = sessionB;
	// }
	//
	// public Session get_sessionA() {
	// return _sessionA;
	// }
	//
	// public Session get_sessionB() {
	// return _sessionB;
	// }

	ExecutionDelta(Set<String> elements, Set<Path> paths) {
		_paths = paths;
		_elements = elements;
	}

	public void addPath(String source, String target) {
		_paths.add(new Path(source, target));
	}

	public void addElement(String elementName) {
		_elements.add(elementName);
	}

	public Set<String> getElements() {
		return _elements;
	}

	public Set<Path> getPaths() {
		return _paths;
	}

}

// class Path {
// final String _source;
// final String _target;
// final String _sig;
//
// Path(String source, String target) {
// Preconditions.checkNotNull(source);
// Preconditions.checkNotNull(target);
// _source = source;
// _target = target;
// _sig = source + " -> " + target;
// }
//
// @Override
// public boolean equals(Object obj) {
// if (obj instanceof Path)
// return ((Path) obj).toString().equals(toString());
// return false;
// }
//
// @Override
// public int hashCode() {
// return toString().hashCode();
// }
//
// @Override
// public String toString() {
// return _sig;
// }
//
// public String get_source() {
// return _source;
// }
//
// public String get_target() {
// return _target;
// }
//
// public boolean existsInSession(Session session) {
// boolean exists = false;
//
// if (session.hasIDForElement(_source) && session.hasIDForElement(_target)) {
// AbstractElement target = session.getElementForName(_target);
//
// if (target instanceof MethodElement) {
// MethodElement targetM = ((MethodElement) target);
//
// exists = targetM.getCalledBy().contains(session.getIdForElement(_source));
//
// } else if (target instanceof FieldElement) {
// Preconditions.checkNotNull(null, "not implemented yet");
// }
// }
//
// // System.out.println("Session contains path: " + toString() + " ? " + exists);
// return exists;
// }

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

public class DynamicPathComparator {
	Logger _log = Logger.getLogger(this.getClass());
	private long _start;

	private boolean _outputInvocationDifferences = false;
	private boolean _outputCountDifferences = false;
	private boolean _outputMissing = true;
	private boolean _outputAdded = true;
	private boolean _outputCombined = true;
	private boolean _outputIndividual = false;
	private boolean _outputPaths = true;

	// /**
	// * @param args
	// */
	// public static void main(String[] args) {
	// // LOGGING
	// LSMRLogger.startLog4J(true, ILonganConstants.LOGGING_LEVEL);
	//
	// long start = System.currentTimeMillis();
	//
	// String path = ILonganConstants.OUTPUT_PATH;
	//
	// Vector<String> executionFiles = new Vector<String>();
	// boolean complete = true;
	// if (complete) {
	// // joda
	// // executionFiles.add(path + "joda1283a.xml");
	// // executionFiles.add(path + "joda1283b.xml");
	// // executionFiles.add(path + "joda1311a.xml");
	// // executionFiles.add(path + "joda1311b.xml");
	// // executionFiles.add(path + "joda1322a.xml");
	// // executionFiles.add(path + "joda1322b.xml");
	// // executionFiles.add(path + "joda1371a.xml");
	// // executionFiles.add(path + "joda1371b.xml");
	//
	// // executionFiles.add(path + "latestA.xml");
	// // executionFiles.add(path + "latestB.xml");
	// // executionFiles.add(path + "1311-1.xml");
	// // executionFiles.add(path + "1311-2.xml");
	// // executionFiles.add(path + "1311-3.xml");
	// // executionFiles.add(path + "1311-4.xml");
	// // executionFiles.add(path + "1311-5.xml");
	// // executionFiles.add(path + "1311-6.xml");
	// // executionFiles.add(path + "1311-7.xml");
	// // executionFiles.add(path + "1311-8.xml");
	// // executionFiles.add(path + "1311-9.xml");
	//
	// // matching pairs
	// // executionFiles.add(path + "cntA.xml");
	// // executionFiles.add(path + "cntB.xml");
	//
	// // different pairs
	// // executionFiles.add(path + "cntC.xml");
	// // executionFiles.add(path + "cntD.xml");
	//
	// // executionFiles.add(path + "cntE.xml");
	// // executionFiles.add(path + "cntF.xml");
	// // executionFiles.add(path + "cntG.xml");
	//
	// // executionFiles.add(path + "inhA.xml");
	// // executionFiles.add(path + "inhB.xml");
	// // executionFiles.add(path + "inhC.xml");
	// // executionFiles.add(path + "inhD.xml");
	// // executionFiles.add(path + "inhE.xml");
	//
	// // } else {
	// // inh run
	// // executionFiles.add(path + "6-30_1283.xml");
	// // executionFiles.add(path + "6-30_1311.xml");
	// // executionFiles.add(path + "6-30_1322.xml");
	// // executionFiles.add(path + "6-30_1371.xml");
	//
	// // executionFiles.add(path + "inhA.xml");
	// // executionFiles.add(path + "inhB.xml");
	// // executionFiles.add(path + "latest.xml");
	// // executionFiles.add(path + "latest.xml");
	//
	// // executionFiles.add(path + "log4j_v1_2_15_rc1.xml");
	// // executionFiles.add(path + "log4j_v1_2_15_rc6.xml");
	//
	// // executionFiles.add(path + "joda1371c.xml");
	// // executionFiles.add(path + "jibx-core.xml");
	// // executionFiles.add(path + "kaching-api.xml");
	// // executionFiles.add(path + "google-rfc-2445.xml");
	//
	// // executionFiles.add(path + "static_JodaTime_1322.xml");
	// // executionFiles.add(path + "static_JodaTime_1322.xml");
	// // executionFiles.add(path + "static_JodaTime_1371.xml");
	// // executionFiles.add(path + "static_JodaTime_1371a.xml");
	// // executionFiles.add(path + "static_JodaTime_1371b.xml");
	// // executionFiles.add(path + "static_JodaTime_1371b.xml");
	// // executionFiles.add(path + "latestB.xml");
	//
	// executionFiles.add(path + "longAnTestC-1a.xml");
	// executionFiles.add(path + "longAnTestC-2a.xml");
	// }
	// DynamicPathComparator ec = new DynamicPathComparator();
	// ec.start();
	//
	// ec.compare(executionFiles);
	//
	// // ec = new ExecutionComparator();
	// // ec.readAndWrite(executionFiles.firstElement());
	//
	// ec.done(start);
	// }

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// LOGGING
		LSMRLogger.startLog4J(true, ILonganConstants.LOGGING_LEVEL);

		long start = System.currentTimeMillis();

		String path = ILonganConstants.OUTPUT_PATH;

		String staticAPath = path + "longAnTestC-1_static.xml";
		String staticBPath = path + "longAnTestC-2_static.xml";
		String dynamicAPath = path + "longAnTestC-1_dynamic.xml";
		String dynamicBPath = path + "longAnTestC-2_dynamic.xml";

		DataProvider provider = new DataProvider(staticAPath, staticBPath, dynamicAPath, dynamicBPath);

		DynamicPathComparator ec = new DynamicPathComparator();
		ec.start();
		ec.run(provider);
		ec.done(start);
	}

	private void run(DataProvider provider) {
		_log.info("DPC.run()");

		_log.info("staticA vs. staticB -> STAT1");
		ExecutionDelta staticDelta = compare(provider.getStaticA(), provider.getStaticB());
		
		_log.info("dynamicA vs. dynamicB -> DYN1");
		ExecutionDelta dynamicDelta = compare(provider.getDynamicA(), provider.getDynamicB());

		_log.info("STAT1 vs. DYN1");
		ExecutionDelta overallDelta1 = compare(staticDelta, dynamicDelta);
		
		_log.info("staticA vs. dynamicA -> RUN_A");
		 ExecutionDelta sessionADelta = compare(provider.getStaticA(), provider.getDynamicA());
		 
		 _log.info("staticB vs. dynamicB -> RUN_B");
		 ExecutionDelta sessionBDelta = compare(provider.getStaticB(), provider.getDynamicB());
		 
		 _log.info("RUN_A vs. RUN_B");
		 ExecutionDelta overallDelta2 = compare(sessionADelta, sessionBDelta);
	}

	private ExecutionDelta compare(ExecutionDelta delta1, ExecutionDelta delta2) {

		_log.info("Comparing deltas");

		Set<String> addedElements = Sets.difference(Sets.union(delta1.get_addedElements(), delta2.get_addedElements()), Sets.intersection(delta1
				.get_addedElements(), delta2.get_addedElements()));
		Set<String> removedElements = Sets.difference(Sets.union(delta1.get_removedElements(), delta2.get_removedElements()), Sets.intersection(
				delta1.get_removedElements(), delta2.get_removedElements()));

		Set<Path> addedPaths = Sets.difference(Sets.union(delta1.get_addedPaths(), delta2.get_addedPaths()), Sets.intersection(delta1
				.get_addedPaths(), delta2.get_addedPaths()));
		Set<Path> removedPaths = Sets.difference(Sets.union(delta1.get_removedPaths(), delta2.get_removedPaths()), Sets.intersection(delta1
				.get_removedPaths(), delta2.get_removedPaths()));

		for (String addedElement : addedElements) {
			boolean in1 = delta1.get_sessionA().hasIDForElement(addedElement);
			boolean in2 = delta2.get_sessionA().hasIDForElement(addedElement);
			if (!in1 && !in2)
				_log.info("Added element (both): " + addedElement);
			else if (!in1)
				_log.info("Added element (first): " + addedElement);
			else if (!in2)
				_log.info("Added element (second): " + addedElement);
			else
				Preconditions.checkNotNull(null, ILonganConstants.NOT_POSSIBLE);
		}

		for (String removedElement : removedElements) {
			boolean in1 = delta1.get_sessionB().hasIDForElement(removedElement);
			boolean in2 = delta2.get_sessionB().hasIDForElement(removedElement);

			if (!in1 && !in2)
				_log.info("Removed element (both): " + removedElement);
			else if (!in1)
				_log.info("Removed element (first): " + removedElement);
			else if (!in2)
				_log.info("Removed element (second): " + removedElement);
			else
				Preconditions.checkNotNull(null, ILonganConstants.NOT_POSSIBLE);
			// _log.info("Removed element: " + removedElement);
		}

		for (Path addedPath : addedPaths) {
			// _log.info("Added path: " + addedPath);

			boolean in1 = true;
			boolean in2 = true;

			if (!addedPath.existsInSession(delta1.get_sessionA()) && addedPath.existsInSession(delta1.get_sessionB()))
				in1 = false;

			if (!addedPath.existsInSession(delta2.get_sessionA()) && addedPath.existsInSession(delta2.get_sessionB()))
				in2 = false;

			if (!in1 && !in2)
				_log.info("Added path (both): " + addedPath);
			else if (!in1)
				_log.info("Added path (first): " + addedPath);
			else if (!in2)
				_log.info("Added path (second): " + addedPath);
			else
				Preconditions.checkNotNull(null, ILonganConstants.NOT_POSSIBLE);

		}

		for (Path removedPath : removedPaths) {
			_log.info("Removed path: " + removedPath);
		}

		return null;
	}

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

	private void compare(Vector<String> executionFiles) {

		Vector<Session> sessions = new Vector<Session>();
		for (String fName : executionFiles) {

			SessionXMLReader sxmlr = new SessionXMLReader();
			Session sess = sxmlr.readXML(fName);
			sessions.add(sess);
		}

		Session previousSession = null;
		for (Session currentSession : sessions) {

			if (previousSession != null) {
				compare(previousSession, currentSession);
			}

			previousSession = currentSession;
		}

	}

	private ExecutionDelta compare(Session sA, Session sB) {

		Preconditions.checkNotNull(sA, "Session A is null");
		Preconditions.checkNotNull(sB, "Session B is null");

		_log.info("Comparing: " + sA.getSessionName() + " to: " + sB.getSessionName());

		ExecutionDelta ed = new ExecutionDelta(sA, sB);

		if (_outputCountDifferences)
			checkTotalMethodInvocationCounts(sA, sB);

		checkForMissingElements(sA, sB, ed);
		checkForNewElements(sA, sB, ed);

		if (_outputPaths) {
			checkForMissingPaths(sA, sB, ed);
			checkForNewPaths(sA, sB, ed);
			checkPathCounts(sA, sB);
		}

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
				if (_outputInvocationDifferences)
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

	//
	// private void checkReturnDifferences(Session sA, Session sB) {
	//
	// _log.info("CHECK COMBINED RETURN DIFFERENCES");
	//
	// ImmutableSet<String> eAnames = ImmutableSet.copyOf(sA.getMethodNames());
	// ImmutableSet<String> eBnames = ImmutableSet.copyOf(sB.getMethodNames());
	//
	// for (String eName : Sets.intersection(eAnames, eBnames)) {
	//
	// MethodElement mA = sA.getMethod(sA.getIdForElement(eName));
	// MethodElement mB = sB.getMethod(sB.getIdForElement(eName));
	//
	// Preconditions.checkNotNull(mA);
	// Preconditions.checkNotNull(mB);
	//
	// compareCombinedReturnTraits(mA, mB);
	// }
	//
	// if (_outputIndividual) {
	// _log.info("CHECK CALLEDBY RETURN DIFFERENCES");
	//
	// for (String eName : Sets.intersection(eAnames, eBnames)) {
	//
	// MethodElement mA = sA.getMethod(sA.getIdForElement(eName));
	// MethodElement mB = sB.getMethod(sB.getIdForElement(eName));
	//
	// Preconditions.checkNotNull(mA);
	// Preconditions.checkNotNull(mB);
	//
	// for (int mACBid : mA.getCalledBy().elementSet()) {
	// String callerName = sA.getElementNameForID(mACBid);
	//
	// compareCalledByReturnTraits(sA, sB, mA, mB, callerName);
	//
	// }
	// }
	// }
	// }
	//
	// private void compareCombinedReturnTraits(MethodElement mA, MethodElement mB) {
	// ReturnTraitContainer mArtc = mA.getReturnTraitContainer();
	// ReturnTraitContainer mBrtc = mB.getReturnTraitContainer();
	//
	// compareTraitDifferences(mArtc.getTraitsCollapsed(), mBrtc.getTraitsCollapsed(), mA.getName(), null, -1, true);
	// }
	//
	// private void compareCalledByReturnTraits(Session sA, Session sB, MethodElement mA, MethodElement mB, String
	// calledByName) {
	//
	// ReturnTraitContainer mArtc = mA.getReturnTraitContainer();
	// ReturnTraitContainer mBrtc = mB.getReturnTraitContainer();
	//
	// ITrait[] aTraits = null;
	// if (sA.hasIDForElement(calledByName))
	// aTraits = mArtc.getTraitsForCaller(sA.getIdForElement(calledByName));
	//
	// ITrait[] bTraits = null;
	// if (sB.hasIDForElement(calledByName))
	// bTraits = mBrtc.getTraitsForCaller(sB.getIdForElement(calledByName));
	//
	// compareTraitDifferences(aTraits, bTraits, mA.getName(), calledByName, -1, false);
	// }
	//
	// private void checkParamDifferences(Session sA, Session sB) {
	//
	// ImmutableSet<String> eAnames = ImmutableSet.copyOf(sA.getMethodNames());
	// ImmutableSet<String> eBnames = ImmutableSet.copyOf(sB.getMethodNames());
	//
	// if (_outputCombined) {
	// _log.info("CHECK COMBINED PARAM DIFFERENCES");
	//
	// for (String eName : Sets.intersection(eAnames, eBnames)) {
	//
	// MethodElement mA = sA.getMethod(sA.getIdForElement(eName));
	// MethodElement mB = sB.getMethod(sB.getIdForElement(eName));
	//
	// Preconditions.checkNotNull(mA);
	// Preconditions.checkNotNull(mB);
	//
	// Vector<ParamTraitContainer> mAptcs = mA.getParamTraitContainers();
	// Vector<ParamTraitContainer> mBptcs = mB.getParamTraitContainers();
	//
	// Preconditions.checkArgument(mAptcs.size() == mBptcs.size());
	//
	// for (int j = 0; j < mAptcs.size(); j++) {
	// ParamTraitContainer mAptc = mAptcs.get(j);
	// ParamTraitContainer mBptc = mBptcs.get(j);
	//
	// Preconditions.checkArgument(mAptc.getClass().equals(mBptc.getClass()));
	//
	// compareTraitDifferences(mAptc.getTraitsCollapsed(), mBptc.getTraitsCollapsed(), mA.getName(), null, j, true);
	// }
	// }
	// }
	//
	// if (_outputIndividual) {
	// _log.info("CHECK CALLEDBY PARAM DIFFERENCES");
	//
	// for (String eName : Sets.intersection(eAnames, eBnames)) {
	//
	// MethodElement mA = sA.getMethod(sA.getIdForElement(eName));
	// MethodElement mB = sB.getMethod(sB.getIdForElement(eName));
	//
	// Preconditions.checkNotNull(mA);
	// Preconditions.checkNotNull(mB);
	//
	// for (int mACBid : mA.getCalledBy().elementSet()) {
	// String callerName = sA.getElementNameForID(mACBid);
	//
	// compareCalledByParamTraits(sA, sB, mA, mB, callerName);
	//
	// }
	// }
	// }
	// }
	//
	// private void compareCalledByParamTraits(Session sA, Session sB, MethodElement mA, MethodElement mB, String
	// calledByName) {
	//
	// Vector<ParamTraitContainer> mAptcs = mA.getParamTraitContainers();
	// Vector<ParamTraitContainer> mBptcs = mB.getParamTraitContainers();
	//
	// Preconditions.checkArgument(mAptcs.size() == mBptcs.size());
	//
	// for (int j = 0; j < mAptcs.size(); j++) {
	// ParamTraitContainer mAptc = mAptcs.get(j);
	// ParamTraitContainer mBptc = mBptcs.get(j);
	//
	// Preconditions.checkArgument(mAptc.getClass().equals(mBptc.getClass()));
	//
	// ITrait[] aTraits = null;
	// if (sA.hasIDForElement(calledByName))
	// aTraits = mAptc.getTraitsForCaller(sA.getIdForElement(calledByName));
	//
	// ITrait[] bTraits = null;
	// if (sB.hasIDForElement(calledByName))
	// bTraits = mBptc.getTraitsForCaller(sB.getIdForElement(calledByName));
	//
	// compareTraitDifferences(aTraits, bTraits, mA.getName(), calledByName, j, false);
	// }
	// }
	//
	// // private void compareTraitDifferences(ITrait[] aTraits, ITrait[] bTraits, String preFix, String postFix) {
	// //
	// // }
	// //
	// // private void compareTraitDifferences(ITrait[] aTraits, ITrait[] bTraits) {
	// //
	// // }
	//
	// private void compareTraitDifferences(ITrait[] aTraits, ITrait[] bTraits, String elemName) {
	//
	// if (aTraits == null && bTraits == null) {
	// return;
	// }
	//
	// if (aTraits != null && bTraits != null && aTraits.length > 0 && bTraits.length > 0) {
	//
	// Preconditions.checkArgument(aTraits.length == bTraits.length);
	// for (int i = 0; i < aTraits.length; i++) {
	// ITrait at = aTraits[i];
	// ITrait bt = bTraits[i];
	//
	// Preconditions.checkArgument(at.getClass().equals(bt.getClass()));
	//
	// Set<DATA_KINDS> bMissing = Sets.difference(at.getData().elementSet(), bt.getData().elementSet());
	// Set<DATA_KINDS> bAdds = Sets.difference(bt.getData().elementSet(), at.getData().elementSet());
	//
	// if (bMissing.size() > 0) {
	// for (DATA_KINDS kind : bMissing) {
	// if (checkTraitRelevance(elemName, at, kind))
	// if (_outputMissing)
	// _log.info("\tMissing trait: " + kind + " ( " + at.getData().count(kind) + " )" + " in: " + elemName);
	// }
	// }
	// if (bAdds.size() > 0) {
	// for (DATA_KINDS kind : bAdds) {
	// if (checkTraitRelevance(elemName, bt, kind))
	// if (_outputAdded)
	// _log.info("\tAdded trait: " + kind + " ( " + bt.getData().count(kind) + " )" + " to: " + elemName);
	// }
	// }
	//
	// Set<String> bMissingSD = Sets.difference(at.getSupplementalData().elementSet(),
	// bt.getSupplementalData().elementSet());
	// Set<String> bAddsSD = Sets.difference(bt.getSupplementalData().elementSet(),
	// at.getSupplementalData().elementSet());
	//
	// if (bMissingSD.size() > 0) {
	// for (String key : bMissingSD) {
	// // if (checkTraitRelevance(elemName, at, key))
	// if (_outputMissing)
	// _log.info("\tMissing trait: " + key + " in: " + elemName);
	// }
	// }
	// if (bAddsSD.size() > 0) {
	// for (String key : bAddsSD) {
	// // if (checkTraitRelevance(elemName, at, key))
	// if (_outputAdded)
	// _log.info("\tAdded trait: " + key + " to: " + elemName);
	// }
	// }
	//
	// }
	//
	// } else {
	//
	// Preconditions.checkArgument(!(aTraits == null && bTraits == null), "Shouldn't be possible.");
	//
	// if (aTraits != null && bTraits != null && aTraits.length < 1 && bTraits.length < 1) {
	// // if they're both 0 then there's nothing missing and nothing gained
	// } else {
	// boolean output = false;
	// ITrait[] singleTrait = null;
	// if (bTraits != null && bTraits.length > 0) {
	// singleTrait = bTraits;
	// if (_outputAdded) {
	// _log.info("\tNew traits for: " + elemName);
	// output = true;
	// }
	// }
	// if (aTraits != null && aTraits.length > 0) {
	// singleTrait = aTraits;
	// if (_outputMissing) {
	// _log.info("\tMissing traits for: " + elemName);
	// output = true;
	// }
	// }
	//
	// Preconditions.checkNotNull(singleTrait);
	//
	// for (ITrait trait : singleTrait) {
	// for (DATA_KINDS kind : trait.getData().elementSet())
	// if (output)
	// _log.info("\t\t" + kind + " ( " + trait.getData().count(kind) + " )");
	// for (String key : trait.getSupplementalData().elementSet())
	// if (output)
	// _log.info("\t\t" + key + " ( " + trait.getData().count(key) + " )");
	// }
	// }
	// }
	// }
	//
	// private void compareTraitDifferences(ITrait[] aTraits, ITrait[] bTraits, String elemName, String calledByName,
	// int paramIndex, boolean combined) {
	//
	// if (aTraits == null && bTraits == null) {
	// return;
	// }
	//
	// String preFix = "";
	// if (paramIndex >= 0)
	// preFix = " param ( " + paramIndex + " ) ";
	// else
	// preFix = " return ";
	//
	// String postFix = "";
	// if (combined)
	// postFix = "";
	// else
	// postFix = " when called by: " + calledByName;
	//
	// if (aTraits != null && bTraits != null) {
	//
	// if (aTraits.length != bTraits.length) {
	// System.out.println("");
	// }
	// Preconditions.checkArgument(aTraits.length == bTraits.length);
	// for (int i = 0; i < aTraits.length; i++) {
	// ITrait at = aTraits[i];
	// ITrait bt = bTraits[i];
	//
	// Preconditions.checkArgument(at.getClass().equals(bt.getClass()));
	//
	// Set<DATA_KINDS> bMissing = Sets.difference(at.getData().elementSet(), bt.getData().elementSet());
	// Set<DATA_KINDS> bAdds = Sets.difference(bt.getData().elementSet(), at.getData().elementSet());
	//
	// if (bMissing.size() > 0) {
	// for (DATA_KINDS kind : bMissing) {
	// if (checkTraitRelevance(elemName, at, kind))
	// if (_outputMissing)
	// _log.info("\tMissing" + preFix + "trait: " + kind + " in: " + elemName + postFix);
	// }
	// }
	// if (bAdds.size() > 0) {
	// for (DATA_KINDS kind : bAdds) {
	// if (checkTraitRelevance(elemName, at, kind))
	// if (_outputAdded)
	// _log.info("\tAdded" + preFix + "trait: " + kind + " to: " + elemName + postFix);
	// }
	// }
	//
	// Set<String> bMissingSD = Sets.difference(at.getSupplementalData().elementSet(),
	// bt.getSupplementalData().elementSet());
	// Set<String> bAddsSD = Sets.difference(bt.getSupplementalData().elementSet(),
	// at.getSupplementalData().elementSet());
	//
	// if (bMissingSD.size() > 0) {
	// for (String key : bMissingSD) {
	// // if (checkTraitRelevance(elemName, at, key))
	// if (_outputMissing)
	// _log.info("\tMissing" + preFix + "trait: " + key + " in: " + elemName);
	// }
	// }
	// if (bAddsSD.size() > 0) {
	// for (String key : bAddsSD) {
	// // if (checkTraitRelevance(elemName, at, key))
	// if (_outputAdded)
	// _log.info("\tAdded" + preFix + "trait: " + key + " to: " + elemName);
	// }
	// }
	//
	// }
	//
	// } else {
	//
	// Preconditions.checkArgument(!(aTraits == null && bTraits == null), "Shouldn't be possible.");
	//
	// boolean output = false;
	//
	// ITrait[] singleTrait = null;
	// if (bTraits != null) {
	// singleTrait = bTraits;
	// if (_outputAdded) {
	// _log.info("\tNew" + preFix + "traits for: " + elemName + postFix);
	// output = true;
	// }
	// }
	// if (aTraits != null) {
	// singleTrait = aTraits;
	// if (_outputMissing) {
	// _log.info("\tMissing" + preFix + "traits for: " + elemName + postFix);
	// output = true;
	// }
	// }
	//
	// Preconditions.checkNotNull(singleTrait);
	//
	// for (ITrait trait : singleTrait) {
	// for (DATA_KINDS kind : trait.getData().elementSet())
	// if (output)
	// _log.info("\t\t" + kind + " ( " + trait.getData().count(kind) + " )");
	// for (String key : trait.getSupplementalData().elementSet())
	// if (output)
	// _log.info("\t\t" + key + " ( " + trait.getData().count(key) + " )");
	// }
	// }
	// }
	//
	// private boolean checkTraitRelevance(String elementName, ITrait trait, DATA_KINDS kind) {
	// boolean isRelevant = true;
	//
	// if (elementName.endsWith(".hashCode()"))
	// isRelevant = false;
	//
	// return isRelevant;
	// }

	private void checkPathCounts(Session sA, Session sB) {
		_log.info("CHECK PATH COUNTS");

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

				int sAcount = mA.getCalledBy().count(sA.getIdForElement(cbName));
				int sBcount = mB.getCalledBy().count(sB.getIdForElement(cbName));

				if (sAcount != sBcount) {
					if (_outputCountDifferences)
						_log.info("Differing count ( " + sAcount + " to: " + sBcount + " ) for call to: " + eName + " from: " + cbName);
				} else {
					_log.trace("Same count ( " + sAcount + " to: " + sBcount + " ) for call to: " + eName + " from: " + cbName);
				}

			}

			for (String cbName : missingCBNames) {
				// RFE: report path counts that have disappeared?
			}

			for (String cbName : newCBNames) {
				// RFE: report path counts for new paths?
			}

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
				ed.removedPath(cbName, eName);
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
				ed.removedPath(cbName, eName);
			}

		}

	}

	ImmutableSet<String> _newElementNames = null;
	ImmutableSet<String> _missingElementNames = null;

	private void checkForNewPaths(Session sA, Session sB, ExecutionDelta ed) {

		_log.info("CHECK FOR NEW PATHS");

		ImmutableSet<String> eAnames = ImmutableSet.copyOf(sA.getMethodNames());
		ImmutableSet<String> eBnames = ImmutableSet.copyOf(sB.getMethodNames());

		ImmutableSet<String> interNames = Sets.intersection(eAnames, eBnames).immutableCopy();
		ImmutableSet<String> missingNames = Sets.difference(eAnames, eBnames).immutableCopy();
		ImmutableSet<String> newNames = Sets.difference(eBnames, eAnames).immutableCopy();

		// RFE: consider edges missing from one set or other
		// check in edges that are present in both sets...
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

			}

			for (String cbName : newCBNames) {

				boolean newTarget = _newElementNames.contains(eName);
				boolean newSource = _newElementNames.contains(cbName);

				if (newSource && newTarget) {
					// source and target are new
					_log.warn("New path ( new source and target ) added from: " + cbName + " to: " + eName);
				} else if (newSource) {
					// new source, existing target
					_log.warn("New path ( new source ) added from: " + cbName + " to: " + eName);
				} else if (newTarget) {
					// new target, existing source
					_log.warn("New path ( new target ) added from: " + cbName + " to: " + eName);
				} else {
					// both exist already
					Preconditions.checkArgument(!newSource && !newTarget);
					_log.warn("New path added from: " + cbName + " to: " + eName);
				}
				ed.addedPath(cbName, eName);
			}
		}


		for (String eName : newNames) {

			// mA doesn't exist because these are new names
			MethodElement mA = null; // sA.getMethodForName(eName);
			MethodElement mB = sB.getMethodForName(eName);

			// Set<AbstractElement> mACBs = sA.getElementSet(mA.getCalledBy().elementSet());
			Set<AbstractElement> mBCBs = sB.getElementSet(mB.getCalledBy().elementSet());

			// Set<String> mACBnames = extractNames(mACBs);
			Set<String> mBCBnames = extractNames(mBCBs);

			for (String cbName : mBCBnames) {
				if (sA.hasIDForElement(cbName)) {
					_log.info("New path (with existing source) added from: " + cbName + " to: " + eName);
				} else {
					_log.info("New path (with new source and target) added from: " + cbName + " to: " + eName);
				}
				ed.addedPath(cbName, eName);
			}

		}
	}

	private void checkForMissingElements(Session sessionA, Session sessionB, ExecutionDelta ed) {

		_log.info("CHECK FOR MISSING ELEMENTS");

		ImmutableSet<String> missingNames = Sets.difference(sessionA.getElementNames(), sessionB.getElementNames()).immutableCopy();
		_missingElementNames = missingNames;
		for (String elemName : _missingElementNames) {
			_log.warn("Session B lacks element: " + elemName);
			ed.removedElement(elemName);
		}

	}

	private void checkForNewElements(Session sessionA, Session sessionB, ExecutionDelta ed) {

		_log.info("CHECK FOR NEW ELEMENTS");

		Vector<String> newElements = new Vector<String>();

		ImmutableSet<String> newNames = Sets.difference(sessionB.getElementNames(), sessionA.getElementNames()).immutableCopy();
		_newElementNames = newNames;

		for (String elemName : _newElementNames) {
			boolean fp = false;
			newElements.add(elemName);
		}

		Collections.sort(newElements);
		for (String elem : newElements) {
			_log.warn("Session B adds new element: " + elem);
			ed.addedElement(elem);
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

	private HashSet<String> _addedElements = new HashSet<String>();
	private HashSet<String> _removedElements = new HashSet<String>();
	private HashSet<Path> _addedPaths = new HashSet<Path>();
	private HashSet<Path> _removedPaths = new HashSet<Path>();
	private Session _sessionA;
	private Session _sessionB;

	ExecutionDelta(Session sessionA, Session sessionB) {
		_sessionA = sessionA;
		_sessionB = sessionB;
	}

	public Session get_sessionA() {
		return _sessionA;
	}

	public Session get_sessionB() {
		return _sessionB;
	}

	public void addedPath(String source, String target) {
		_addedPaths.add(new Path(source, target));
	}

	public void removedPath(String source, String target) {
		_removedPaths.add(new Path(source, target));
	}

	public void addedElement(String elementName) {
		_addedElements.add(elementName);
	}

	public void removedElement(String elementName) {
		_removedElements.add(elementName);
	}

	public HashSet<String> get_addedElements() {
		return _addedElements;
	}

	public HashSet<String> get_removedElements() {
		return _removedElements;
	}

	public HashSet<Path> get_addedPaths() {
		return _addedPaths;
	}

	public HashSet<Path> get_removedPaths() {
		return _removedPaths;
	}
}

class Path {
	final String _source;
	final String _target;
	final String _sig;

	Path(String source, String target) {
		Preconditions.checkNotNull(source);
		Preconditions.checkNotNull(target);
		_source = source;
		_target = target;
		_sig = source + " -> " + target;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Path)
			return ((Path) obj).toString().equals(toString());
		return false;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public String toString() {
		return _sig;
	}

	public String get_source() {
		return _source;
	}

	public String get_target() {
		return _target;
	}

	public boolean existsInSession(Session session) {
		boolean exists = false;

		if (session.hasIDForElement(_source) && session.hasIDForElement(_target)) {
			AbstractElement target = session.getElementForName(_target);

			if (target instanceof MethodElement) {
				MethodElement targetM = ((MethodElement) target);

				exists = targetM.getCalledBy().contains(session.getIdForElement(_source));

			} else if (target instanceof FieldElement) {
				Preconditions.checkNotNull(null, "not implemented yet");
			}
		}

//		System.out.println("Session contains path: " + toString() + " ? " + exists);
		return exists;
	}
}
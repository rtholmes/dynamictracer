package edu.washington.cse.longan;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;

import ca.lsmr.common.log.LSMRLogger;
import ca.lsmr.common.util.TimeUtility;
import edu.washington.cse.longan.io.SessionXMLReader;
import edu.washington.cse.longan.io.SessionXMLWriter;
import edu.washington.cse.longan.model.AbstractElement;
import edu.washington.cse.longan.model.FieldElement;
import edu.washington.cse.longan.model.FieldTraitContainer;
import edu.washington.cse.longan.model.ILonganConstants;
import edu.washington.cse.longan.model.MethodElement;
import edu.washington.cse.longan.model.ParamTraitContainer;
import edu.washington.cse.longan.model.ReturnTraitContainer;
import edu.washington.cse.longan.model.Session;
import edu.washington.cse.longan.trait.ExceptionTrait;
import edu.washington.cse.longan.trait.ITrait;
import edu.washington.cse.longan.trait.ITrait.DATA_KINDS;

public class ExecutionComparator {
	Logger _log = Logger.getLogger(this.getClass());
	private long _start;

	private boolean _outputInvocationDifferences = false;
	private boolean _outputCountDifferences = false;
	private boolean _outputMissing = false;
	private boolean _outputAdded = true;
	private boolean _outputCombined = true;
	private boolean _outputIndividual = false;
	private boolean _outputPaths = true;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// LOGGING
		LSMRLogger.startLog4J(true, ILonganConstantsPriv.LOGGING_LEVEL);

		long start = System.currentTimeMillis();

		String path = ILonganConstants.OUTPUT_PATH;

		Vector<String> executionFiles = new Vector<String>();
		boolean complete = true;
		if (complete) {
			// joda
			// executionFiles.add(path + "joda1283a.xml");
			// executionFiles.add(path + "joda1283b.xml");
			// executionFiles.add(path + "joda1311a.xml");
			// executionFiles.add(path + "joda1311b.xml");
			// executionFiles.add(path + "joda1322a.xml");
			// executionFiles.add(path + "joda1322b.xml");
			// executionFiles.add(path + "joda1371a.xml");
			// executionFiles.add(path + "joda1371b.xml");

			// executionFiles.add(path + "1311-1.xml");
			// executionFiles.add(path + "1311-2.xml");
			// executionFiles.add(path + "1311-3.xml");
			// executionFiles.add(path + "1311-4.xml");
			// executionFiles.add(path + "1311-5.xml");
			// executionFiles.add(path + "1311-6.xml");
			// executionFiles.add(path + "1311-7.xml");
			// executionFiles.add(path + "1311-8.xml");
			// executionFiles.add(path + "1311-9.xml");

			// matching pairs
			// executionFiles.add(path + "cntA.xml");
			// executionFiles.add(path + "cntB.xml");

			// different pairs
			// executionFiles.add(path + "cntC.xml");
			// executionFiles.add(path + "cntD.xml");

			// executionFiles.add(path + "cntE.xml");
			// executionFiles.add(path + "cntF.xml");
			// executionFiles.add(path + "cntG.xml");

			// executionFiles.add(path + "inhA.xml");
			// executionFiles.add(path + "inhB.xml");
			// executionFiles.add(path + "inhC.xml");
			// executionFiles.add(path + "inhD.xml");
			// executionFiles.add(path + "inhE.xml");

			// } else {
			// inh run
			// executionFiles.add(path + "6-30_1283.xml");
			// executionFiles.add(path + "6-30_1311.xml");
			// executionFiles.add(path + "6-30_1322.xml");
			// executionFiles.add(path + "6-30_1371.xml");

			// executionFiles.add(path + "inhA.xml");
			// executionFiles.add(path + "inhB.xml");
			// executionFiles.add(path + "latest.xml");
			// executionFiles.add(path + "latest.xml");

			// executionFiles.add(path + "log4j_v1_2_15_rc1.xml");
			// executionFiles.add(path + "log4j_v1_2_15_rc6.xml");

			// executionFiles.add(path + "joda1371c.xml");
			// executionFiles.add(path + "jibx-core.xml");
			// executionFiles.add(path + "kaching-api.xml");
			// executionFiles.add(path + "google-rfc-2445.xml");

			// executionFiles.add(path + "static_JodaTime_1322.xml");
			// executionFiles.add(path + "static_JodaTime_1322.xml");
			// executionFiles.add(path + "static_JodaTime_1371.xml");
			// executionFiles.add(path + "static_JodaTime_1371a.xml");
			// executionFiles.add(path + "static_JodaTime_1371b.xml");
			// executionFiles.add(path + "latestA.xml");
			// executionFiles.add(path + "latestB.xml");

			executionFiles.add(path + "longAnTestC-1a.xml");
			executionFiles.add(path + "longAnTestC-2a.xml");
		}
		ExecutionComparator ec = new ExecutionComparator();
		ec.start();

		ec.compare(executionFiles);

		// ec = new ExecutionComparator();
		// ec.readAndWrite(executionFiles.firstElement());

		ec.done(start);
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

	private void compare(Session sA, Session sB) {

		_log.info("Comparing: " + sA.getSessionName() + " to: " + sB.getSessionName());

		if (_outputCountDifferences)
			checkTotalMethodInvocationCounts(sA, sB);

		checkForMissingElements(sA, sB);
		checkForNewElements(sA, sB);

		if (_outputPaths) {
			checkForMissingPaths(sA, sB);
			checkForNewPaths(sA, sB);
			checkPathCounts(sA, sB);
		}

		checkParamDifferences(sA, sB);
		checkReturnDifferences(sA, sB);

		checkExceptionDifferences(sA, sB);
		checkFieldDifferences(sA, sB);

	}

	private void checkFieldDifferences(Session sA, Session sB) {
		ImmutableSet<String> eAnames = ImmutableSet.copyOf(sA.getFieldNames());
		ImmutableSet<String> eBnames = ImmutableSet.copyOf(sB.getFieldNames());

		if (_outputIndividual) {
			_log.info("CHECK FIELD GET DIFFERENCES");
			for (String eName : Sets.intersection(eAnames, eBnames)) {

				_log.debug("common field: " + eName);

				FieldElement fA = sA.getField(sA.getIdForElement(eName));
				FieldElement fB = sB.getField(sB.getIdForElement(eName));

				FieldTraitContainer ftgA = fA.getFieldGetTraitContainer();
				FieldTraitContainer ftgB = fB.getFieldGetTraitContainer();

				_log.debug("\tA gets: " + fA.getGetBy().size() + "\tsets: " + fA.getSetBy().size());
				_log.debug("\tB gets: " + fB.getGetBy().size() + "\tsets: " + fB.getSetBy().size());

				for (int getById : fA.getGetBy().elementSet()) {
					String elemName = sA.getElementNameForID(getById);

					ITrait traitsA[] = null;
					ITrait traitsB[] = null;

					traitsA = ftgA.getTraitsForCaller(getById);
					if (sB.hasIDForElement(elemName))
						traitsB = ftgB.getTraitsForCaller(sB.getIdForElement(elemName));

					// compare trait arrays

					compareTraitDifferences(traitsA, traitsB, fA.getName() + " referenced by: " + elemName);
				}

				for (int getById : fB.getGetBy().elementSet()) {
					String elemName = sB.getElementNameForID(getById);

					ITrait traitsB[] = null;
					ITrait traitsA[] = null;

					traitsB = ftgB.getTraitsForCaller(getById);
					if (sA.hasIDForElement(elemName))
						traitsA = ftgA.getTraitsForCaller(sA.getIdForElement(elemName));

					// compare trait arrays
					compareTraitDifferences(traitsA, traitsB, fB.getName() + " referenced by: " + elemName);
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

		if (_outputIndividual) {
			_log.info("CHECK FIELD SET DIFFERENCES");
			for (String eName : Sets.intersection(eAnames, eBnames)) {

				_log.debug("common field: " + eName);

				FieldElement fA = sA.getField(sA.getIdForElement(eName));
				FieldElement fB = sB.getField(sB.getIdForElement(eName));

				FieldTraitContainer ftgA = fA.getFieldSetTraitContainer();
				FieldTraitContainer ftgB = fB.getFieldSetTraitContainer();

				_log.debug("\tgets: " + fA.getGetBy().size() + "\tsets: " + fA.getSetBy().size());
				for (int setById : fA.getSetBy().elementSet()) {
					String elemName = sA.getElementNameForID(setById);

					ITrait traitsA[] = null;
					ITrait traitsB[] = null;

					traitsA = ftgA.getTraitsForCaller(setById);
					if (sB.hasIDForElement(elemName))
						traitsB = ftgB.getTraitsForCaller(sB.getIdForElement(elemName));

					// compare trait arrays
					compareTraitDifferences(traitsA, traitsB, fA.getName() + " referenced by: " + elemName);
				}

				for (int setById : fB.getSetBy().elementSet()) {
					String elemName = sB.getElementNameForID(setById);

					ITrait traitsB[] = null;
					ITrait traitsA[] = null;

					traitsB = ftgB.getTraitsForCaller(setById);
					if (sA.hasIDForElement(elemName))
						traitsA = ftgA.getTraitsForCaller(sA.getIdForElement(elemName));

					// compare trait arrays
					compareTraitDifferences(traitsA, traitsB, fB.getName() + " referenced by: " + elemName);
				}

			}
		}

		if (_outputCombined) {
			_log.info("CHECK COMBINED FIELD GET DIFFERENCES");
			for (String eName : Sets.intersection(eAnames, eBnames)) {
				_log.debug("common field: " + eName);

				FieldElement fA = sA.getField(sA.getIdForElement(eName));
				FieldElement fB = sB.getField(sB.getIdForElement(eName));

				FieldTraitContainer ftgA = fA.getFieldGetTraitContainer();
				FieldTraitContainer ftgB = fB.getFieldGetTraitContainer();

				if (ftgA != null && ftgB != null) {
					compareTraitDifferences(ftgA.getTraitsCollapsed(), ftgB.getTraitsCollapsed(), fA.getName());
				} else {
					// this won't happen for dynamic traces but might for static
				}
			}
		}

		if (_outputCombined) {
			_log.info("CHECK COMBINED FIELD SET DIFFERENCES");
			for (String eName : Sets.intersection(eAnames, eBnames)) {
				_log.debug("common field: " + eName);

				FieldElement fA = sA.getField(sA.getIdForElement(eName));
				FieldElement fB = sB.getField(sB.getIdForElement(eName));

				FieldTraitContainer ftgA = fA.getFieldSetTraitContainer();
				FieldTraitContainer ftgB = fB.getFieldSetTraitContainer();
				if (ftgA != null && ftgB != null) {
					compareTraitDifferences(ftgA.getTraitsCollapsed(), ftgB.getTraitsCollapsed(), fA.getName());
				} else {
					// this won't happen for dynamic traces but might for static
				}
			}
		}
	}

	private void checkExceptionDifferences(Session sA, Session sB) {
		_log.info("CHECK EXCEPTION DIFFERENCES");

		ImmutableSet<String> eAnames = ImmutableSet.copyOf(sA.getMethodNames());
		ImmutableSet<String> eBnames = ImmutableSet.copyOf(sB.getMethodNames());

		// we can be a lot more precise here; eg., we could note exceptions that
		// have different sources (rather than whole paths) or are caught in different locations
		for (String eName : Sets.intersection(eAnames, eBnames)) {

			MethodElement mA = sA.getMethod(sA.getIdForElement(eName));
			MethodElement mB = sB.getMethod(sB.getIdForElement(eName));

			for (ExceptionTrait etA : mA.getExceptions()) {
				boolean matched = false;
				for (ExceptionTrait etB : mB.getExceptions()) {
					if (ExceptionTrait.equals(etA, etB, sA, sB)) {
						matched = true;
					}
				}
				if (matched) {

				} else {
					if (_outputMissing)
						_log.info("Exception disappeared from: " + mA + " ex: " + etA);
				}
			}

			for (ExceptionTrait etB : mB.getExceptions()) {
				boolean matched = false;
				for (ExceptionTrait etA : mA.getExceptions()) {
					if (ExceptionTrait.equals(etA, etB, sA, sB)) {
						matched = true;
					}
				}
				if (matched) {

				} else {
					if (_outputAdded)
						_log.info("Exception added to: " + mB + " ex: " + etB);
				}
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

	private void checkReturnDifferences(Session sA, Session sB) {

		_log.info("CHECK COMBINED RETURN DIFFERENCES");

		ImmutableSet<String> eAnames = ImmutableSet.copyOf(sA.getMethodNames());
		ImmutableSet<String> eBnames = ImmutableSet.copyOf(sB.getMethodNames());

		for (String eName : Sets.intersection(eAnames, eBnames)) {

			MethodElement mA = sA.getMethod(sA.getIdForElement(eName));
			MethodElement mB = sB.getMethod(sB.getIdForElement(eName));

			Preconditions.checkNotNull(mA);
			Preconditions.checkNotNull(mB);

			compareCombinedReturnTraits(mA, mB);
		}

		if (_outputIndividual) {
			_log.info("CHECK CALLEDBY RETURN DIFFERENCES");

			for (String eName : Sets.intersection(eAnames, eBnames)) {

				MethodElement mA = sA.getMethod(sA.getIdForElement(eName));
				MethodElement mB = sB.getMethod(sB.getIdForElement(eName));

				Preconditions.checkNotNull(mA);
				Preconditions.checkNotNull(mB);

				for (int mACBid : mA.getCalledBy().elementSet()) {
					String callerName = sA.getElementNameForID(mACBid);

					compareCalledByReturnTraits(sA, sB, mA, mB, callerName);

				}
			}
		}
	}

	private void compareCombinedReturnTraits(MethodElement mA, MethodElement mB) {
		ReturnTraitContainer mArtc = mA.getReturnTraitContainer();
		ReturnTraitContainer mBrtc = mB.getReturnTraitContainer();

		if (mArtc != null && mBrtc != null) {
			compareTraitDifferences(mArtc.getTraitsCollapsed(), mBrtc.getTraitsCollapsed(), mA.getName(), null, -1, true);
		} else {
			// this won't happen for dynamic traces but it is ok for static traces
		}
	}

	private void compareCalledByReturnTraits(Session sA, Session sB, MethodElement mA, MethodElement mB, String calledByName) {

		ReturnTraitContainer mArtc = mA.getReturnTraitContainer();
		ReturnTraitContainer mBrtc = mB.getReturnTraitContainer();

		ITrait[] aTraits = null;
		if (sA.hasIDForElement(calledByName))
			aTraits = mArtc.getTraitsForCaller(sA.getIdForElement(calledByName));

		ITrait[] bTraits = null;
		if (sB.hasIDForElement(calledByName))
			bTraits = mBrtc.getTraitsForCaller(sB.getIdForElement(calledByName));

		compareTraitDifferences(aTraits, bTraits, mA.getName(), calledByName, -1, false);
	}

	private void checkParamDifferences(Session sA, Session sB) {

		ImmutableSet<String> eAnames = ImmutableSet.copyOf(sA.getMethodNames());
		ImmutableSet<String> eBnames = ImmutableSet.copyOf(sB.getMethodNames());

		if (_outputCombined) {
			_log.info("CHECK COMBINED PARAM DIFFERENCES");

			for (String eName : Sets.intersection(eAnames, eBnames)) {

				MethodElement mA = sA.getMethod(sA.getIdForElement(eName));
				MethodElement mB = sB.getMethod(sB.getIdForElement(eName));

				Preconditions.checkNotNull(mA);
				Preconditions.checkNotNull(mB);

				Vector<ParamTraitContainer> mAptcs = mA.getParamTraitContainers();
				Vector<ParamTraitContainer> mBptcs = mB.getParamTraitContainers();

				Preconditions.checkArgument(mAptcs.size() == mBptcs.size());

				for (int j = 0; j < mAptcs.size(); j++) {
					ParamTraitContainer mAptc = mAptcs.get(j);
					ParamTraitContainer mBptc = mBptcs.get(j);

					Preconditions.checkArgument(mAptc.getClass().equals(mBptc.getClass()));

					compareTraitDifferences(mAptc.getTraitsCollapsed(), mBptc.getTraitsCollapsed(), mA.getName(), null, j, true);
				}
			}
		}

		if (_outputIndividual) {
			_log.info("CHECK CALLEDBY PARAM DIFFERENCES");

			for (String eName : Sets.intersection(eAnames, eBnames)) {

				MethodElement mA = sA.getMethod(sA.getIdForElement(eName));
				MethodElement mB = sB.getMethod(sB.getIdForElement(eName));

				Preconditions.checkNotNull(mA);
				Preconditions.checkNotNull(mB);

				for (int mACBid : mA.getCalledBy().elementSet()) {
					String callerName = sA.getElementNameForID(mACBid);

					compareCalledByParamTraits(sA, sB, mA, mB, callerName);

				}
			}
		}
	}

	private void compareCalledByParamTraits(Session sA, Session sB, MethodElement mA, MethodElement mB, String calledByName) {

		Vector<ParamTraitContainer> mAptcs = mA.getParamTraitContainers();
		Vector<ParamTraitContainer> mBptcs = mB.getParamTraitContainers();

		Preconditions.checkArgument(mAptcs.size() == mBptcs.size());

		for (int j = 0; j < mAptcs.size(); j++) {
			ParamTraitContainer mAptc = mAptcs.get(j);
			ParamTraitContainer mBptc = mBptcs.get(j);

			Preconditions.checkArgument(mAptc.getClass().equals(mBptc.getClass()));

			ITrait[] aTraits = null;
			if (sA.hasIDForElement(calledByName))
				aTraits = mAptc.getTraitsForCaller(sA.getIdForElement(calledByName));

			ITrait[] bTraits = null;
			if (sB.hasIDForElement(calledByName))
				bTraits = mBptc.getTraitsForCaller(sB.getIdForElement(calledByName));

			compareTraitDifferences(aTraits, bTraits, mA.getName(), calledByName, j, false);
		}
	}

	// private void compareTraitDifferences(ITrait[] aTraits, ITrait[] bTraits, String preFix, String postFix) {
	//		
	// }
	//	
	// private void compareTraitDifferences(ITrait[] aTraits, ITrait[] bTraits) {
	//		
	// }

	private void compareTraitDifferences(ITrait[] aTraits, ITrait[] bTraits, String elemName) {

		if (aTraits == null && bTraits == null) {
			return;
		}

		if (aTraits != null && bTraits != null && aTraits.length > 0 && bTraits.length > 0) {

			Preconditions.checkArgument(aTraits.length == bTraits.length);
			for (int i = 0; i < aTraits.length; i++) {
				ITrait at = aTraits[i];
				ITrait bt = bTraits[i];

				Preconditions.checkArgument(at.getClass().equals(bt.getClass()));

				Set<DATA_KINDS> bMissing = Sets.difference(at.getData().elementSet(), bt.getData().elementSet());
				Set<DATA_KINDS> bAdds = Sets.difference(bt.getData().elementSet(), at.getData().elementSet());

				if (bMissing.size() > 0) {
					for (DATA_KINDS kind : bMissing) {
						if (checkTraitRelevance(elemName, at, kind))
							if (_outputMissing)
								_log.info("\tMissing trait: " + kind + " ( " + at.getData().count(kind) + " )" + " in: " + elemName);
					}
				}
				if (bAdds.size() > 0) {
					for (DATA_KINDS kind : bAdds) {
						if (checkTraitRelevance(elemName, bt, kind))
							if (_outputAdded)
								_log.info("\tAdded trait: " + kind + " ( " + bt.getData().count(kind) + " )" + " to: " + elemName);
					}
				}

				Set<String> bMissingSD = Sets.difference(at.getSupplementalData().elementSet(), bt.getSupplementalData().elementSet());
				Set<String> bAddsSD = Sets.difference(bt.getSupplementalData().elementSet(), at.getSupplementalData().elementSet());

				if (bMissingSD.size() > 0) {
					for (String key : bMissingSD) {
						// if (checkTraitRelevance(elemName, at, key))
						if (_outputMissing)
							_log.info("\tMissing trait: " + key + " in: " + elemName);
					}
				}
				if (bAddsSD.size() > 0) {
					for (String key : bAddsSD) {
						// if (checkTraitRelevance(elemName, at, key))
						if (_outputAdded)
							_log.info("\tAdded trait: " + key + " to: " + elemName);
					}
				}

			}

		} else {

			Preconditions.checkArgument(!(aTraits == null && bTraits == null), "Shouldn't be possible.");

			if (aTraits != null && bTraits != null && aTraits.length < 1 && bTraits.length < 1) {
				// if they're both 0 then there's nothing missing and nothing gained
			} else {
				boolean output = false;
				ITrait[] singleTrait = null;
				if (bTraits != null && bTraits.length > 0) {
					singleTrait = bTraits;
					if (_outputAdded) {
						_log.info("\tNew traits for: " + elemName);
						output = true;
					}
				}
				if (aTraits != null && aTraits.length > 0) {
					singleTrait = aTraits;
					if (_outputMissing) {
						_log.info("\tMissing traits for: " + elemName);
						output = true;
					}
				}

				Preconditions.checkNotNull(singleTrait);

				for (ITrait trait : singleTrait) {
					for (DATA_KINDS kind : trait.getData().elementSet())
						if (output)
							_log.info("\t\t" + kind + " ( " + trait.getData().count(kind) + " )");
					for (String key : trait.getSupplementalData().elementSet())
						if (output)
							_log.info("\t\t" + key + " ( " + trait.getData().count(key) + " )");
				}
			}
		}
	}

	private void compareTraitDifferences(ITrait[] aTraits, ITrait[] bTraits, String elemName, String calledByName, int paramIndex, boolean combined) {

		if (aTraits == null && bTraits == null) {
			return;
		}

		String preFix = "";
		if (paramIndex >= 0)
			preFix = " param ( " + paramIndex + " ) ";
		else
			preFix = " return ";

		String postFix = "";
		if (combined)
			postFix = "";
		else
			postFix = " when called by: " + calledByName;

		if (aTraits != null && bTraits != null) {

			if (aTraits.length != bTraits.length) {
				System.out.println("");
			}
			Preconditions.checkArgument(aTraits.length == bTraits.length);
			for (int i = 0; i < aTraits.length; i++) {
				ITrait at = aTraits[i];
				ITrait bt = bTraits[i];

				Preconditions.checkArgument(at.getClass().equals(bt.getClass()));

				Set<DATA_KINDS> bMissing = Sets.difference(at.getData().elementSet(), bt.getData().elementSet());
				Set<DATA_KINDS> bAdds = Sets.difference(bt.getData().elementSet(), at.getData().elementSet());

				if (bMissing.size() > 0) {
					for (DATA_KINDS kind : bMissing) {
						if (checkTraitRelevance(elemName, at, kind))
							if (_outputMissing)
								_log.info("\tMissing" + preFix + "trait: " + kind + " in: " + elemName + postFix);
					}
				}
				if (bAdds.size() > 0) {
					for (DATA_KINDS kind : bAdds) {
						if (checkTraitRelevance(elemName, at, kind))
							if (_outputAdded)
								_log.info("\tAdded" + preFix + "trait: " + kind + " to: " + elemName + postFix);
					}
				}

				Set<String> bMissingSD = Sets.difference(at.getSupplementalData().elementSet(), bt.getSupplementalData().elementSet());
				Set<String> bAddsSD = Sets.difference(bt.getSupplementalData().elementSet(), at.getSupplementalData().elementSet());

				if (bMissingSD.size() > 0) {
					for (String key : bMissingSD) {
						// if (checkTraitRelevance(elemName, at, key))
						if (_outputMissing)
							_log.info("\tMissing" + preFix + "trait: " + key + " in: " + elemName);
					}
				}
				if (bAddsSD.size() > 0) {
					for (String key : bAddsSD) {
						// if (checkTraitRelevance(elemName, at, key))
						if (_outputAdded)
							_log.info("\tAdded" + preFix + "trait: " + key + " to: " + elemName);
					}
				}

			}

		} else {

			Preconditions.checkArgument(!(aTraits == null && bTraits == null), "Shouldn't be possible.");

			boolean output = false;

			ITrait[] singleTrait = null;
			if (bTraits != null) {
				singleTrait = bTraits;
				if (_outputAdded) {
					_log.info("\tNew" + preFix + "traits for: " + elemName + postFix);
					output = true;
				}
			}
			if (aTraits != null) {
				singleTrait = aTraits;
				if (_outputMissing) {
					_log.info("\tMissing" + preFix + "traits for: " + elemName + postFix);
					output = true;
				}
			}

			Preconditions.checkNotNull(singleTrait);

			for (ITrait trait : singleTrait) {
				for (DATA_KINDS kind : trait.getData().elementSet())
					if (output)
						_log.info("\t\t" + kind + " ( " + trait.getData().count(kind) + " )");
				for (String key : trait.getSupplementalData().elementSet())
					if (output)
						_log.info("\t\t" + key + " ( " + trait.getData().count(key) + " )");
			}
		}
	}

	private boolean checkTraitRelevance(String elementName, ITrait trait, DATA_KINDS kind) {
		boolean isRelevant = true;

		if (elementName.endsWith(".hashCode()"))
			isRelevant = false;

		return isRelevant;
	}

	private void checkPathCounts(Session sA, Session sB) {
		_log.info("CHECK PATH COUNTS");

		ImmutableSet<String> eAnames = ImmutableSet.copyOf(sA.getElementNames());
		ImmutableSet<String> eBnames = ImmutableSet.copyOf(sB.getElementNames());

		ImmutableSet<String> interNames = Sets.intersection(eAnames, eBnames).immutableCopy();
		ImmutableSet<String> missingNames = Sets.difference(eAnames, eBnames).immutableCopy();
		ImmutableSet<String> newNames = Sets.difference(eBnames, eAnames).immutableCopy();

		for (String eName : interNames) {

			AbstractElement mAae = sA.getElementForName(eName);
			AbstractElement mBae = sB.getElementForName(eName);

			if (mAae instanceof MethodElement && mBae instanceof MethodElement) {

				MethodElement mA = (MethodElement) mAae;
				MethodElement mB = (MethodElement) mBae;

				Set<AbstractElement> mACBs = sA.getElementSet(((MethodElement) mA).getCalledBy().elementSet());
				Set<AbstractElement> mBCBs = sB.getElementSet(((MethodElement) mB).getCalledBy().elementSet());

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
	}

	private Set<String> extractNames(Set<AbstractElement> elements) {
		HashSet<String> set = new HashSet<String>();
		for (AbstractElement ae : elements)
			set.add(ae.getName());

		return set;
	}

	private void checkForMissingPaths(Session sA, Session sB) {

		_log.info("CHECK FOR MISSING PATHS");

		// XXX: include fields
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
//			Set<AbstractElement> mBCBs = sB.getElementSet(mB.getCalledBy().elementSet());

			 Set<String> mACBnames = extractNames(mACBs);
//			Set<String> mBCBnames = extractNames(mBCBs);

			for (String cbName : mACBnames) {
				if (sB.hasIDForElement(cbName)) {
					_log.info("Path missing (with existing source) from: " + cbName + " to: " + eName);
				} else {
					_log.info("Path missing (with missing source and target) from: " + cbName + " to: " + eName);
				}
			}

		}
		

	}

	ImmutableSet<String> _newElementNames = null;
	ImmutableSet<String> _missingElementNames = null;

	private void checkForNewPaths(Session sA, Session sB) {

		_log.info("CHECK FOR NEW PATHS");
		// XXX: include fields
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
				// common paths, no need to report
			}

			for (String cbName : missingCBNames) {
				_log.info("does this ever happen?");
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
			}

		}

	}

	private void checkForMissingElements(Session sessionA, Session sessionB) {

		_log.info("CHECK FOR MISSING ELEMENTS");

		ImmutableSet<String> missingNames = Sets.difference(sessionA.getElementNames(), sessionB.getElementNames()).immutableCopy();
		_missingElementNames = missingNames;
		for (String elemName : _missingElementNames) {
			_log.warn("Session B lacks element: " + elemName);
		}

	}

	private void checkForNewElements(Session sessionA, Session sessionB) {

		_log.info("CHECK FOR NEW ELEMENTS");

		ImmutableSet<String> newNames = Sets.difference(sessionB.getElementNames(), sessionA.getElementNames()).immutableCopy();
		_newElementNames = newNames;
		for (String elemName : _newElementNames) {
			boolean fp = false;
			_log.warn("Session B adds new element: " + elemName);
		}

	}

}

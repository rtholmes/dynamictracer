package edu.washington.cse.longan;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import ca.lsmr.common.log.LSMRLogger;
import ca.lsmr.common.util.TimeUtility;
import edu.washington.cse.longan.io.SessionXMLReader;
import edu.washington.cse.longan.io.SessionXMLWriter;
import edu.washington.cse.longan.model.ILonganConstants;
import edu.washington.cse.longan.model.MethodElement;
import edu.washington.cse.longan.model.ParamTraitContainer;
import edu.washington.cse.longan.model.ReturnTraitContainer;
import edu.washington.cse.longan.model.Session;
import edu.washington.cse.longan.trait.ITrait;
import edu.washington.cse.longan.trait.ITrait.DATA_KINDS;

public class ExecutionComparator {
	Logger _log = Logger.getLogger(this.getClass());
	private long _start;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		LSMRLogger.startLog4J(true, ILonganConstants.LOGGING_LEVEL);

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

			// BUG: NPE
			executionFiles.add(path + "1311-1.xml");
			executionFiles.add(path + "1322-1.xml");
			// } else {
			// inh run
			// executionFiles.add(path + "inhTesta.xml");
			// executionFiles.add(path + "inhTestb.xml");
		}
		ExecutionComparator ec = new ExecutionComparator();
		ec.start();

		ec.compare(executionFiles);

		// ec = new ExecutionComparator();
		// ec.readAndWrite(executionFiles.firstElement());

		ec.done();
	}

	private void start() {
		_start = System.currentTimeMillis();

	}

	private void done() {
		_log.info("Done in: " + TimeUtility.msToHumanReadableDelta(_start));
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

			} else {
				previousSession = currentSession;
			}
		}

	}

	private void compare(Session sA, Session sB) {

		checkForMissingElements(sA, sB);
		checkForNewElements(sA, sB);

		checkForMissingPaths(sA, sB);
		checkForNewPaths(sA, sB);
		checkPathCounts(sA, sB);

		checkParamDifferences(sA, sB);
		checkReturnDifferences(sA, sB);
	}

	private void checkReturnDifferences(Session sA, Session sB) {

		_log.info("CHECK RETURN DIFFERENCES");

		ImmutableSet<String> eAnames = ImmutableSet.copyOf(sA.getElementNames());
		ImmutableSet<String> eBnames = ImmutableSet.copyOf(sB.getElementNames());

		for (String eName : Sets.intersection(eAnames, eBnames)) {

			MethodElement mA = sA.getMethod(sA.getIdForElement(eName));
			MethodElement mB = sB.getMethod(sB.getIdForElement(eName));

			Preconditions.checkNotNull(mA);
			Preconditions.checkNotNull(mB);

			for (int mACBid : mA.getCalledBy().elementSet()) {
				String callerName = sA.getElementNameForID(mACBid);

				compareReturnTraits(sA, sB, mA, mB, callerName);

			}
		}
	}

	private void compareReturnTraits(Session sA, Session sB, MethodElement mA, MethodElement mB, String calledByName) {

		ReturnTraitContainer mArtc = mA.getReturnTraitContainer();
		ReturnTraitContainer mBrtc = mB.getReturnTraitContainer();

		ITrait[] aTraits = null;
		if (sA.hasIDForElement(calledByName))
			aTraits = mArtc.getTraitsForCaller(sA.getIdForElement(calledByName));

		ITrait[] bTraits = null;
		if (sB.hasIDForElement(calledByName))
			bTraits = mBrtc.getTraitsForCaller(sB.getIdForElement(calledByName));

		compareTraitDifferences(aTraits, bTraits, mA.getName(), calledByName, -1);
	}

	private void checkParamDifferences(Session sA, Session sB) {
		_log.info("CHECK PARAM DIFFERENCES");

		ImmutableSet<String> eAnames = ImmutableSet.copyOf(sA.getElementNames());
		ImmutableSet<String> eBnames = ImmutableSet.copyOf(sB.getElementNames());

		for (String eName : Sets.intersection(eAnames, eBnames)) {

			MethodElement mA = sA.getMethod(sA.getIdForElement(eName));
			MethodElement mB = sB.getMethod(sB.getIdForElement(eName));

			Preconditions.checkNotNull(mA);
			Preconditions.checkNotNull(mB);

			for (int mACBid : mA.getCalledBy().elementSet()) {
				String callerName = sA.getElementNameForID(mACBid);

				compareParamTraits(sA, sB, mA, mB, callerName);

			}
		}
	}

	private void compareParamTraits(Session sA, Session sB, MethodElement mA, MethodElement mB, String calledByName) {

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

			compareTraitDifferences(aTraits, bTraits, mA.getName(), calledByName, j);
		}
	}

	private void compareTraitDifferences(ITrait[] aTraits, ITrait[] bTraits, String elemName, String calledByName, int paramIndex) {

		if (aTraits == null && bTraits == null) {
			return;
		}

		String preamble = "";
		if (paramIndex >= 0)
			preamble = " param ( " + paramIndex + " ) ";
		else
			preamble = " return ";

		if (aTraits != null && bTraits != null) {

			Preconditions.checkArgument(aTraits.length == bTraits.length);
			for (int i = 0; i < aTraits.length; i++) {
				ITrait at = aTraits[i];
				ITrait bt = bTraits[i];

				Preconditions.checkArgument(at.getClass().equals(bt.getClass()));

				Set<DATA_KINDS> bMissing = Sets.difference(at.getData().elementSet(), bt.getData().elementSet());
				Set<DATA_KINDS> bAdds = Sets.difference(bt.getData().elementSet(), at.getData().elementSet());

				if (bMissing.size() > 0) {
					for (DATA_KINDS kind : bMissing) {
						_log.info("\tMissing" + preamble + "trait: " + kind + " in: " + elemName + " when called by: " + calledByName);
					}
				}
				if (bAdds.size() > 0) {
					for (DATA_KINDS kind : bAdds) {
						_log.info("\tAdded" + preamble + "trait: " + kind + " to: " + elemName + " when called by: " + calledByName);
					}
				}

			}

		} else {

			Preconditions.checkArgument(!(aTraits == null && bTraits == null), "Shouldn't be possible.");

			ITrait[] singleTrait = null;
			if (bTraits != null) {
				singleTrait = bTraits;
				_log.info("\tNew" + preamble + "traits for: " + elemName + " when called by: " + calledByName);
			}
			if (aTraits != null) {
				singleTrait = aTraits;
				_log.info("\tMissing" + preamble + "traits for: " + elemName + " when called by: " + calledByName);
			}

			Preconditions.checkNotNull(singleTrait);

			for (ITrait trait : singleTrait) {
				for (DATA_KINDS kind : trait.getData().elementSet())
					_log.info("\t\t" + kind + " ( " + trait.getData().count(kind) + " )");
				for (String key : trait.getSupplementalData().elementSet())
					_log.info("\t\t" + key + " ( " + trait.getData().count(key) + " )");
			}
		}
	}

	private void checkPathCounts(Session sA, Session sB) {
		_log.info("CHECK PATH COUNTS");

		ImmutableSet<String> eAnames = ImmutableSet.copyOf(sA.getElementNames());
		ImmutableSet<String> eBnames = ImmutableSet.copyOf(sB.getElementNames());

		ImmutableSet<String> interNames = Sets.intersection(eAnames, eBnames).immutableCopy();
		ImmutableSet<String> missingNames = Sets.difference(eAnames, eBnames).immutableCopy();
		ImmutableSet<String> newNames = Sets.difference(eBnames, eAnames).immutableCopy();

		for (String eName : interNames) {

			MethodElement mA = sA.getElementForName(eName);
			MethodElement mB = sB.getElementForName(eName);

			Set<MethodElement> mACBs = sA.getElementSet(mA.getCalledBy().elementSet());
			Set<MethodElement> mBCBs = sB.getElementSet(mB.getCalledBy().elementSet());

			Set<String> mACBnames = extractNames(mACBs);
			Set<String> mBCBnames = extractNames(mBCBs);

			ImmutableSet<String> interCBNames = Sets.intersection(mACBnames, mBCBnames).immutableCopy();
			ImmutableSet<String> missingCBNames = Sets.difference(mACBnames, mBCBnames).immutableCopy();
			ImmutableSet<String> newCBNames = Sets.difference(mBCBnames, mACBnames).immutableCopy();

			for (String cbName : interCBNames) {

				int sAcount = mA.getCalledBy().count(sA.getIdForElement(cbName));
				int sBcount = mB.getCalledBy().count(sB.getIdForElement(cbName));

				if (sAcount != sBcount) {
					_log.info("Differing count ( " + sAcount + " to: " + sBcount + " ) for call to: " + eName + " from: " + cbName);
				} else {
					_log.debug("Same count ( " + sAcount + " to: " + sBcount + " ) for call to: " + eName + " from: " + cbName);
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

	private Set<String> extractNames(Set<MethodElement> mElements) {
		HashSet<String> set = new HashSet<String>();
		for (MethodElement me : mElements)
			set.add(me.getName());

		return set;
	}

	private void checkForMissingPaths(Session sA, Session sB) {

		_log.info("CHECK FOR MISSING PATHS");

		ImmutableSet<String> eAnames = ImmutableSet.copyOf(sA.getElementNames());
		ImmutableSet<String> eBnames = ImmutableSet.copyOf(sB.getElementNames());

		ImmutableSet<String> interNames = Sets.intersection(eAnames, eBnames).immutableCopy();
		ImmutableSet<String> missingNames = Sets.difference(eAnames, eBnames).immutableCopy();
		ImmutableSet<String> newNames = Sets.difference(eBnames, eAnames).immutableCopy();

		for (String eName : interNames) {

			MethodElement mA = sA.getElementForName(eName);
			MethodElement mB = sB.getElementForName(eName);

			Set<MethodElement> mACBs = sA.getElementSet(mA.getCalledBy().elementSet());
			Set<MethodElement> mBCBs = sB.getElementSet(mB.getCalledBy().elementSet());

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

	}

	ImmutableSet<String> _newElementNames = null;
	ImmutableSet<String> _missingElementNames = null;

	private void checkForNewPaths(Session sA, Session sB) {

		_log.info("CHECK FOR NEW PATHS");

		ImmutableSet<String> eAnames = ImmutableSet.copyOf(sA.getElementNames());
		ImmutableSet<String> eBnames = ImmutableSet.copyOf(sB.getElementNames());

		ImmutableSet<String> interNames = Sets.intersection(eAnames, eBnames).immutableCopy();
		ImmutableSet<String> missingNames = Sets.difference(eAnames, eBnames).immutableCopy();
		ImmutableSet<String> newNames = Sets.difference(eBnames, eAnames).immutableCopy();

		// RFE: consider edges missing from one set or other
		// check in edges that are present in both sets...
		for (String eName : interNames) {

			MethodElement mA = sA.getElementForName(eName);
			MethodElement mB = sB.getElementForName(eName);

			Set<MethodElement> mACBs = sA.getElementSet(mA.getCalledBy().elementSet());
			Set<MethodElement> mBCBs = sB.getElementSet(mB.getCalledBy().elementSet());

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
			}

		}
		// for (MethodElement mA : sA.getMethods()) {
		//
		// if (sB.hasIDForElement(mA.getName())) {
		// MethodElement mB = sB.getMethod(sB.getIdForElement(mA.getName()));
		//
		// // BUG: broken, comparing on element ids unsafe safe
		// Set<Integer> diff = Sets.difference(mB.getCalledBy().elementSet(), mA.getCalledBy().elementSet());
		// if (diff.size() > 0) {
		// _log.warn("New path(s) added to: " + mA.getName());
		// for (int bId : diff) {
		// _log.info("\tCall added from: " + sB.getMethod(bId).getName());
		// }
		// } else {
		// // no new paths
		// }
		//
		// } else {
		// _log.debug("Missing element: " + mA.getName());
		// }
		// }

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

package edu.washington.cse.longan;

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
import edu.washington.cse.longan.model.Session;

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
			executionFiles.add(path + "joda1371a.xml");
			executionFiles.add(path + "joda1371b.xml");
		} else {
			// inh run
			executionFiles.add(path + "inhTesta.xml");
			executionFiles.add(path + "inhTestb.xml");
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
		
		ImmutableSet<String> eAnames = ImmutableSet.copyOf(sA.getElementNames());
		ImmutableSet<String> eBnames = ImmutableSet.copyOf(sB.getElementNames());

		for (String eName : Sets.intersection(eAnames, eBnames)) {
			
			MethodElement mA = sA.getMethod(sA.getIdForElement(eName));
			MethodElement mB = sB.getMethod(sB.getIdForElement(eName));
			
			Preconditions.checkNotNull(mA);
			Preconditions.checkNotNull(mB);

			
			
		}

	}

	private void checkParamDifferences(Session sA, Session sB) {
		// TODO Auto-generated method stub

	}

	private void checkPathCounts(Session sA, Session sB) {
		_log.info("CHECK PATH COUNTS");

		// for every method in sA ...
		for (MethodElement mA : sA.getMethods()) {

			// that is also in sB
			if (sB.hasIDForElement(mA.getName())) {
				int mBid = sB.getIdForElement(mA.getName());
				MethodElement mB = sB.getMethod(mBid);

				// find the method caller elements that both mA and mB call in common
				Set<MethodElement> inter = Sets.intersection(sA.getElementSet(mA.getCalledBy().elementSet()), sB
						.getElementSet(mB.getCalledBy().elementSet()));

				// consider each of the calledbys in the intersection
				for (MethodElement mACBinter : inter) {

					// A id from the intersection
					int mACBid = mACBinter.getId();
					// B id equivalent to the same element as the A id
					int mBCBid = sB.getIdForElement(mACBinter.getName());

					// int calledByCount = session.getMethod(method.getId()).getCalledBy().count(caller);

					int sAcount = mA.getCalledBy().count(mACBid);
					int sBcount = mB.getCalledBy().count(mBCBid);

					if (sAcount != sBcount) {
						_log.warn("Differing count ( " + sAcount + " to: " + sBcount + " ) for: "
								+ sA.getMethod(mACBid).getName() + " calling: " + mA.getName());
					} else {
						_log.debug("Same count ( " + sAcount + " to: " + sBcount + " ) for: "
								+ sA.getMethod(mACBid).getName() + " calling: " + mA.getName());
					}
				}
			} else {
				_log.debug("Missing element: " + mA.getName());
			}
		}
	}

	private void checkForMissingPaths(Session sA, Session sB) {

		_log.info("CHECK FOR MISSING PATHS");
		for (MethodElement mA : sA.getMethods()) {

			if (sB.hasIDForElement(mA.getName())) {
				MethodElement mB = sB.getMethod(sB.getIdForElement(mA.getName()));

				// BUG: broken, comparing on element ids unsafe safe
				Set<Integer> diff = Sets.difference(mA.getCalledBy().elementSet(), mB.getCalledBy().elementSet());
				if (diff.size() > 0) {
					_log.warn("Paths to: " + mA.getName() + " missing.");
					for (int aId : diff) {
						_log.info("\tCall missing from: " + sA.getMethod(aId).getName());
					}
				} else {

				}

			} else {
				_log.debug("Missing element (and paths to it): " + mA.getName());
			}
		}

	}

	private void checkForNewPaths(Session sA, Session sB) {

		_log.info("CHECK FOR NEW PATHS");
		for (MethodElement mA : sA.getMethods()) {

			if (sB.hasIDForElement(mA.getName())) {
				MethodElement mB = sB.getMethod(sB.getIdForElement(mA.getName()));

				// BUG: broken, comparing on element ids unsafe safe
				Set<Integer> diff = Sets.difference(mB.getCalledBy().elementSet(), mA.getCalledBy().elementSet());
				if (diff.size() > 0) {
					_log.warn("New paths added to: " + mA.getName());
					for (int bId : diff) {
						_log.info("\tCall added from: " + sB.getMethod(bId).getName());
					}
				} else {
					// no new paths
				}

			} else {
				_log.debug("Missing element: " + mA.getName());
			}
		}

	}

	private void checkForMissingElements(Session sessionA, Session sessionB) {

		_log.info("CHECK FOR MISSING ELEMENTS");

		// works, comparing on element names safe
		Set<String> diff = Sets.difference(sessionA.getElementNames(), sessionB.getElementNames());

		for (String elemName : diff) {
			_log.warn("Session B lacks element: " + elemName);
		}

	}

	private void checkForNewElements(Session sessionA, Session sessionB) {

		_log.info("CHECK FOR NEW ELEMENTS");

		// works, comparing on element names safe
		Set<String> diff = Sets.difference(sessionB.getElementNames(), sessionA.getElementNames());
		for (String elemName : diff) {
			_log.warn("Session B adds new element: " + elemName);

		}

	}

}

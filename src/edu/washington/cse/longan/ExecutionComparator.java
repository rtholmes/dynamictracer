package edu.washington.cse.longan;

import java.util.Vector;

import org.apache.log4j.Logger;

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
		// joda 1
		executionFiles.add(path + "joda1371a.xml");
		// joda 2
		executionFiles.add(path + "joda1371b.xml");
		// inh run 1
		// executionFiles.add(path + "inhTesta.xml");
		// executionFiles.add(path + "inhTestb.xml");

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

		checkForMissingOrNewElements(sA, sB);
		checkForMissingOrNewPaths(sA, sB);

	}

	private void checkForMissingOrNewPaths(Session sA, Session sB) {

		for (MethodElement mA : sA.getMethods()) {

			if (sB.hasIDForElement(mA.getName())) {
				MethodElement mB = sB.getMethod(sB.getIdForElement(mA.getName()));

				if (mB.getCalledBy().containsAll(mA.getCalledBy())) {
					// no new paths
				} else {
					_log.warn("Existing paths missing for: " + mA.getName());
				}

				if (mA.getCalledBy().containsAll(mB.getCalledBy())) {
					// no paths missing
				} else {
					_log.warn("New paths added for: " + mA.getName());
				}
			} else {
				_log.info("Missing element: " + mA.getName());
			}
		}

	}

	private void checkForMissingOrNewElements(Session sessionA, Session sessionB) {

		for (String elemName : sessionA.getElementNames()) {
			if (!sessionB.getElementNames().contains(elemName)) {
				_log.warn("Session B lacks element: " + elemName);
			}
		}

		for (String elemName : sessionB.getElementNames()) {
			if (!sessionA.getElementNames().contains(elemName)) {
				_log.warn("Session B adds new element: " + elemName);
			}
		}

	}
}

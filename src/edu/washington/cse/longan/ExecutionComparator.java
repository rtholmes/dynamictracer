package edu.washington.cse.longan;

import java.util.Vector;

import org.apache.log4j.Logger;

import ca.lsmr.common.log.LSMRLogger;
import edu.washington.cse.longan.io.SessionXMLReader;
import edu.washington.cse.longan.io.SessionXMLWriter;
import edu.washington.cse.longan.model.ILonganConstants;
import edu.washington.cse.longan.model.Session;

public class ExecutionComparator {
	Logger _log = Logger.getLogger(this.getClass());

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		LSMRLogger.startLog4J(true, ILonganConstants.LOGGING_LEVEL);

		String path = ILonganConstants.OUTPUT_PATH;

		Vector<String> executionFiles = new Vector<String>();
		// joda 1
		// executionFiles.add(path + "joda1371a.xml");
		// joda 2
		// executionFiles.add(path+"joda1371b.xml");
		// inh run 1
		executionFiles.add(path + "inhTesta.xml");
		executionFiles.add(path + "inhTestb.xml");

		ExecutionComparator ec = new ExecutionComparator();
		ec.compare(executionFiles);

		ec = new ExecutionComparator();
		ec.readAndWrite(executionFiles.firstElement());
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

	}
}

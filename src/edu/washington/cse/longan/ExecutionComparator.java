package edu.washington.cse.longan;

import java.util.Vector;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import ca.lsmr.common.log.LSMRLogger;

import edu.washington.cse.longan.io.SessionXMLReader;
import edu.washington.cse.longan.model.Session;

public class ExecutionComparator {
	Logger _log = Logger.getLogger(this.getClass());

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		LSMRLogger.startLog4J(true, Level.INFO);

		String path = "/Users/rtholmes/Documents/workspaces/workspace/longAn/tmp/";

		Vector<String> executionFiles = new Vector<String>();
		// joda 1
		// executionFiles.add(path + "2009-06-15T17-19-12.153-0700.xml");
		// joda 2
		// executionFiles.add(path+"2009-06-15T17-24-21.461-0700.xml");
		// inh run 1
		executionFiles.add(path + "a.xml");

		ExecutionComparator ec = new ExecutionComparator();
		ec.compare(executionFiles);
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

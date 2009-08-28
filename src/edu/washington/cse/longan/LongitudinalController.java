package edu.washington.cse.longan;

import java.util.Vector;

import org.apache.log4j.Logger;

import ca.lsmr.common.log.LSMRLogger;
import edu.washington.cse.longan.model.ILonganConstants;

public class LongitudinalController {

	static Logger _log = Logger.getLogger(LongitudinalController.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		LSMRLogger.startLog4J(false, ILonganConstants.LOGGING_LEVEL);

		// String path = "/Users/rtholmes/Documents/workspaces/workspace/longAn/data/JodaTime/Aug25/";
		// String path = "/Users/rtholmes/Documents/workspaces/workspace/longAn/data/JodaTime/Aug26/";
		String path = "/Users/rtholmes/Documents/workspaces/workspace/longAn/data/JodaTime/Aug27/";

		String projectName = "JodaTime_";
		String staticPostfix = "_static";
		String dynamicPostfix = "_dynamic";

		Vector<String> versions = new Vector<String>();
		versions.add("1366");
		versions.add("1367");
		versions.add("1374");
		versions.add("1378");
		versions.add("1379");
		versions.add("1380");
		versions.add("1381");
		versions.add("1388");
		versions.add("1389");
		versions.add("1396");

		Vector<String> staticData = new Vector<String>();
		Vector<String> dynamicData = new Vector<String>();

		for (String version : versions) {
			String sP = path + projectName + version + staticPostfix + ".xml";
			String dP = path + projectName + version + dynamicPostfix + ".xml";

			staticData.add(sP);
			dynamicData.add(dP);
		}

		String s1;
		String s2;
		String d1;
		String d2;

		Vector<ComparatorResult> results = new Vector<ComparatorResult>();

		for (int i = 1; i < versions.size(); i++) {
			s1 = staticData.get(i - 1);
			s2 = staticData.get(i);
			d1 = dynamicData.get(i - 1);
			d2 = dynamicData.get(i);

			_log.info("*** Comparing version 1: " + versions.get(i - 1) + " to version 2: " + versions.get(i));
			VennComparator vc = new VennComparator();
			results.add(vc.run(new DataProvider(s1, s2, d1, d2)));
		}

		for (ComparatorResult result : results) {
			System.out.println(result.generateCSVNodeRow());
			System.out.println(result.generateCSVPathRow());
		}
	}

}

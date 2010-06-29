package edu.washington.cse.longan;

import java.util.Vector;

import org.apache.log4j.Logger;

import ca.lsmr.common.log.LSMRLogger;

public class LongitudinalController {

	static Logger _log = Logger.getLogger(LongitudinalController.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		LSMRLogger.startLog4J(false, ILonganConstantsPriv.LOGGING_LEVEL);

		// String path = "/Users/rtholmes/Documents/workspaces/workspace/longAn/data/JodaTime/Aug25/";
		// String path = "/Users/rtholmes/Documents/workspaces/workspace/longAn/data/JodaTime/Aug26/";
		// String path = "/Users/rtholmes/Documents/workspaces/workspace/longAn/data/JodaTime/Aug27/";
		String path;

		String projectName; // = "JodaTime_";
		String staticPostfix; // = "_static";
		String dynamicPostfix;// = "_dynamic";

		Vector<String> versions = new Vector<String>();

		projectName = "JodaTime_";
		staticPostfix = "_static";
		dynamicPostfix = "_dynamic";
		path = "/Users/rtholmes/Documents/workspaces/workspace/longAn/data/JodaTime/Aug27/";
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

		// projectName = "JodaTime_";
		// staticPostfix = "_static";
		// dynamicPostfix = "_dynamic";
		// path = "/Users/rtholmes/Documents/workspaces/workspace/longAn/data/JodaTime/Aug27/";
		// versions.add("1381-1.5");
		// versions.add("1381-1.6");

		// projectName = "google-rfc-2445_";
		// staticPostfix = "_static";
		// dynamicPostfix = "_dynamic";
		// path = "/Users/rtholmes/Documents/workspaces/workspace/longAn/data/google-rfc-2445/Sept2/";
		// versions.add("9a");
		// versions.add("12a");
		// versions.add("13a");
		// versions.add("15a");
		// versions.add("16a");
		// versions.add("17a");
		// versions.add("18a");
		// versions.add("20a");
		// versions.add("21a");
		// versions.add("22a");

		// projectName = "google-visualization_";
		// staticPostfix = "_static";
		// dynamicPostfix = "_dynamic";
		// path = "/Users/rtholmes/Documents/workspaces/workspace/longAn/data/google-visualization/Sept2/";
		// versions.add("17a");
		// versions.add("19a");
		// versions.add("20a");
		// versions.add("21a");
		// versions.add("22a");
		// versions.add("23a");
		// versions.add("24a");
		// versions.add("28a");
		// versions.add("29a");
		// versions.add("30b");

		// projectName = "jMock-";
		// staticPostfix = "_static";
		// dynamicPostfix = "_dynamic";
		// path = "/Users/rtholmes/workspace/longAn/data/jMock/Sept2/";
		// // versions.add("1351"); (static missing)
		// versions.add("1353");
		// versions.add("1357");
		// versions.add("1358");
		// versions.add("1359");
		// versions.add("1360");
		// versions.add("1361");
		// versions.add("1362");
		// versions.add("1363");
		// versions.add("1365");
		//
		// projectName = "log4j-";
		// staticPostfix = "_static";
		// dynamicPostfix = "_dynamic";
		// path = "/Users/rtholmes/workspace/longAn/data/log4j/Sept2/";
		// versions.add("773775");
		// versions.add("773779");
		// versions.add("1357");
		// versions.add("1358");
		// versions.add("1359");
		// versions.add("1360");
		// versions.add("1361");
		// versions.add("1362");
		// versions.add("1363");
		// versions.add("1365");

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
			// System.out.println(result.generateCSVNodeRow());
			// System.out.println(result.generateCSVPathRow());
			System.out.println(result.generateCSVPathRow3());
		}
	}

}

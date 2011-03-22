package edu.washington.cse.longan;

import ca.lsmr.common.log.LSMRLogger;
import edu.washington.cse.longan.model.ILonganConstants;

public abstract class HTMLGenerator {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// LOGGING
		LSMRLogger.startLog4J(false, ILonganConstantsPriv.LOGGING_LEVEL);

		long start = System.currentTimeMillis();

		String outputHTMLpath = ILonganConstants.OUTPUT_HTML;

		// TODO: specify paths
		String staticAPath = "";
		String staticBPath = "";
		String dynamicAPath = "";
		String dynamicBPath = "";

		// get paths
		DataProvider provider = new DataProvider(staticAPath, staticBPath, dynamicAPath, dynamicBPath);

		VennComparator ec = new VennComparator();
		ec.run(provider);

		String htmlFName = outputHTMLpath + "";

		// can print to a file
		// FileOutputStream fileStream;
		// PrintStream out;
		// try {
		// Create a new file output stream
		// fileStream = new FileOutputStream(htmlFName);
		//
		// Connect print stream to the output stream
		//
		// out = new PrintStream(fileStream);
		//
		// ec.writeHTML(out);
		//
		// out.close();
		// } catch (Exception e) {
		// System.err.println("Error in writing to file");
		// }

		ec.writeHTML(System.out);
	}
}

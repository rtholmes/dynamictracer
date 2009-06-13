package edu.washington.cse.longan.io;

import org.jdom.Document;
import org.jdom.Element;

import ca.lsmr.common.util.TimeUtility;
import ca.lsmr.common.util.xml.XMLTools;
import edu.washington.cse.longan.Collector;

public class SessionXMLWriter {

	public static void write(String fName, Collector collector) {
		Document doc = XMLTools.newXMLDocument();
		
		Element root = new Element(ILonganIO.ROOT);
		root.setAttribute(ILonganIO.DATE, TimeUtility.getCurrentLSMRDateString());
		
		boolean success = XMLTools.writeXMLDocument(doc, fName);
	}
}

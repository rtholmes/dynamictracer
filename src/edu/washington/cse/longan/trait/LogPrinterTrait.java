package edu.washington.cse.longan.trait;

import org.apache.log4j.Logger;
import org.jdom.Element;

import com.google.common.collect.Multiset;


public class LogPrinterTrait extends AbstractTrait{

	public static String ID = "LogPrinterTrait";
	
	Logger _log = Logger.getLogger(this.getClass());
	public Multiset<DATA_KINDS> getData() {
		return null;
	}

	public String getDescription() {
		return "Debugging trait for printing values to the screen";
	}

	public String getName() {
		return ID;
	}

	public Multiset<String> getSupplementalData() {
		return null;
	}

	public void track(Object obj) {
		_log.debug(obj);
	}

	@Override
	public String toString() {
		return "";
	}

	
	public static ITrait parseXML(Element element){
		throw new AssertionError("Subtypes should implement this method.");
	}
}

package edu.washington.cse.longan.trait;

import org.apache.log4j.Logger;

import com.google.common.collect.Multiset;


public class LogPrinterTrait implements ITrait {

	Logger _log = Logger.getLogger(this.getClass());
	public Multiset<DATA_KINDS> getData() {
		return null;
	}

	public String getDescription() {
		return "Debugging trait for printing values to the screen";
	}

	public String getName() {
		return "LogPrinterTrait";
	}

	public Multiset<String> getSupplementalData() {
		return null;
	}

	public void track(Object obj) {
		_log.debug(obj);
	}

}

/**
 * Created on Apr 15, 2009
 * @author rtholmes
 */
package edu.washington.cse.longan;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.CodeSignature;

public class Collector {
	private Logger _log = Logger.getLogger(this.getClass());

	Hashtable<Integer, Signature> idToSignatureMap = new Hashtable<Integer, Signature>();
	
	Stack<Signature> globalStack = new Stack<Signature>();
	
	public static final boolean OUTPUT = false;
	
	private Collector() {

	}

	private static Collector _instance = null;

	public static Collector getInstance() {
		if (_instance == null)
			_instance = new Collector();
		return _instance;
	}
	
	
	
	


}

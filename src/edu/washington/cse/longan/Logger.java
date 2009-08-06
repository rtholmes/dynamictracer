package edu.washington.cse.longan;

import java.util.Hashtable;

public class Logger {

	@SuppressWarnings("unchecked")
	private static Hashtable<Class, Logger> _instanceMap = new Hashtable<Class, Logger>();
	private String _prefix;

	private Logger() {

	}

	@SuppressWarnings("unchecked")
	private Logger(Class clazz) {
		_prefix = clazz.getSimpleName();
	}

	@SuppressWarnings("unchecked")
	public static Logger getLogger(Class clazz) {

		if (!_instanceMap.containsKey(clazz))
			_instanceMap.put(clazz, new Logger(clazz));
//			_instanceMap.put(clazz, new Logger(clazz));

		return _instanceMap.get(clazz);
	}

	public void warn(String msg) {
		System.out.println("WARN\t"+_prefix+"\t"+msg);
	}

	public void info(String msg) {
		System.out.println("INFO\t"+_prefix+"\t"+msg);
	}

	public void debug(String msg) {
		System.out.println("DEBUG\t"+_prefix+"\t"+msg);
	}

	public void error(Exception e) {
		System.err.println("ERROR\t"+_prefix+"\t"+e.getMessage());
	}

	public void error(String msg) {
		System.err.println("ERROR\t"+_prefix+"\t"+msg);
	}

	public void trace(String msg) {
		System.out.println("TRACE\t"+_prefix+"\t"+msg);
	}

}

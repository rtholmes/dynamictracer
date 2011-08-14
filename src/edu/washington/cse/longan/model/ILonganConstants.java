package edu.washington.cse.longan.model;

//import org.apache.log4j.Level;

public interface ILonganConstants {

	public static final String VOID_RETURN = "void";

	public static final String SEPARATOR = "::";

	public static final String INIT_METHOD = SEPARATOR + "init" + SEPARATOR;

	public static final int UNKNOWN_METHOD_ID = -1;
	public static final String UNKNOWN_METHOD_NAME = SEPARATOR + "unknownM" + SEPARATOR;

	// LOGGING
	// public static final Level LOGGING_LEVEL = Level.INFO;

	public static final boolean CALLSTACK_ONLY = true;

	public static final boolean SILENT = false;

	// makes the XML file easier to read by subbing strings for int indexes
	// also causes the latest.xml to be generated
	public static final boolean OUTPUT_DEBUG = true;

	// WARNING: if you set this true make sure that LOGGING_LEVEL is TRACE or DEBUG
	public static final boolean OUTPUT_SCREEN = false;

	// big kahuna
	public static final boolean OUTPUT = false;

	public static final boolean OUTPUT_SUMMARY = false;

	// whether to call the abstractTracker.track method
	public static final boolean TRACK_TRAITS = false;

	public static final boolean OUTPUT_XML = true;
	public static final boolean OUTPUT_ZIP = false;

	public static final String OUTPUT_PATH = "report/dynamic/tmp/";

	public static final String OUTPUT_HTML = "report/html/";

	public static final String INPUT_STATIC_PATH = "data/static/";

	public static final String INPUT_DYNAMIC_PATH = "data/dynamic/";

	public static final String NOT_POSSIBLE = "Shouldn't be reachable";

	public static final String ELIDED_STRING = "elided";

}

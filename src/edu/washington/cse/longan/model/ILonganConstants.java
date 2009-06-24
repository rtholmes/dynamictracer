package edu.washington.cse.longan.model;

import org.apache.log4j.Level;

public interface ILonganConstants {

	public static final String VOID_RETURN = "void";

	public static final String SEPARATOR = "::";

	public static final String INIT_METHOD = SEPARATOR + "init" + SEPARATOR;

	public static final int UNKNOWN_METHOD_ID = -1;
	public static final String UNKNOWN_METHOD_NAME = SEPARATOR + "unknownM" + SEPARATOR;

	public static final Level LOGGING_LEVEL = Level.INFO;

	public static final boolean OUTPUT_DEBUG = true;
	
	// WARNING: if you set this true make sure that LOGGING_LEVEL is TRACE or DEBUG
	public static final boolean OUTPUT_SCREEN = false;
	
	public static final boolean OUTPUT_SUMMARY = false;
	
	public static final boolean OUTPUT_XML = true;
	public static final boolean OUTPUT_ZIP = false;
	public static final String OUTPUT_PATH = "/Users/rtholmes/Documents/workspaces/workspace/longAn/tmp/";

	public static final String NOT_POSSIBLE = "Shouldn't be reachable";

}

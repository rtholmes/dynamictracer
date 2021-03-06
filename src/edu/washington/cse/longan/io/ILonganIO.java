package edu.washington.cse.longan.io;

public class ILonganIO {

	public static final String ROOT = "executionSession";
	public static final String DATE = "date";
	public static final String TRAITS = "traits";
	public static final String TRAIT = "trait";
	public static final String STATIC = "static";
	public static final String DYNAMIC = "dynamic";
	public static final String METHODS = "methods";
	public static final String METHOD = "method";
	public static final String FIELDS = "fields";
	public static final String FIELD = "field";
	public static final String PARAMETERS = "params";
	public static final String PARAMETER = "param";
	public static final String RETURN = "return";
	public static final String TYPE = "type";
	public static final String POSITION = "pos";
	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String EXTERNAL = "ext";
	public static final String VALUE = "value";
	public static final String KEY = "key";
	public static final String DATA = "data";
	public static final String SUPPLEMENTAL_DATA = "sdata";
	public static final String TIME = "time";
	public static final String UNKNOWN_CALLER = "unknown";
	public static final String CALLEDBY = "calledBy";
	public static final String UNKNOWN_ID = "-1";
	public static final String COUNT = "count";
	public static final String EXCEPTIONS = "exceptions";
	public static final String EXCEPTION = "exception";
	public static final String SERIAL = "serial";
	public static final String GET = "get";
	public static final String SET = "set";

	// public static final String ROOT = "executionSession";
	// public static final String DATE = "date";
	// public static final String TRAITS = "ts";//"traits";
	// public static final String TRAIT = "t";//"trait";
	// public static final String STATIC = "static";
	// public static final String DYNAMIC = "dynamic";
	// public static final String METHODS = "methods";
	// public static final String METHOD = "m";//"method";
	// public static final String FIELDS = "fields";
	// public static final String FIELD = "f";//"field";
	// public static final String PARAMETERS = "ps";//"params";
	// public static final String PARAMETER = "p";//"param";
	// public static final String RETURN = "r";//"return";
	// public static final String TYPE = "type";
	// public static final String POSITION = "pos";
	// public static final String ID = "id";
	// public static final String NAME = "n";//"name";
	// public static final String VALUE = "v";//"value";
	// public static final String KEY = "k";//"key";
	// public static final String DATA = "d";//"data";
	// public static final String SUPPLEMENTAL_DATA = "sdata";
	// public static final String TIME = "t";//"time";
	// public static final String UNKNOWN_CALLER = "unknown";
	// public static final String CALLEDBY = "cb";//"calledBy";
	// public static final String UNKNOWN_ID = "-1";
	// public static final String COUNT = "cnt";//"count";

	public static boolean ignoreName(String name) {
		if (name.contains("<clinit>"))
			return true;

		if (name.contains("java.lang.StringBuilder"))
			return true;

		if (name.contains("$$"))
			return true;

		if (name.equals(""))
			return true;

		if (name.contains("<init>"))
			return true;

		if (name.startsWith("com.imprev.service.config.SiteSettingsFactory"))
			return true;

		if (!name.startsWith("com.imprev"))
			return true;

		if (name.indexOf("Test.") > 0)
			return true;

		if (name.indexOf("$") > 0)
			return true;

		if (name.indexOf("ImprevTestCase") > 0)
			return true;

		return false;
	}
}

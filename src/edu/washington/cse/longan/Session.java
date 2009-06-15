package edu.washington.cse.longan;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Set;

/**
 * Contains the dynamic details of any single session. 
 * 
 * @author rtholmes
 *
 */
public class Session {

	private Hashtable<Integer, FieldAgent> _fields = new Hashtable<Integer, FieldAgent>();

	/**
	 * Uses the JPS.getId() as an index; the stored element is the 'base' index for the element associated with the JPS
	 * id. (JPS.id binds every join point itself so there can be multiple points for any program element)
	 */
	// private Integer[] _ids = new Integer[1024];

	private Hashtable<Integer, MethodAgent> _methods = new Hashtable<Integer, MethodAgent>();

	/**
	 * This index is used to maintain the _ids array: in this way the names of elements are tracked and using the name
	 * the common base id can be found.
	 */
	private Hashtable<String, Integer> _nameToBaseIdMap = new Hashtable<String, Integer>();

	/**
	 * id -> milliseconds
	 */
	private Hashtable<Integer, Long> _profile = new Hashtable<Integer, Long>();

	public MethodAgent getMethod(int id) {
		return _methods.get(id);
	}

	boolean methodExists(int id) {
		return _methods.containsKey(id);
	}

	public void addMethod(int id, MethodAgent method) {
		_methods.put(id, method);
	}

	public Collection<FieldAgent> getFields() {
		return _fields.values();
	}

	public Collection<MethodAgent> getMethods() {
		return _methods.values();
	}

	public int getIdForElement(String name) {
		return _nameToBaseIdMap.get(name);
	}

	public boolean hasIDForElement(String name) {
		return _nameToBaseIdMap.containsKey(name);
	}

	public void addIDForElement(String name, int id) {
		_nameToBaseIdMap.put(name, id);
	}
	
	protected Hashtable<Integer, Long> getProfile() {
		return _profile;
	}
	
	protected Set<String> getElementNames() {
		return _nameToBaseIdMap.keySet();
	}
}

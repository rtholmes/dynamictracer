package edu.washington.cse.longan.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * Contains the dynamic details of any single session.
 * 
 * @author rtholmes
 * 
 */
public class Session {

	private Logger _log = Logger.getLogger(this.getClass());

	private Hashtable<Integer, FieldElement> _fields = new Hashtable<Integer, FieldElement>();

	/**
	 * Uses the JPS.getId() as an index; the stored element is the 'base' index for the element associated with the JPS
	 * id. (JPS.id binds every join point itself so there can be multiple points for any program element)
	 */
	// private Integer[] _ids = new Integer[1024];

	private Hashtable<Integer, MethodElement> _methods = new Hashtable<Integer, MethodElement>();

	/**
	 * This index is used to maintain the _ids array: in this way the names of elements are tracked and using the name
	 * the common base id can be found.
	 */
	private Hashtable<String, Integer> _nameToBaseIdMap = new Hashtable<String, Integer>();

	/**
	 * id -> milliseconds
	 */
	private Hashtable<Integer, Long> _profile = new Hashtable<Integer, Long>();

	public Session() {
		_log.info("New session instantiated.");
		createDefaultElements();
	}

	private void createDefaultElements() {

		addMethod(ILonganConstants.UNKNOWN_METHOD_ID, new MethodElement(ILonganConstants.UNKNOWN_METHOD_ID,
				ILonganConstants.UNKNOWN_METHOD_NAME, true));

		addIDForElement(ILonganConstants.UNKNOWN_METHOD_NAME, ILonganConstants.UNKNOWN_METHOD_ID);

	}

	public MethodElement getMethod(int id) {
		return _methods.get(id);
	}

	public boolean methodExists(int id) {
		return _methods.containsKey(id);
	}

	public void addMethod(int id, MethodElement method) {
		_methods.put(id, method);
	}

	public Collection<FieldElement> getFields() {
		return _fields.values();
	}

	public Collection<MethodElement> getMethods() {
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

	public Hashtable<Integer, Long> getProfile() {
		return _profile;
	}

	public Set<String> getElementNames() {
		return _nameToBaseIdMap.keySet();
	}

	public Set<MethodElement> getElementSet(Set<Integer> elementSet) {
		HashSet<MethodElement> returnSet = new HashSet<MethodElement>();
		for (int id : elementSet) {
			returnSet.add(getMethod(id));
		}
		return returnSet;
	}
}

package edu.washington.cse.longan.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

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
	private BiMap<String, Integer> _nameToBaseIdMap = HashBiMap.create();

	private BiMap<String, Integer> _fieldNameToBaseIdMap = HashBiMap.create();

	private BiMap<String, Integer> _methodNameToBaseIdMap = HashBiMap.create();

	/**
	 * id -> milliseconds
	 */
	private Hashtable<Integer, Long> _profile = new Hashtable<Integer, Long>();

	private String _sessionName;

	public Session(String sessionName) {
		_log.info("New session instantiated.");
		_sessionName = sessionName;
		createDefaultElements();
	}

	public String getSessionName() {
		return _sessionName;
	}

	private void createDefaultElements() {

		addMethod(ILonganConstants.UNKNOWN_METHOD_ID, new MethodElement(ILonganConstants.UNKNOWN_METHOD_ID, ILonganConstants.UNKNOWN_METHOD_NAME,
				true));

		addIDForElement(ILonganConstants.UNKNOWN_METHOD_NAME, ILonganConstants.UNKNOWN_METHOD_ID);

		_profile.put(ILonganConstants.UNKNOWN_METHOD_ID, 0L);

	}

	public MethodElement getMethod(int id) {
		return _methods.get(id);
	}

	public FieldElement getField(int id) {
		return _fields.get(id);
	}

	public boolean methodExists(int id) {
		return _methods.containsKey(id);
	}

	public void addMethod(int id, MethodElement method) {
		_methods.put(id, method);

		if (!_methodNameToBaseIdMap.containsKey(method.getName()))
			_methodNameToBaseIdMap.put(method.getName(), id);
	}

	public Collection<FieldElement> getFields() {
		return _fields.values();
	}

	public Collection<MethodElement> getMethods() {
		return _methods.values();
	}

	public int getIdForElement(String name) {
		Preconditions.checkArgument(_nameToBaseIdMap.containsKey(name), "check with hasID first");
		return _nameToBaseIdMap.get(name);
	}

	public String getElementNameForID(int id) {
		return _nameToBaseIdMap.inverse().get(id);
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

	public MethodElement getElementForName(String name) {
		return getMethod(getIdForElement(name));
	}

	public void addField(int id, FieldElement field) {
		_fields.put(id, field);

		if (!_fieldNameToBaseIdMap.containsKey(field.getName()))
			_fieldNameToBaseIdMap.put(field.getName(), id);
	}

	public boolean fieldExists(int id) {
		return _fields.containsKey(id);
	}

	
	public Set<String> getMethodNames() {
		return _methodNameToBaseIdMap.keySet();
	}

	public Set<String> getFieldNames() {
		return _fieldNameToBaseIdMap.keySet();
	}
}

package edu.washington.cse.longan.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSet;

import edu.washington.cse.longan.Logger;
import edu.washington.cse.longan.Path;

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
	 * Uses the JPS.getId() as an index; the stored element is the 'base' index for the element associated with the JPS id. (JPS.id binds every join
	 * point itself so there can be multiple points for any program element)
	 */
	// private Integer[] _ids = new Integer[1024];

	private Hashtable<Integer, MethodElement> _methods = new Hashtable<Integer, MethodElement>();

	/**
	 * This index is used to maintain the _ids array: in this way the names of elements are tracked and using the name the common base id can be
	 * found.
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
		// _log.info("New session instantiated.");
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
		// _log.info("Creating method: "+method.getName());
		_methods.put(id, method);

		if (!_methodNameToBaseIdMap.containsKey(method.getName())) {
			_methodNameToBaseIdMap.put(method.getName(), id);

			// RFE: these maps should all be combined so we don't make so many mistakes
			// (not the maps but their updating)
			if (!_nameToBaseIdMap.containsKey(method.getName())) {
				_nameToBaseIdMap.put(method.getName(), id);
			} else {
				Preconditions.checkNotNull(null, ILonganConstants.NOT_POSSIBLE);
			}
		}
	}

	public Collection<FieldElement> getFields() {
		return _fields.values();
	}

	public Collection<MethodElement> getMethods() {
		return _methods.values();
	}

	public int getIdForElement(String name) {
		Preconditions.checkArgument(_nameToBaseIdMap.containsKey(name), "check with hasID first (" + name + ")");
		return _nameToBaseIdMap.get(name);
	}

	public String getElementNameForID(int id) {
		return _nameToBaseIdMap.inverse().get(id);
	}

	public boolean hasIDForElement(String name) {
		return _nameToBaseIdMap.containsKey(name);
	}

	private void addIDForElement(String name, int id) {
		_nameToBaseIdMap.put(name, id);
	}

	public Hashtable<Integer, Long> getProfile() {
		return _profile;
	}

	public Set<String> getElementNames() {
		return _nameToBaseIdMap.keySet();
	}

	public Set<AbstractElement> getElementSet(Set<Integer> elementSet) {
		HashSet<AbstractElement> returnSet = new HashSet<AbstractElement>();
		for (int id : elementSet) {
			AbstractElement ae = null;

			boolean isField = _fields.containsKey(id);
			boolean isMethod = _methods.containsKey(id);

			Preconditions.checkArgument(!(isField && isMethod), ILonganConstants.NOT_POSSIBLE);
			Preconditions.checkArgument(isField || isMethod, "Unknown element id: " + id);

			if (isMethod)
				ae = getMethod(id);
			else if (isField)
				ae = getField(id);

			returnSet.add(ae);
		}
		return returnSet;
	}

	/**
	 * @param name
	 * @return
	 */
	public AbstractElement getElementForName(String name) {
		if (_fieldNameToBaseIdMap.containsKey(name))
			return getField(getIdForElement(name));
		else if (_methodNameToBaseIdMap.containsKey(name))
			return getMethod(getIdForElement(name));
		else
			Preconditions.checkNotNull(null, "Unknown element: " + name);
		return null;
	}

	public MethodElement getMethodForName(String name) {
		return getMethod(getIdForElement(name));
	}

	public FieldElement getFieldForName(String name) {
		return getField(getIdForElement(name));
	}

	public void addField(int id, FieldElement field) {

		_fields.put(id, field);

		if (!_fieldNameToBaseIdMap.containsKey(field.getName()))
			_fieldNameToBaseIdMap.put(field.getName(), id);

		if (!_nameToBaseIdMap.containsKey(field.getName())) {
			_nameToBaseIdMap.put(field.getName(), id);
		} else {
			Preconditions.checkNotNull(null, ILonganConstants.NOT_POSSIBLE);
		}
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

	public void setSessionName(String fName) {
		_sessionName = fName;
	}

	public ImmutableSet<Path> getPaths() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * This is _very_ uncommon; only currently used by SessionXMLWriter while massaging the data to remove spurious access methods
	 * 
	 * @param method
	 */
	public void removeMethod(MethodElement method) {
		_methods.remove(method);
		_methodNameToBaseIdMap.remove(method.getName());
		_nameToBaseIdMap.remove(method.getName());

	}
}

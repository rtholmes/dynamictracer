package edu.washington.cse.longan.io;

import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

import edu.washington.cse.longan.model.FieldElement;
import edu.washington.cse.longan.model.MethodElement;
import edu.washington.cse.longan.model.Session;

public class DSAXMLReader {

	private Logger _log = Logger.getLogger(this.getClass());

	public enum OPERATION {
		ADDED, DELETED, NO_CHANGE
	}

	public enum KIND {
		CLASS, FIELD, METHOD
	}

	public enum VISIBILITY {
		PUBLIC, PROTECTED, PRIVATE
	}

	private int _nextId = 1;

	@SuppressWarnings("unchecked")
	public Session read(Document doc, String fName) {

		Vector<StructuralDefinition> sds = new Vector<StructuralDefinition>();
		Element rootElement = doc.getRootElement();

		for (Element sdElem : (List<Element>) rootElement.getChildren()) {

			String kind = sdElem.getAttributeValue("kind");
			String name = sdElem.getAttributeValue("name");

			StructuralDefinition sd = new StructuralDefinition(name, KIND.valueOf(kind), null, null);
			sds.add(sd);

			Element callsElem = sdElem.getChild("calls");
			Element referencesElem = sdElem.getChild("references");

			for (Element callElem : (List<Element>) callsElem.getChildren()) {

				String id = callElem.getAttributeValue("id");
				sd.addCall(id);
			}
			for (Element refElem : (List<Element>) referencesElem.getChildren()) {

				String id = refElem.getAttributeValue("id");
				sd.addReference(id);
			}
		}

		Session session = new Session(fName);

		// nodes

		for (StructuralDefinition sd : sds) {

			if (sd.getKind().equals(KIND.METHOD)) {

				String methodName = sd.getName();
				convertMethod(methodName, session);

			} else if (sd.getKind().equals(KIND.FIELD)) {

				String fieldName = sd.getName();
				convertField(fieldName, session);

			} else if (sd.getKind().equals(KIND.CLASS)) {
				// do nothing
			} else {
				_log.info("Unknown kind: " + sd.getKind());
			}
		}

		// edges

		for (StructuralDefinition sd : sds) {

			if (sd.getKind().equals(KIND.METHOD)) {

				int id = -1;
				String methodName = sd.getName();

				// don't track calls from clinit
				if (!methodName.contains("<clinit>")) {
					MethodElement source = convertMethod(methodName, session);
					for (String targetName : sd.getCalls()) {
						MethodElement target = convertMethod(targetName, session);

						if (source == null || target == null) {
							_log.error("Call dropped: " + methodName + " -> " + targetName);
						} else {
							// don't track calls from class initializers

							target.getCalledBy().add(source.getId());
						}

					}
					
					
					for (String targetName : sd.getReferences()){
						FieldElement target = convertField(targetName,session);
						
						if (source == null || target == null) {
							_log.error("Reference dropped: " + methodName + " -> " + targetName);
						} else {
							// don't track calls from class initializers

							target.getGetBy().add(source.getId());
						}
						
					}
				}

			} 
			
//			else if (sd.getKind().equals(KIND.FIELD)) {
//
//				int id = -1;
//				String fieldName = sd.getName();
//				if (!session.hasIDForElement(fieldName)) {
//					// session.addIDForElement(fieldName, _nextId++);
//					id = nextId++;
//				} else {
//					id = session.getIdForElement(fieldName);
//				}
//
//				if (!session.fieldExists(id)) {
//					session.addField(id, new FieldElement(id, fieldName));
//				}
//
//			} else if (sd.getKind().equals(KIND.CLASS)) {
//				// do nothing
//			} else {
//				_log.info("Unknown kind: " + sd.getKind());
//			}
		}

		return session;
	}

	private FieldElement convertField(String fieldName, Session session) {
		int id = -1;

		if (!session.hasIDForElement(fieldName)) {
			// session.addIDForElement(fieldName, _nextId++);
			id = _nextId++;
		} else {
			id = session.getIdForElement(fieldName);
		}

		if (!session.fieldExists(id)) {
			session.addField(id, new FieldElement(id, fieldName));
		}
		return session.getFieldForName(fieldName);
	}

	private MethodElement convertMethod(String methodName, Session session) {
		int id = -1;

		if (!session.hasIDForElement(methodName)) {
			id = _nextId++;
		} else {
			id = session.getIdForElement(methodName);
		}
		if (!session.methodExists(id)) {
			session.addMethod(id, new MethodElement(id, methodName, false));
		}

		return session.getMethodForName(methodName);
	}

	public class StructuralDefinition {
		private Logger _log = Logger.getLogger(this.getClass());

		private String _name;

		private KIND _kind;

		private VISIBILITY _visibility;

		private OPERATION _operation;

		private String _packageName = "";

		private String _extendedType = "";

		private Vector<String> _implementedInterfaces;

		private boolean _isInterface = false;

		private Vector<String> _parameters;

		private String _parentClassName = "";

		private String _returnType = null;

		private Vector<String> _paramTypes = new Vector<String>();

		private Vector<String> _inherits = new Vector<String>();

		private String _extends = null;

		private String _fieldType = null;

		public StructuralDefinition(String elementName, KIND kind, VISIBILITY visibility, OPERATION operation) {

			_name = elementName;
			_kind = kind;
			_visibility = visibility;
			_operation = operation;

		}

		// for all elements
		public void setPackageName(String packageName) {
			if (packageName == null)
				packageName = "";

			_packageName = packageName;
		}

		public String getPackageName() {
			return _packageName;
		}

		// methods only
		public void setReturnType(String returnType) {
			_returnType = returnType;
		}

		public String getReturnType() {
			return _returnType;
		}

		// classes / interfaces only
		public void setExtendedType(String extendedType) {
			_extendedType = extendedType;
		}

		public String getExtendedType() {
			return _extendedType;
		}

		// classes / interfaces only
		public void setImplementedInterfaces(Vector<String> implementedInterfaces) {
			_implementedInterfaces = implementedInterfaces;
		}

		public Vector<String> getImplementedInterfaces() {
			return _implementedInterfaces;
		}

		// classes / interfaces only
		public void setIsInterface(boolean isInterface) {
			_isInterface = isInterface;
		}

		public boolean isInterface() {
			return _isInterface;
		}

		// methods only
		public void setParameterTypes(Vector<String> parameters) {
			_parameters = parameters;
		}

		public Vector<String> getParameterTypes() {
			return _parameters;
		}

		// methods / fields only
		public void setParentClassName(String className) {
			_parentClassName = className;
		}

		public void setFieldTypeName(String typeName) {
			_fieldType = typeName;
		}

		public String getFieldTypeName() {
			return _fieldType;
		}

		public String getParentClassName() {
			return _parentClassName;
		}

		public StructuralDefinition() {

		}

		public KIND getKind() {

			return _kind;

		}

		public VISIBILITY getVisibility() {

			return _visibility;

		}

		public String getName() {

			return _name;

		}

		public Element getXML() {

			Element defnElement = new Element(CDXML.DEFINITION);

			defnElement.setAttribute(CDXML.KIND, getKind().toString());
			defnElement.setAttribute(CDXML.VISIBILITY, getVisibility().toString());
			defnElement.setAttribute(CDXML.NAME, getName());
			defnElement.setAttribute(CDXML.OPERATION, getOperation().toString());
			defnElement.setAttribute(CDXML.PACKAGE, getPackageName());

			if (getKind().equals(KIND.CLASS)) {

				defnElement.setAttribute(CDXML.ISINTERFACE, Boolean.toString(isInterface()));

				String extendedType = getExtendedType();
				if (extendedType == null)
					extendedType = "";
				defnElement.setAttribute(CDXML.EXTENDS, extendedType);

				Vector<String> implementedInterfaces = getImplementedInterfaces();
				Element implementsElement = new Element(CDXML.IMPLEMENTS);
				for (String implementedInterface : implementedInterfaces) {
					Element implementElement = new Element(CDXML.IMPLEMENT);
					implementElement.setAttribute(CDXML.TYPE, implementedInterface);
					implementsElement.addContent(implementElement);
				}
				defnElement.addContent(implementsElement);

			}

			if (getKind().equals(KIND.METHOD)) {

				String parentClass = getParentClassName();
				if (parentClass == null)
					parentClass = "";
				defnElement.setAttribute(CDXML.PARENT_TYPE, parentClass);

				String returnType = getReturnType();
				if (returnType == null)
					returnType = "";
				defnElement.setAttribute(CDXML.RETURNTYPE, returnType);

				Vector<String> paramTypes = getParameterTypes();
				Element parametersElement = new Element(CDXML.PARAMETERS);
				for (String parameterType : paramTypes) {
					Element parameterElement = new Element(CDXML.PARAMETER);
					parameterElement.setAttribute(CDXML.TYPE, parameterType);
					parametersElement.addContent(parameterElement);
				}
				defnElement.addContent(parametersElement);

			}

			if (getKind().equals(KIND.FIELD)) {

				String parentClass = getParentClassName();
				if (parentClass == null)
					parentClass = "";
				defnElement.setAttribute(CDXML.PARENT_TYPE, parentClass);

				String fieldType = getFieldTypeName();
				if (fieldType == null)
					fieldType = "";
				defnElement.setAttribute(CDXML.FIELDTYPE, fieldType);

			}

			return defnElement;

		}

		public OPERATION getOperation() {

			return _operation;

		}

		public void parse(Element definitionElement) {

			if (!CDXML.DEFINITION.toString().equals(definitionElement.getName())) {
				_log.fatal("ERROR");
				return;
			}

			setName(definitionElement.getAttributeValue(CDXML.NAME));
			setVisibility(VISIBILITY.valueOf(definitionElement.getAttributeValue(CDXML.VISIBILITY)));
			setKind(KIND.valueOf(definitionElement.getAttributeValue(CDXML.KIND)));
			setOperation(OPERATION.valueOf(definitionElement.getAttributeValue(CDXML.OPERATION)));

		}

		public void setOperation(OPERATION operation) {

			_operation = operation;

		}

		private void setVisibility(VISIBILITY vis) {

			_visibility = vis;

		}

		private void setKind(KIND kind) {

			_kind = kind;

		}

		private void setName(String name) {

			_name = name;

		}

		private String _stringRep = null;

		private Vector<String> _reference = new Vector<String>();

		private Vector<String> _calls = new Vector<String>();

		@Override
		public String toString() {
			if (_stringRep == null) {
				// HACK: this is just to make comparredependencylists happy
				// if (getRevision() != null)
				// _stringRep = getRevision().toString() + ":" + getName();
				// else
				_stringRep = getName();
			}
			return _stringRep;
		}

		@Override
		public int hashCode() {
			return toString().hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof StructuralDefinition)
				return toString().equals(obj.toString());

			return false;
		}

		public Vector<String> getCalls() {
			return _calls;
		}

		public Vector<String> getReferences() {
			return _reference;
		}

		public void addReference(String ref) {
			if (!_reference.contains(ref))
				_reference.add(ref);
		}

		public void addCall(String call) {
			if (!_calls.contains(call))
				_calls.add(call);
		}

		public void addExtends(String ext) {
			_extends = ext;
		}

		public String getExtends() {
			return _extends;
		}

		public void addInherits(String inh) {
			if (!_inherits.contains(inh))
				_inherits.add(inh);
		}

		public Vector<String> getInherits() {
			return _inherits;
		}

		public void addReturnType(String type) {
			_returnType = type;
		}

		public void addParameterType(String type) {
			if (!_paramTypes.contains(type))
				_paramTypes.add(type);
		}

		public void addFieldType(String type) {
			_fieldType = type;
		}

		public String getFieldType() {
			return _fieldType;
		}

	}

}

interface CDXML {

	public static String ERROR = "error";

	public static String STATUS = "status";

	public static String RESPONSE = "ChangeDistillerServerResponse";

	public static String QUERY = "ChangeDistillerServerQuery";

	public static String LEFT = "left";

	public static String RIGHT = "right";

	public static String FNAME = "fName";

	public static String REVISION = "revision";

	public static String SUCCESS = "success";

	public static String CHANGES = "changes";

	public static String CHANGE = "change";

	public static String CODES = "codes";

	public static String NAMES = "names";

	public static String CHANGE_OP_CODE = "changeOpCode";

	public static String CHANGE_CODE = "opCode";

	public static String CHANGE_STRUCTURE_CODE = "structureCode";

	public static String CHANGE_ELEMENT_NAME = "elementName";

	public static String CHANGE_PARENT_NAME = "parentName";

	public static String CHANGE_STRUCTURE_NAME = "structureName";

	public static String CHANGE_OLD_NAME = "oldName";

	public static String LOCATIONS = "locations";

	public static String CHANGE_LOCATION_LEFT_OFFSET = "leftOffset";

	public static String CHANGE_LOCATION_LEFT_LENGTH = "leftLength";

	public static String CHANGE_LOCATION_RIGHT_OFFSET = "rightOffset";

	public static String CHANGE_LOCATION_RIGHT_LENGTH = "rightLength";

	public static String DATE = "timeStamp";

	public static String PROC_TIME = "procTime";

	public static String CHANGE_CLASSIFIER = "changeClassifier";

	public static String CHANGE_DSA_STATUS = "dsaStatusCode";

	public static String CHANGE_CDSIGNIFICANCE = "cdSignificance";

	public static String CHANGE_DSASIGNIFICANCE = "dsaSignificance";

	public static String DEFINITIONS = "definitions";

	public static String DEFINITION = "definition";

	public static String KIND = "kind";

	public static String VISIBILITY = "visibility";

	public static String NAME = "name";

	public static String OPERATION = "operation";

	public static String MESSAGE = "message";

	public static String PACKAGE = "package";

	public static String EXTENDS = "extends";

	public static String IMPLEMENTS = "implements";

	public static String IMPLEMENT = "implement";

	public static String RETURNTYPE = "returnType";

	public static String PARAMETERS = "parameters";

	public static String PARAMETER = "parameter";

	public static String FIELDTYPE = "fieldType";

	public static String TYPE = "type";

	public static String ISINTERFACE = "isInterface";

	public static String PARENT_TYPE = "parentType";

	public static String REQUEST_KIND = "requestKind";

}

package edu.washington.cse.longan.io;

/**
 * Created on Jun 8, 2006
 * 
 * @author rtholmes
 */

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import ca.lsmr.common.util.TimeUtility;

import com.google.common.base.Preconditions;

import edu.washington.cse.longan.model.FieldElement;
import edu.washington.cse.longan.model.ILonganConstants;
import edu.washington.cse.longan.model.MethodElement;
import edu.washington.cse.longan.model.Session;

public class GilliganXMLReader implements IGilliganStoreIO {

	private Logger _log = Logger.getLogger(this.getClass());

	private int _nextId = 1;

	public Session read(Document doc, String fName) {
		_log.debug("Initializing Gilligan Stores");

		// Document doc = parseFile(fName);
		Element root = doc.getRootElement();

		Session session = new Session(fName);

		Element structure = root.getChild(XML_STRUCTURE);

		// read source
		Element source = structure.getChild(XML_SOURCE);
		populateNodes(source.getChild(XML_NODES), session);

		// GilliganStore sourceStore = createStore(source);
		populateEdges(source.getChild(XML_EDGES), session);

		return session;
	}

	@SuppressWarnings("unchecked")
	private void populateNodes(Element nodesElement, Session session) {
		for (Element packageElement : (List<Element>) nodesElement.getChildren(XML_PACKAGE)) {
			readElement(packageElement, session);
		}

	}

	@SuppressWarnings("unchecked")
	private void populateEdges(Element edges, Session session) {
		Element calls = edges.getChild(XML_CALLS);
		Element references = edges.getChild(XML_REFERENCES);
		Element inherits = edges.getChild(XML_INHERITS);

		for (Element call : ((List<Element>) calls.getChildren())) {
			String sourceName = call.getAttributeValue(XML_SOURCE);

			if (!sourceName.contains("<clinit>")) {

				sourceName = computeShortMethodName(sourceName);
				String targetName = computeShortMethodName(call.getAttributeValue(XML_TARGET));

				MethodElement source = session.getMethodForName(sourceName);
				MethodElement target = session.getMethodForName(targetName);

				if (source == null || target == null) {
					_log.error("Call dropped: " + call.getAttributeValue(XML_SOURCE) + " -> " + call.getAttributeValue(XML_TARGET));
				} else {
					// don't track calls from class initializers

					target.getCalledBy().add(source.getId());
				}
			}
		}

		for (Element call : ((List<Element>) references.getChildren())) {
			String sourceName = call.getAttributeValue(XML_SOURCE);
			// don't track references from class initializers
			if (!sourceName.contains("<clinit>")) {

				sourceName = computeShortMethodName(sourceName);

				MethodElement source = session.getMethodForName(sourceName);
				FieldElement field = session.getFieldForName(computeFieldName(call.getAttributeValue(XML_TARGET)));
				if (source == null || field == null) {
					_log.error("Reference dropped: " + call.getAttributeValue(XML_SOURCE) + " -> " + call.getAttributeValue(XML_TARGET));
				} else {
					// NOTE: statically we just consider everyting gets, we don't consider sets
					field.getGetBy().add(source.getId());
				}
			}
			// store reference
		}

	}

	private String computeShortMethodName(String fullName) {

		String unqualifiedMethodName = "";
		// String fullName = method.getName();

		int firstBrace = -1;
		int lastBrace = -1;

		firstBrace = fullName.indexOf("(");
		lastBrace = fullName.lastIndexOf(")");

		if (firstBrace < 1) {
			firstBrace = fullName.indexOf("<");
			lastBrace = fullName.lastIndexOf(">");
		}

		int lastdot = fullName.lastIndexOf(".", firstBrace);
		int typedot = fullName.lastIndexOf(".", lastdot - 1);

		String nameQualifier = fullName.substring(0, typedot + 1);
		String typeMethodName = fullName.substring(typedot + 1, firstBrace + 1);

		String parameterList = fullName.substring(firstBrace + 1, lastBrace);

		// .<init>(Lorg.eclipse.swt.widgets.Shell;,I,I,Ljava.lang.String;,Ljava.lang.String;)
		String shortParameterList = "";
		if (parameterList.length() > 0) {
			String[] params = parameterList.split(";");

			for (String param : params) {
				String[] innerParams = param.split(",");

				// java.io.ByteArrayInputStream([B[])

				for (String p : innerParams) {
					if (p.length() >= 1) {
						boolean is1Darray = false;
						boolean is2Darray = false;

						is2Darray = p.startsWith("[[");
						if (!is2Darray)
							is1Darray = p.startsWith("[");

						String shortParam = "";

						if (p.lastIndexOf(".") > 0)
							shortParam = p.substring(p.lastIndexOf(".") + 1, p.length());
						else {
							// it's a primitive
							if (is1Darray || is2Darray) {
								shortParam = p.substring(p.lastIndexOf('[') + 1, p.length());
							} else {
								shortParam = p.substring(p.lastIndexOf(".") + 1, p.length());
							}
						}

						if (shortParam.length() == 1)
							shortParam = translatePrimitive(shortParam);

						if (is2Darray)
							shortParam += "[][]";
						if (is1Darray)
							shortParam += "[]";

						shortParameterList += shortParam + ", ";
					}
				}
			}

			// get rid of the trailing comma
			shortParameterList = shortParameterList.substring(0, shortParameterList.length() - 2);
		}

		String methodName = nameQualifier + typeMethodName + shortParameterList + fullName.charAt(lastBrace);
		// longan does not have init designations
		methodName = methodName.replace(".<init>", "");
		// longan does not keep $s in names (although they are in the type sigs)
		methodName = methodName.replace("$", ".");

		return methodName;
	}

	private String translatePrimitive(String shortParam) {
		if (shortParam.equals("B")) {
			return "byte";
		} else if (shortParam.equals("C")) {
			return "char";
		} else if (shortParam.equals("D")) {
			return "double";
		} else if (shortParam.equals("F")) {
			return "float";
		} else if (shortParam.equals("I")) {
			return "int";
		} else if (shortParam.equals("J")) {
			return "long";
		} else if (shortParam.equals("S")) {
			return "short";
		} else if (shortParam.equals("Z")) {
			return "boolean";
		}
		return shortParam;
		// B byte signed byte
		// C char character
		// D double double precision IEEE float
		// F float single precision IEEE float
		// I int integer
		// J long long integer
		// L; ... an object of the given class
		// S short signed short
		// Z boolean true or false

	}

	@SuppressWarnings("unchecked")
	private void readElement(Element elem, Session session) {
		try {
			String elemName = elem.getName();

			boolean exists = session.hasIDForElement(elem.getAttributeValue(XML_NAME));
			// boolean exists = store.getElement(elem.getAttributeValue(XML_NAME)) != null;
			if (!exists) {
				if (elemName.equals(XML_PACKAGE)) {

					// don't care about packages but they contain classes and other packages
					for (Element child : (List<Element>) elem.getChildren()) {
						readElement(child, session);
					}

				} else if (elemName.equals(XML_CLASS)) {

					// don't care about classes but they contain interesting methods and fields
					for (Element child : (List<Element>) elem.getChildren()) {
						readElement(child, session);
					}

				} else if (elemName.equals(XML_METHOD)) {
					String methodName = elem.getAttributeValue(XML_NAME);

					// let's not track any class initialization
					if (!methodName.contains("<clinit>")) {
						_log.trace("sta name trans: " + methodName + " -> " + computeShortMethodName(methodName));
						methodName = computeShortMethodName(methodName);
						int id = -1;
						if (!session.hasIDForElement(methodName)) {
							// session.addIDForElement(methodName, _nextId++);
							id = _nextId++;
						} else {
							id = session.getIdForElement(methodName);
						}
						if (!session.methodExists(id)) {
							session.addMethod(id, new MethodElement(id, methodName, false));
						}
					}
				} else if (elemName.equals(XML_FIELD)) {
					String fieldName = elem.getAttributeValue(XML_NAME);
					fieldName = computeFieldName(fieldName);
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

				} else {
					_log.error("unknown node type: " + elemName);
				}
			} else {
				Preconditions.checkNotNull(null, ILonganConstants.NOT_POSSIBLE);
			}

		} catch (Exception e) {
			_log.error(e);
		}

	}

	private String computeFieldName(String fieldName) {
		return fieldName.replace("$", ".");
	}

	// @SuppressWarnings("unchecked")
	// private void readDecorator(Element child, IElement ie) {
	// for (Element decorator : (List<Element>) child.getChildren()) {
	// // RFE: load decorators
	// String decoratorID = decorator.getAttributeValue(IStoreIO.XML_NAME);
	// String decoratorValue = decorator.getAttributeValue(IStoreIO.XML_VALUE);
	// }
	// }

	private Document parseFile(String fName) {
		SAXBuilder builder = new SAXBuilder(false);
		Document doc = null;
		try {
			long start = System.currentTimeMillis();
			_log.info("Loading saved session: " + fName);
			File f = new File(fName);
			if (f.exists()) {
				doc = builder.build(f);
				_log.info("Session loaded in " + TimeUtility.msToHumanReadableDelta(start));
			} else {
				_log.error("File does not exist: " + fName);
			}
		} catch (JDOMException jdome) {
			_log.error(jdome);
		} catch (IOException ioe) {
			_log.error(ioe);
		}
		return doc;
	}

}

package edu.washington.cse.longan.io;

import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import edu.washington.cse.longan.model.FieldElement;
import edu.washington.cse.longan.model.ILonganConstants;
import edu.washington.cse.longan.model.MethodElement;
import edu.washington.cse.longan.model.Session;

public class LonganDynamicXMLReader {

	private int _nextId = 0;

	@SuppressWarnings("unchecked")
	public Session read(Document doc, String fName) {

		Session session = new Session(fName);

		Element rootElement = doc.getRootElement();

		Element staticElement = rootElement.getChild("static");

		for (Element methodElem : (List<Element>) staticElement.getChild("methods").getChildren("method")) {

			String methodName = methodElem.getAttributeValue("name");
			if (!ILonganIO.ignoreName(methodName))
				convertMethod(methodName, session);

		}

		// RFE: don't load fields for now
		// for (Element fieldElem : (List<Element>) staticElement.getChild("fields").getChildren("field")) {
		//
		// String fieldName = fieldElem.getAttributeValue("name");
		// FieldElement fe = convertField(fieldName, session);
		//
		// }

		// edges
		Element dynamicElement = rootElement.getChild("dynamic");
		for (Element targetElem : (List<Element>) dynamicElement.getChild("methods").getChildren("method")) {
			String targetName = targetElem.getAttributeValue("name");

			for (Element sourceElem : (List<Element>) targetElem.getChildren("calledBy")) {

				String sourceName = sourceElem.getAttributeValue("name");
				if (ILonganIO.ignoreName(sourceName) || ILonganIO.ignoreName(targetName)) {

				} else {

					MethodElement targetMethod = convertMethod(targetName, session);
					MethodElement sourceMethod = convertMethod(sourceName, session);

					// if (targetMethod.getName().contains("<clinit>") || ILonganIO.ignoreName(targetName)) {
					//
					// } else {
					if (sourceMethod != null && targetMethod != null) {
						targetMethod.getCalledBy().add(sourceMethod.getId());
					}
					// }
				}
			}
		}

		// RFE: field references not being parsed because they just aren't happening
		// for (Element targetElem : (List<Element>) dynamicElement.getChild("fields").getChildren("field")) {
		//
		// MethodElement targetMethod = convertMethod(targetElem.getAttributeValue("name"), session);
		// for (Element sourceElem : (List<Element>) targetElem.getChildren("calledBy")) {
		// MethodElement sourceMethod = convertMethod(sourceElem.getAttributeValue("name"), session);
		//
		// if (targetMethod.getName().contains("<clinit>")) {
		//
		// } else {
		// if (sourceMethod != null && targetMethod != null) {
		// targetMethod.getCalledBy().add(sourceMethod.getId());
		// }
		// }
		// }
		// }

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

		methodName = standardizeMethodName(methodName);

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

	private String standardizeMethodName(String originalName) {
		if (originalName.equals(ILonganConstants.UNKNOWN_METHOD_NAME))
			return originalName;

		// XXX: if the object is a constructor, don't do this, it'll trim to the first parameter this way

		int firstSpace = originalName.indexOf(' ');

		// the whole point of this is to strip off the return type, but this shouldn't be fully-qualified anyways
		if (firstSpace > 0 && originalName.substring(0, firstSpace).indexOf(".") < 0) {
			String newName = "";
			if (firstSpace > 0)
				newName = originalName.substring(firstSpace + 1);
			// _log.trace("dyn name trans: " + originalName + " -> " + newName);

			return newName;
		}

		// _log.trace("dyn name same: " + originalName);
		return originalName;
		// return newName;
	}
}

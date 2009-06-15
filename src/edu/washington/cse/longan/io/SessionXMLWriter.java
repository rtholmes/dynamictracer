package edu.washington.cse.longan.io;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import org.jdom.Document;
import org.jdom.Element;

import ca.lsmr.common.util.TimeUtility;
import ca.lsmr.common.util.xml.XMLTools;
import edu.washington.cse.longan.FieldAgent;
import edu.washington.cse.longan.MethodAgent;
import edu.washington.cse.longan.Session;
import edu.washington.cse.longan.tracker.IObjectTracker;

public class SessionXMLWriter {

	public void write(String fName, Session session) {
		Document doc = XMLTools.newXMLDocument();

		Element root = new Element(ILonganIO.ROOT);
		root.setAttribute(ILonganIO.DATE, TimeUtility.getCurrentLSMRDateString());

		List<MethodAgent> methods = new Vector<MethodAgent>(session.getMethods());
		List<FieldAgent> fields = new Vector<FieldAgent>(session.getFields());

		Collections.sort(methods, new Comparator<MethodAgent>() {
			public int compare(MethodAgent m1, MethodAgent m2) {
				return m1.getName().compareTo(m2.getName());
			}
		});

		Collections.sort(fields, new Comparator<FieldAgent>() {
			public int compare(FieldAgent f1, FieldAgent f2) {
				return f1.getName().compareTo(f2.getName());
			}
		});

		root.addContent(genStatic(methods, fields));
		root.addContent(genDynamic(methods, fields));

		doc.setRootElement(root);
		boolean success = XMLTools.writeXMLDocument(doc, fName);
	}

	Element genStatic(Collection<MethodAgent> methods, Collection<FieldAgent> fields) {
		Element staticElement = new Element(ILonganIO.STATIC);

		staticElement.addContent(genMethods(methods));
		staticElement.addContent(genFields(fields));

		return staticElement;
	}

	Element genDynamic(Collection<MethodAgent> methods, Collection<FieldAgent> fields) {
		Element staticElement = new Element(ILonganIO.DYNAMIC);

		for (MethodAgent method : methods) {

		}
		return staticElement;
	}

	Element genMethods(Collection<MethodAgent> methods) {
		Element methodsElement = new Element(ILonganIO.METHODS);

		for (MethodAgent method : methods) {
			Element methodElement = new Element(ILonganIO.METHOD);

			methodElement.setAttribute(ILonganIO.ID, method.getId() + "");
			methodElement.setAttribute(ILonganIO.NAME, method.getName() + "");

			Element returnElement = new Element(ILonganIO.RETURN);
			IObjectTracker returnTracker = method.getReturnTrackerDefinition();

			if (returnTracker != null) {
				returnElement.setAttribute(ILonganIO.TYPE, returnTracker.getStaticTypeName());
			} else if (method.hasVoidReturn()) {
				returnElement.setAttribute(ILonganIO.TYPE, "void");
			} else {
				returnElement.setAttribute(ILonganIO.TYPE, "<init>");
			}

			methodElement.addContent(returnElement);

			Element paramsElement = new Element(ILonganIO.PARAMETERS);

			IObjectTracker[] paramTrackers = method.getParameterTrackerDefinitions();
			for (int i = 0; i < paramTrackers.length; i++) {
				Element paramElement = new Element(ILonganIO.PARAMETER);
				IObjectTracker paramTracker = paramTrackers[i];

				paramElement.setAttribute(ILonganIO.POSITION, i + "");
				paramElement.setAttribute(ILonganIO.TYPE, paramTracker.getStaticTypeName());
				paramElement.setAttribute(ILonganIO.NAME, paramTracker.getName());

				paramsElement.addContent(paramElement);
			}

			methodElement.addContent(paramsElement);
			methodsElement.addContent(methodElement);
		}

		return methodsElement;
	}

	Element genFields(Collection<FieldAgent> fields) {
		Element fieldsElement = new Element(ILonganIO.FIELDS);

		return fieldsElement;
	}

	Element genDyanmic(Collection<MethodAgent> methods, Collection<FieldAgent> fields) {
		Element dynamicElement = new Element(ILonganIO.DYNAMIC);

		return dynamicElement;
	}

}

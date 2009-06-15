package edu.washington.cse.longan.io;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

import ca.lsmr.common.util.TimeUtility;
import ca.lsmr.common.util.xml.XMLTools;
import edu.washington.cse.longan.FieldAgent;
import edu.washington.cse.longan.MethodAgent;
import edu.washington.cse.longan.Session;
import edu.washington.cse.longan.tracker.IObjectTracker;
import edu.washington.cse.longan.trait.AbstractTrait;
import edu.washington.cse.longan.trait.ITrait;

public class SessionXMLWriter {

	Logger _log = Logger.getLogger(this.getClass());

	public void write(String fName, Session session) {
		Document doc = XMLTools.newXMLDocument();

		Element root = new Element(ILonganIO.ROOT);
		root.setAttribute(ILonganIO.DATE, TimeUtility.getCurrentLSMRDateString());

		root.addContent(genStatic(session));
		root.addContent(genDynamic(session));

		doc.setRootElement(root);
		boolean success = XMLTools.writeXMLDocument(doc, fName);
	}

	private Element genStatic(Session session) {
		Element staticElement = new Element(ILonganIO.STATIC);

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

		staticElement.addContent(genMethods(session.getMethods()));
		staticElement.addContent(genFields(session.getFields()));

		return staticElement;
	}

	// private Element genDynamic(Session session) {
	//
	// Element dynamicElement = new Element(ILonganIO.DYNAMIC);
	//
	// for (MethodAgent method : session.getMethods()) {
	//
	// }
	// return dynamicElement;
	// }

	private Element genMethods(Collection<MethodAgent> methods) {
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

	private Element genFields(Collection<FieldAgent> fields) {
		Element fieldsElement = new Element(ILonganIO.FIELDS);
		// TODO: add fields
		return fieldsElement;
	}

	private Element genDynamic(Session session) {
		Element dynamicElement = new Element(ILonganIO.DYNAMIC);

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

		Element methodsElement = new Element(ILonganIO.METHODS);
		for (MethodAgent method : methods) {
			Element methodElement = new Element(ILonganIO.METHOD);
			methodElement.setAttribute(ILonganIO.ID, method.getId() + "");
			methodElement.setAttribute(ILonganIO.TIME, session.getProfile().get(method.getId()) + "");

			Collection<Integer> uniqueCallers = method.getCalledBy();

			for (Integer caller : uniqueCallers) {

				MethodAgent calledBy = session.getMethod(caller);
				String calledByName = "";

				Element calledByElement = new Element(ILonganIO.CALLEDBY);

				if (calledBy != null) {
					calledByName = calledBy.getName();
					calledByElement.setAttribute(ILonganIO.ID, calledBy.getId() + "");
				} else {
					calledByName = ILonganIO.UNKNOWN_CALLER;
					calledByElement.setAttribute(ILonganIO.ID, ILonganIO.UNKNOWN_ID);
				}

				int calledByCount = session.getMethod(method.getId()).getCalledBy().count(caller);
				calledByElement.setAttribute(ILonganIO.COUNT, calledByCount + "");

				// _log.info("\t<-- id: " + caller + "; # calls: " + calledByCount + "; name: " + calledByName);

				IObjectTracker[] paramTracker = method.getParameterTrackers().get(caller);
				IObjectTracker returnTracker = method.getReturnTrackers().get(caller);

				Element paramsElement = new Element(ILonganIO.PARAMETERS);
				if (paramTracker.length > 0) {
					for (int i = 0; i < paramTracker.length; i++) {
						Element paramElement = new Element(ILonganIO.PARAMETER);
						paramElement.setAttribute(ILonganIO.POSITION, i + "");

						IObjectTracker tracker = paramTracker[i];

						// _log.info("\t\tParam: " + tracker.getTrackerName() + " - [ idx: " + tracker.getPosition()
						// + " ] name: " + tracker.getName() + " static type: " + tracker.getStaticTypeName());
						// _log.info("\t\t\t" + tracker.toString());

						// don't record tracker details, it's the traits that hold the useful information
						for (ITrait trait : tracker.getTraits()) {
							paramElement.addContent(((AbstractTrait) trait).toXML());
						}

						paramsElement.addContent(paramElement);
					}

				}
				calledByElement.addContent(paramsElement);

				if (returnTracker != null) {
//					_log.info("\t\tReturn: " + returnTracker.getTrackerName() + " static type: "
//							+ returnTracker.getStaticTypeName());
//					_log.info("\t\t\t" + returnTracker.toString());
					Element returnElement = new Element(ILonganIO.RETURN);

					// _log.info("\t\tParam: " + tracker.getTrackerName() + " - [ idx: " + tracker.getPosition()
					// + " ] name: " + tracker.getName() + " static type: " + tracker.getStaticTypeName());
					// _log.info("\t\t\t" + tracker.toString());

					// don't record tracker details, it's the traits that hold the useful information
					for (ITrait trait : returnTracker.getTraits()) {
						returnElement.addContent(((AbstractTrait) trait).toXML());
					}

					calledByElement.addContent(returnElement);
					
				}

				methodElement.addContent(calledByElement);
			}

			methodsElement.addContent(methodElement);
		}
		dynamicElement.addContent(methodsElement);

		// TODO: add fields

		return dynamicElement;
	}

}

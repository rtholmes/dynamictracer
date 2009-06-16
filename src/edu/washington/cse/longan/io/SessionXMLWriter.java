package edu.washington.cse.longan.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.SAXOutputter;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import ca.lsmr.common.util.TimeUtility;

import com.sun.org.apache.xalan.internal.xsltc.runtime.AttributeList;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

import edu.washington.cse.longan.model.Session;
import edu.washington.cse.longan.trace.AJFieldAgent;
import edu.washington.cse.longan.trace.AJMethodAgent;
import edu.washington.cse.longan.trace.tracker.IObjectTracker;
import edu.washington.cse.longan.trait.AbstractTrait;
import edu.washington.cse.longan.trait.ITrait;

public class SessionXMLWriter implements ILonganIO {

	Logger _log = Logger.getLogger(this.getClass());

	public void write(String fName, Session session) {
		Element staticData = genStatic(session);

		OutputFormat format = new OutputFormat();
		format.setIndenting(true);

		try {
			// init output infrastructure
			OutputStream out = new FileOutputStream(new File(fName));
			ContentHandler handler = new XMLSerializer(out, format);
			SAXOutputter saxo = new SAXOutputter(handler);

			// create document
			handler.startDocument();

			// set up root element
			AttributeList attrs = new AttributeList();
			attrs.add(ILonganIO.DATE, TimeUtility.getCurrentLSMRDateString());
			handler.startElement(null, null, ILonganIO.ROOT, attrs);

			// add static data. this should be manageable so don't bother SAXing it
			saxo.outputFragment(staticData);

			handler.startElement(null, null, ILonganIO.DYNAMIC, null);

			genDynamic(session, handler, saxo);

			handler.endElement(null, null, ILonganIO.DYNAMIC);

			// clean up
			handler.endElement(null, null, ILonganIO.ROOT);
			handler.endDocument();

		} catch (FileNotFoundException fnfe) {
			_log.error(fnfe);
		} catch (SAXException saxe) {
			_log.error(saxe);
		} catch (JDOMException jdome) {
			_log.error(jdome);
		}
	}

	private Element genStatic(Session session) {
		Element staticElement = new Element(ILonganIO.STATIC);

		List<AJMethodAgent> methods = new Vector<AJMethodAgent>(session.getMethods());
		List<AJFieldAgent> fields = new Vector<AJFieldAgent>(session.getFields());

		Collections.sort(methods, new Comparator<AJMethodAgent>() {
			public int compare(AJMethodAgent m1, AJMethodAgent m2) {
				return m1.getName().compareTo(m2.getName());
			}
		});

		Collections.sort(fields, new Comparator<AJFieldAgent>() {
			public int compare(AJFieldAgent f1, AJFieldAgent f2) {
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
	// for (AJMethodAgent method : session.getMethods()) {
	//
	// }
	// return dynamicElement;
	// }

	private Element genMethods(Collection<AJMethodAgent> methods) {
		Element methodsElement = new Element(ILonganIO.METHODS);

		for (AJMethodAgent method : methods) {
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

	private Element genFields(Collection<AJFieldAgent> fields) {
		Element fieldsElement = new Element(ILonganIO.FIELDS);
		// TODO: add fields
		return fieldsElement;
	}

	private void genDynamic(Session session, ContentHandler handler, SAXOutputter saxo) throws SAXException,
			JDOMException {
		// Element dynamicElement = new Element(ILonganIO.DYNAMIC);

		List<AJMethodAgent> methods = new Vector<AJMethodAgent>(session.getMethods());
		List<AJFieldAgent> fields = new Vector<AJFieldAgent>(session.getFields());

		Collections.sort(methods, new Comparator<AJMethodAgent>() {
			public int compare(AJMethodAgent m1, AJMethodAgent m2) {
				return m1.getName().compareTo(m2.getName());
			}
		});

		Collections.sort(fields, new Comparator<AJFieldAgent>() {
			public int compare(AJFieldAgent f1, AJFieldAgent f2) {
				return f1.getName().compareTo(f2.getName());
			}
		});

		// Element methodsElement = new Element(ILonganIO.METHODS);
		handler.startElement(null, null, ILonganIO.METHODS, null);

		for (AJMethodAgent method : methods) {
			Element methodElement = new Element(ILonganIO.METHOD);
			methodElement.setAttribute(ILonganIO.ID, method.getId() + "");
			methodElement.setAttribute(ILonganIO.TIME, session.getProfile().get(method.getId()) + "");

			Collection<Integer> uniqueCallers = method.getCalledBy();

			for (Integer caller : uniqueCallers) {

				AJMethodAgent calledBy = session.getMethod(caller);
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
					// _log.info("\t\tReturn: " + returnTracker.getTrackerName() + " static type: "
					// + returnTracker.getStaticTypeName());
					// _log.info("\t\t\t" + returnTracker.toString());
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

			// methodsElement.addContent(methodElement);
			saxo.outputFragment(methodElement);

		}
		handler.endElement(null, null, ILonganIO.METHODS);
		// dynamicElement.addContent(methodsElement);

		// TODO: add fields

		// return dynamicElement;
	}

}

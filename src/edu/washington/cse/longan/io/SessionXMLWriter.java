package edu.washington.cse.longan.io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.SAXOutputter;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import ca.lsmr.common.util.TimeUtility;

import com.google.common.base.Preconditions;
import com.sun.org.apache.xalan.internal.xsltc.runtime.AttributeList;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

import edu.washington.cse.longan.Logger;
import edu.washington.cse.longan.model.FieldElement;
import edu.washington.cse.longan.model.FieldTraitContainer;
import edu.washington.cse.longan.model.ILonganConstants;
import edu.washington.cse.longan.model.MethodElement;
import edu.washington.cse.longan.model.ParamTraitContainer;
import edu.washington.cse.longan.model.ReturnTraitContainer;
import edu.washington.cse.longan.model.Session;
import edu.washington.cse.longan.trait.AbstractTrait;
import edu.washington.cse.longan.trait.ExceptionTrait;
import edu.washington.cse.longan.trait.ITrait;

public class SessionXMLWriter implements ILonganIO {

	Logger _log = Logger.getLogger(this.getClass());

	public void write(String fName, Session session) {

		long start = System.currentTimeMillis();

		Element staticData = genStatic(session);

		OutputFormat format = new OutputFormat();
		format.setIndenting(true);

		try {
			// init output infrastructure
			OutputStream out;
			if (ILonganConstants.OUTPUT_ZIP) {
				ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(new File(fName + ".zip"))));
				ZipEntry ze = new ZipEntry(fName.substring(fName.lastIndexOf(File.separator)));
				zos.putNextEntry(ze);
				out = zos;

				// GZIPOutputStream gz = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(
				// new File(fName+".gz"))));
				// out = gz;

			} else {
				out = new BufferedOutputStream(new FileOutputStream(new File(fName)));

			}
			ContentHandler handler = new XMLSerializer(out, format);
			SAXOutputter saxo = new SAXOutputter(handler);

			_log.info("Writing trace to: " + fName);

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

			out.close();
		} catch (FileNotFoundException fnfe) {
			_log.error(fnfe);
		} catch (SAXException saxe) {
			_log.error(saxe);
		} catch (JDOMException jdome) {
			_log.error(jdome);
		} catch (IOException ioe) {
			_log.error(ioe);
		}

		if (ILonganConstants.OUTPUT_DEBUG) {
			// In debug mode it can be handy to send the last output to a static file name
			// for easier manual analysis

			long end = System.currentTimeMillis();
			String latestFName = ILonganConstants.OUTPUT_PATH + "latest.xml";

			try {
				// Create channel on the source
				FileChannel srcChannel = new FileInputStream(fName).getChannel();

				// Create channel on the destination
				FileChannel dstChannel = new FileOutputStream(latestFName).getChannel();

				// Copy file contents from source to destination
				dstChannel.transferFrom(srcChannel, 0, srcChannel.size());

				// Close the channels
				srcChannel.close();
				dstChannel.close();
			} catch (IOException ioe) {
				_log.error(ioe);
			}

			_log.info("Trace written in: " + TimeUtility.msToHumanReadable((end - start)) + " (copy took: " + TimeUtility.msToHumanReadableDelta(end)
					+ ") and copied to: " + latestFName);
		} else {
			_log.info("Trace written to: " + fName + " in: " + TimeUtility.msToHumanReadableDelta(start));
		}
	}

	private Element genStatic(Session session) {
		Element staticElement = new Element(ILonganIO.STATIC);

		List<MethodElement> methods = new Vector<MethodElement>(session.getMethods());
		List<FieldElement> fields = new Vector<FieldElement>(session.getFields());

		Collections.sort(methods, new Comparator<MethodElement>() {
			public int compare(MethodElement m1, MethodElement m2) {
				return m1.getName().compareTo(m2.getName());
			}
		});

		Collections.sort(fields, new Comparator<FieldElement>() {
			public int compare(FieldElement f1, FieldElement f2) {
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

	private Element genMethods(Collection<MethodElement> methods) {
		Element methodsElement = new Element(ILonganIO.METHODS);

		for (MethodElement method : methods) {
			Element methodElement = new Element(ILonganIO.METHOD);

			methodElement.setAttribute(ILonganIO.ID, method.getId() + "");
			methodElement.setAttribute(ILonganIO.EXTERNAL, method.isExternal() + "");
			methodElement.setAttribute(ILonganIO.NAME, method.getName() + "");

			Element returnElement = new Element(ILonganIO.RETURN);
			ReturnTraitContainer rtc = method.getReturnTraitContainer();
			// IObjectTracker returnTracker = method.getReturnTrackerDefinition();

			if (rtc != null) {
				returnElement.setAttribute(ILonganIO.TYPE, rtc.getStaticTypeName());
			} else if (method.hasVoidReturn()) {
				returnElement.setAttribute(ILonganIO.TYPE, ILonganConstants.VOID_RETURN);
			} else {
				returnElement.setAttribute(ILonganIO.TYPE, ILonganConstants.INIT_METHOD);
			}

			methodElement.addContent(returnElement);

			Element paramsElement = new Element(ILonganIO.PARAMETERS);

			Vector<ParamTraitContainer> ptcs = method.getParamTraitContainers();

			// IObjectTracker[] paramTrackers = method.getParameterTrackerDefinitions();
			for (ParamTraitContainer ptc : ptcs) {
				Element paramElement = new Element(ILonganIO.PARAMETER);
				// ParamTraitContainer ptc = ptcs.get(i);
				// IObjectTracker paramTracker = paramTrackers[i];

				paramElement.setAttribute(ILonganIO.POSITION, ptc.getPosition() + "");
				paramElement.setAttribute(ILonganIO.TYPE, ptc.getStaticTypeName());
				paramElement.setAttribute(ILonganIO.NAME, ptc.getName());

				paramsElement.addContent(paramElement);
			}

			methodElement.addContent(paramsElement);
			methodsElement.addContent(methodElement);
		}

		return methodsElement;
	}

	private Element genFields(Collection<FieldElement> fields) {
		Element fieldsElement = new Element(ILonganIO.FIELDS);

		for (FieldElement field : fields) {
			Element fieldElement = new Element(ILonganIO.FIELD);

			FieldTraitContainer ftc = null;
			ftc = field.getFieldGetTraitContainer();
			if (ftc == null)
				ftc = field.getFieldSetTraitContainer();

			Preconditions.checkNotNull(ftc, "We shouldn't know about it if there was never a get or set.");

			String fieldType = ftc.getStaticTypeName();

			fieldElement.setAttribute(ILonganIO.ID, field.getId() + "");
			fieldElement.setAttribute(ILonganIO.NAME, field.getName() + "");
			fieldElement.setAttribute(ILonganIO.TYPE, fieldType);

			fieldsElement.addContent(fieldElement);
		}
		return fieldsElement;
	}

	private void genDynamic(Session session, ContentHandler handler, SAXOutputter saxo) throws SAXException, JDOMException {
		// Element dynamicElement = new Element(ILonganIO.DYNAMIC);

		List<MethodElement> methods = new Vector<MethodElement>(session.getMethods());
		List<FieldElement> fields = new Vector<FieldElement>(session.getFields());

		Collections.sort(methods, new Comparator<MethodElement>() {
			public int compare(MethodElement m1, MethodElement m2) {
				return m1.getName().compareTo(m2.getName());
			}
		});

		Collections.sort(fields, new Comparator<FieldElement>() {
			public int compare(FieldElement f1, FieldElement f2) {
				return f1.getName().compareTo(f2.getName());
			}
		});

		handler.startElement(null, null, ILonganIO.METHODS, null);

		for (MethodElement method : methods) {
			Element methodElement = new Element(ILonganIO.METHOD);
			methodElement.setAttribute(ILonganIO.ID, method.getId() + "");

			Long time = session.getProfile().get(method.getId());
			if (time == null) {
				_log.warn("Null profile time for: " + method.getName());
				time = 0L;
			}
			methodElement.setAttribute(ILonganIO.TIME, time + "");

			// make the xml files easier to manually inspect (but larger)
			if (ILonganConstants.OUTPUT_DEBUG) {
				methodElement.setAttribute(ILonganIO.NAME, method.getName());
			}

			if (method.getExceptions().size() > 0) {
				Element exceptionsElement = new Element(ILonganIO.EXCEPTIONS);

				for (ExceptionTrait et : method.getExceptions().elementSet()) {
					Element exceptionElement = new Element(ILonganIO.EXCEPTION);
					exceptionElement.setAttribute(ILonganIO.SERIAL, et.toString());
					exceptionsElement.addContent(exceptionElement);
				}
				methodElement.addContent(exceptionsElement);
			}

			Collection<Integer> uniqueCallers = method.getCalledBy().elementSet();

			for (Integer caller : uniqueCallers) {

				MethodElement calledBy = session.getMethod(caller);
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

				// make the xml files easier to manually inspect (but larger)
				if (ILonganConstants.OUTPUT_DEBUG) {
					if (calledBy != null)
						calledByElement.setAttribute(ILonganIO.NAME, calledBy.getName());
				}

				ITrait[] returnTraits = null;
				if (method.getReturnTraitContainer() != null)
					returnTraits = method.getReturnTraitContainer().getTraitsForCaller(caller);

				Vector<ParamTraitContainer> ptcs = method.getParamTraitContainers();

				if (ILonganConstants.TRACK_TRAITS) {
					Element paramsElement = new Element(ILonganIO.PARAMETERS);

					for (ParamTraitContainer ptc : ptcs) {
						Element paramElement = new Element(ILonganIO.PARAMETER);
						paramElement.setAttribute(ILonganIO.POSITION, ptc.getPosition() + "");

						// don't record tracker details, it's the traits that hold the useful information
						for (ITrait trait : ptc.getTraitsForCaller(caller)) {
							paramElement.addContent(((AbstractTrait) trait).toXML());
						}

						paramsElement.addContent(paramElement);
					}

					calledByElement.addContent(paramsElement);

					if (returnTraits != null) {
						Element returnElement = new Element(ILonganIO.RETURN);

						// don't record tracker details, it's the traits that hold the useful information
						for (ITrait trait : returnTraits) {
							returnElement.addContent(((AbstractTrait) trait).toXML());
						}

						calledByElement.addContent(returnElement);

					}
				}

				methodElement.addContent(calledByElement);
			}

			// methodsElement.addContent(methodElement);
			// Note: this adds a xmlns="" to the method nodes
			// This happens because we can't control the NamespaceStack inside outputFragment
			saxo.outputFragment(methodElement);

		}
		handler.endElement(null, null, ILonganIO.METHODS);

		// RFE: add fields

		handler.startElement(null, null, ILonganIO.FIELDS, null);

		for (FieldElement field : fields) {
			Element fieldElement = new Element(ILonganIO.FIELD);
			fieldElement.setAttribute(ILonganIO.ID, field.getId() + "");

			// make the xml files easier to manually inspect (but larger)
			if (ILonganConstants.OUTPUT_DEBUG) {
				fieldElement.setAttribute(ILonganIO.NAME, field.getName());
			}

			// get traits
			FieldTraitContainer gftc = field.getFieldGetTraitContainer();
			FieldTraitContainer sftc = field.getFieldSetTraitContainer();
			Preconditions.checkArgument(!(sftc == null && gftc == null), ILonganConstants.NOT_POSSIBLE);

			if (gftc != null) {
				// Element getsElement = new Element(ILonganIO.GETBY);

				Collection<Integer> uniqueGetters = field.getGetBy().elementSet();
				for (Integer getById : uniqueGetters) {
					Element getElement = new Element(ILonganIO.GET);

					getElement.setAttribute(ILonganIO.ID, getById + "");
					getElement.setAttribute(ILonganIO.COUNT, field.getGetBy().count(getById) + "");

					if (ILonganConstants.OUTPUT_DEBUG) {
						getElement.setAttribute(ILonganIO.NAME, session.getElementNameForID(getById));
					}

					if (ILonganConstants.TRACK_TRAITS) {
						for (ITrait trait : field.getFieldGetTraitContainer().getTraitsForCaller(getById)) {
							getElement.addContent(((AbstractTrait) trait).toXML());
						}
					}

					// getsElement.addContent(getElement);
					fieldElement.addContent(getElement);
				}

			}

			// set traits
			if (sftc != null) {
				// Element setsElement = new Element(ILonganIO.SETBY);

				Collection<Integer> uniqueSetters = field.getSetBy().elementSet();
				for (Integer setById : uniqueSetters) {
					Element setElement = new Element(ILonganIO.SET);

					setElement.setAttribute(ILonganIO.ID, setById + "");
					setElement.setAttribute(ILonganIO.COUNT, field.getSetBy().count(setById) + "");

					if (ILonganConstants.OUTPUT_DEBUG) {
						setElement.setAttribute(ILonganIO.NAME, session.getElementNameForID(setById));
					}

					if (ILonganConstants.TRACK_TRAITS) {
						for (ITrait trait : field.getFieldSetTraitContainer().getTraitsForCaller(setById)) {
							setElement.addContent(((AbstractTrait) trait).toXML());
						}
					}
					// setsElement.addContent(setElement);
					fieldElement.addContent(setElement);
				}
				// fieldElement.addContent(setsElement);
			}

			saxo.outputFragment(fieldElement);

		}

		handler.endElement(null, null, ILonganIO.FIELDS);

	}
}

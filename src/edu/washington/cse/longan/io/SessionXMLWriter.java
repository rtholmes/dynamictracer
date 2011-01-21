package edu.washington.cse.longan.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Vector;

import ca.lsmr.common.util.TimeUtility;
import edu.washington.cse.longan.model.ILonganConstants;
import edu.washington.cse.longan.model.Session;

public class SessionXMLWriter extends ILonganIO {

	// Logger _log = Logger.getLogger(this.getClass());

	public void write(String fName, Session session) {

		PrintWriter out;
		try {
			out = new PrintWriter(fName);

			Hashtable<String, String> rootAttrs = new Hashtable<String, String>();
			rootAttrs.put("date", TimeUtility.getCurrentLSMRDateString());

			out.println(tag(ILonganIO.ROOT, rootAttrs, false));
			// AttributeList attrs = new AttributeList();
			// attrs.add(ILonganIO.DATE, new Date().toString());
			// handler.startElement(null, null, ILonganIO.ROOT, attrs);

			genStatic(session, out);

			// // set up root element
			//
			// // add static data. this should be manageable so don't bother SAXing it
			// saxo.outputFragment(staticData);

			out.println(tag(ILonganIO.DYNAMIC, null, false));
			// handler.startElement(null, null, ILonganIO.DYNAMIC, null);

			genDynamic(session, out);

			out.println(endTag(ILonganIO.DYNAMIC));
			// handler.endElement(null, null, ILonganIO.DYNAMIC);

			// clean up
			out.println(endTag(ILonganIO.ROOT));
			// handler.endElement(null, null, ILonganIO.ROOT);
			// handler.endDocument();

			out.close();

			System.out.println("Dynamic session written to: " + new File(fName).getAbsolutePath());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (ILonganConstants.OUTPUT_DEBUG) {
			// In debug mode it can be handy to send the last output to a static file name
			// for easier manual analysis

			long end = System.currentTimeMillis();
			String latestFName = ILonganConstants.OUTPUT_PATH + "latestDynamic.xml";

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
				System.out.println("Dynamic session written to: " + new File(latestFName).getAbsolutePath());
			} catch (IOException ioe) {
				System.err.println(ioe);
			}

			// System.out.println("Trace written in: " + TimeUtility.msToHumanReadable((end - start)) + " (copy took: "
			// +
			// TimeUtility.msToHumanReadableDelta(end)
			// + ") and copied to: " + latestFName);
		} else {
			System.out.println("Trace written to: " + fName);// + " in: " + TimeUtility.msToHumanReadableDelta(start));
		}
	}

	// public void write(String fName, Hashtable<Integer, HashSet<Integer>> callMap) {
	//
	// long start = System.currentTimeMillis();
	//
	// // reverse the call map
	//
	// Hashtable<Integer, HashSet<Integer>> reverseCallMap = new Hashtable<Integer, HashSet<Integer>>();
	// Enumeration<Integer> callerIDEnum = callMap.keys();
	// while (callerIDEnum.hasMoreElements()) {
	// int callerID = callerIDEnum.nextElement();
	//
	// for (int calleeID : callMap.get(callerID)) {
	// reverseCallMap.put(calleeID, new HashSet<Integer>());
	// }
	// }
	//
	// callerIDEnum = callMap.keys();
	// while (callerIDEnum.hasMoreElements()) {
	// int callerID = callerIDEnum.nextElement();
	//
	// for (int calleeID : callMap.get(callerID)) {
	// reverseCallMap.get(calleeID).add(callerID);
	// }
	// }
	//
	// callMap = reverseCallMap;
	//
	// PrintWriter out;
	// try {
	// out = new PrintWriter(fName);
	//
	// Hashtable<String, String> rootAttrs = new Hashtable<String, String>();
	// rootAttrs.put("date", TimeUtility.getCurrentLSMRDateString());
	//
	// out.println(tag(ILonganIO.ROOT, rootAttrs, false));
	// // AttributeList attrs = new AttributeList();
	// // attrs.add(ILonganIO.DATE, new Date().toString());
	// // handler.startElement(null, null, ILonganIO.ROOT, attrs);
	//
	// genStatic(callMap, out);
	//
	// // // set up root element
	// //
	// // // add static data. this should be manageable so don't bother SAXing it
	// // saxo.outputFragment(staticData);
	//
	// out.println(tag(ILonganIO.DYNAMIC, null, false));
	// // handler.startElement(null, null, ILonganIO.DYNAMIC, null);
	//
	// genDynamic(callMap, out);
	//
	// out.println(endTag(ILonganIO.DYNAMIC));
	// // handler.endElement(null, null, ILonganIO.DYNAMIC);
	//
	// // clean up
	// out.println(endTag(ILonganIO.ROOT));
	// // handler.endElement(null, null, ILonganIO.ROOT);
	// // handler.endDocument();
	//
	// out.close();
	//
	// System.out.println("Dynamic session written to: " + fName);
	// } catch (FileNotFoundException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	// if (ILonganConstants.OUTPUT_DEBUG) {
	// // In debug mode it can be handy to send the last output to a static file name
	// // for easier manual analysis
	//
	// long end = System.currentTimeMillis();
	// String latestFName = ILonganConstants.OUTPUT_PATH + "latestDynamic.xml";
	//
	// try {
	// // Create channel on the source
	// FileChannel srcChannel = new FileInputStream(fName).getChannel();
	//
	// // Create channel on the destination
	// FileChannel dstChannel = new FileOutputStream(latestFName).getChannel();
	//
	// // Copy file contents from source to destination
	// dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
	//
	// // Close the channels
	// srcChannel.close();
	// dstChannel.close();
	// System.out.println("Dynamic session written to: " + latestFName);
	// } catch (IOException ioe) {
	// System.err.println(ioe);
	// }
	//
	// // System.out.println("Trace written in: " + TimeUtility.msToHumanReadable((end - start)) + " (copy took: "
	// // +
	// // TimeUtility.msToHumanReadableDelta(end)
	// // + ") and copied to: " + latestFName);
	// } else {
	// System.out.println("Trace written to: " + fName);// + " in: " + TimeUtility.msToHumanReadableDelta(start));
	// }
	// }

	private void genStatic(Session session, PrintWriter out) {
		// Element staticElement = new Element(ILonganIO.STATIC);
		out.println(tag(ILonganIO.STATIC, null, false));

		// List<MethodElement> methods = new Vector<MethodElement>(session.getMethods());
		// List<FieldElement> fields = new Vector<FieldElement>(session.getFields());

		// Vector<String> methods = new Vector<String>();
		// methods.add("::unknownM::");
		//
		// for (Integer methodID : callMap.keySet()) {
		// if (methodID >= 0) {
		// methods.add(MethodInstrumentor.getMethodSignature(methodID));
		// }
		// }
		//
		// Collections.sort(methods);

		// Collections.sort(fields, new Comparator<FieldElement>() {
		// public int compare(FieldElement f1, FieldElement f2) {
		// return f1.getName().compareTo(f2.getName());
		// }
		// });

		genMethods(session, out);

		out.println(endTag(ILonganIO.METHODS));
		out.println(endTag(ILonganIO.STATIC));
		// staticElement.addContent(genFields(session.getFields()));

		// return staticElement;
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

	// private void genMethods(Vector<>, PrintWriter out) {
	private void genMethods(Session session, PrintWriter out) {
		// Element methodsElement = new Element(ILonganIO.METHODS);
		out.println(tag(ILonganIO.METHODS, null, false));

		// Hashtable<String, Integer> nameIdMap = new Hashtable<String, Integer>();

		Vector<String> nameList = new Vector<String>();
		nameList.addAll(session.getMethodNames());

		// for (int mID : session.keySet()) {
		// String methodName = "::unknownM::";
		// if (mID >= 0) {
		// methodName = MethodInstrumentor.getMethodSignature(mID);
		// }
		// nameIdMap.put(methodName, mID);
		// nameList.add(methodName);
		// }

		Collections.sort(nameList);

		for (String methodName : nameList) {
			// int mID = nameIdMap.get(methodName);
			int mID = -1;
			if (session.hasIDForElement(methodName))
				mID = session.getIdForElement(methodName);

			// for (int mID : callMap.keySet()) {
			// String methodName = "::unknownM::";
			// if (mID >= 0) {
			// methodName = MethodInstrumentor.getMethodSignature(mID);
			// }
			// Element methodElement = new Element(ILonganIO.METHOD);

			Hashtable<String, String> attrs = new Hashtable<String, String>();
			attrs.put(ILonganIO.ID, mID + "");
			attrs.put(ILonganIO.NAME, transFQN(methodName));

			// methodElement.setAttribute(ILonganIO.ID, mID + "");
			// methodElement.setAttribute(ILonganIO.EXTERNAL, "UNKNOWN");
			// methodElement.setAttribute(ILonganIO.NAME, methodName);

			out.println(tag(ILonganIO.METHOD, attrs, true));

			// Element returnElement = new Element(ILonganIO.RETURN);
			// ReturnTraitContainer rtc = method.getReturnTraitContainer();
			// // IObjectTracker returnTracker = method.getReturnTrackerDefinition();
			//
			// if (rtc != null) {
			// returnElement.setAttribute(ILonganIO.TYPE, rtc.getStaticTypeName());
			// } else if (method.hasVoidReturn()) {
			// returnElement.setAttribute(ILonganIO.TYPE, ILonganConstants.VOID_RETURN);
			// } else {
			// returnElement.setAttribute(ILonganIO.TYPE, ILonganConstants.INIT_METHOD);
			// }
			// methodElement.addContent(returnElement);
			//
			// Element paramsElement = new Element(ILonganIO.PARAMETERS);
			//
			// Vector<ParamTraitContainer> ptcs = method.getParamTraitContainers();
			//
			// // IObjectTracker[] paramTrackers = method.getParameterTrackerDefinitions();
			// for (ParamTraitContainer ptc : ptcs) {
			// Element paramElement = new Element(ILonganIO.PARAMETER);
			// // ParamTraitContainer ptc = ptcs.get(i);
			// // IObjectTracker paramTracker = paramTrackers[i];
			//
			// paramElement.setAttribute(ILonganIO.POSITION, ptc.getPosition() + "");
			// paramElement.setAttribute(ILonganIO.TYPE, ptc.getStaticTypeName());
			// paramElement.setAttribute(ILonganIO.NAME, ptc.getName());
			//
			// paramsElement.addContent(paramElement);
			// }
			//
			// methodElement.addContent(paramsElement);
			// methodsElement.addContent(methodElement);
		}

		// return methodsElement;
	}

	private String transFQN(String name) {
		name = name.replace("/", ".");
		name = name.replace("<", "&lt;");
		name = name.replace(">", "&gt;");

		name = name.substring(0, name.indexOf(")") + 1);

		return name;
	}

	private String endTag(String text) {
		return "</" + text + ">";
	}

	private String tag(String text, Hashtable<String, String> attributes, boolean oneLine) {
		String tag = "";

		if (attributes == null || attributes.size() == 0) {
			tag = "<" + text;
		} else {
			tag = "<" + text;
			for (String key : attributes.keySet()) {
				String value = attributes.get(key);
				tag += " " + key + "=\"" + value + "\"";
			}

		}
		if (oneLine) {
			tag += "/>";
		} else {
			tag += ">";
		}
		return tag;
	}

	// private Element genFields(Collection<FieldElement> fields) {
	// Element fieldsElement = new Element(ILonganIO.FIELDS);
	//
	// for (FieldElement field : fields) {
	// Element fieldElement = new Element(ILonganIO.FIELD);
	//
	// FieldTraitContainer ftc = null;
	// ftc = field.getFieldGetTraitContainer();
	// if (ftc == null)
	// ftc = field.getFieldSetTraitContainer();
	//
	// // Preconditions.checkNotNull(ftc, "We shouldn't know about it if there was never a get or set.");
	// String fieldType;
	// if (ftc == null)
	// fieldType = "";
	// else
	// fieldType = ftc.getStaticTypeName();
	//
	// fieldElement.setAttribute(ILonganIO.ID, field.getId() + "");
	// fieldElement.setAttribute(ILonganIO.NAME, field.getName() + "");
	// fieldElement.setAttribute(ILonganIO.TYPE, fieldType);
	//
	// fieldsElement.addContent(fieldElement);
	// }
	// return fieldsElement;
	// }

	private void genDynamic(Session session, PrintWriter out) {
		// Element dynamicElement = new Element(ILonganIO.DYNAMIC);

		// List<MethodElement> methods = new Vector<MethodElement>(session.getMethods());
		// List<FieldElement> fields = new Vector<FieldElement>(session.getFields());
		//
		// Collections.sort(methods, new Comparator<MethodElement>() {
		// public int compare(MethodElement m1, MethodElement m2) {
		// return m1.getName().compareTo(m2.getName());
		// }
		// });
		//
		// Collections.sort(fields, new Comparator<FieldElement>() {
		// public int compare(FieldElement f1, FieldElement f2) {
		// return f1.getName().compareTo(f2.getName());
		// }
		// });

		out.println(tag(ILonganIO.METHODS, null, false));
		// handler.startElement(null, null, ILonganIO.METHODS, null);

		// for (MethodElement method : methods) {

		Vector<String> methodNames = new Vector<String>();
		methodNames.addAll(session.getMethodNames());
		Collections.sort(methodNames);

		// Enumeration<Integer> callerIDEnum = callMap.keys();
		// while (callerIDEnum.hasMoreElements()) {

		for (String methodName : methodNames) {
			int callerID = session.getIdForElement(methodName);

			// int callerID = callerIDEnum.nextElement();
			// for (int callerID : map.keySet()) {

			// for (int calleeID : callMap.get(callerID)) {

			// Element methodElement = new Element(ILonganIO.METHOD);
			Hashtable<String, String> mAttrs = new Hashtable<String, String>();

			mAttrs.put(ILonganIO.ID, callerID + "");
			if (ILonganConstants.OUTPUT_DEBUG) {
				// String methodName = "::unknownM::";
				// if (callerID >= 0) {
				// if (session.getMethod(callerID) != null)
				// methodName = session.getMethod(callerID).getName();
				// // methodElement.setAttribute(ILonganIO.NAME, methodName);
				// }
				mAttrs.put(ILonganIO.NAME, transFQN(methodName));
			}

			out.println(tag(ILonganIO.METHOD, mAttrs, false));
			// methodElement.setAttribute(ILonganIO.ID, callerID + "");

			// Long time = session.getProfile().get(method.getId());
			// if (time == null) {
			// _log.warn("Null profile time for: " + method.getName());
			// time = 0L;
			// }
			// methodElement.setAttribute(ILonganIO.TIME, time + "");

			// make the xml files easier to manually inspect (but larger)

			// if (method.getExceptions().size() > 0) {
			// Element exceptionsElement = new Element(ILonganIO.EXCEPTIONS);
			//
			// for (ExceptionTrait et : method.getExceptions().elementSet()) {
			// Element exceptionElement = new Element(ILonganIO.EXCEPTION);
			// exceptionElement.setAttribute(ILonganIO.SERIAL, et.toString());
			// exceptionsElement.addContent(exceptionElement);
			// }
			// methodElement.addContent(exceptionsElement);
			// }

			// Collection<Integer> uniqueCallers = method.getCalledBy().elementSet();

			// for (Integer caller : uniqueCallers) {

			for (int calleeID : session.getMethod(callerID).getCalledBy()) {

				// MethodElement calledBy = session.getMethod(caller);
				// String calledByName = "";

				// Element calledByElement = new Element(ILonganIO.CALLEDBY);
				Hashtable<String, String> cAttrs = new Hashtable<String, String>();

				cAttrs.put(ILonganIO.ID, calleeID + "");
				if (ILonganConstants.OUTPUT_DEBUG) {

					String name = "::unknownM::";
					if (calleeID >= 0) {
						if (session.methodExists(calleeID))
							name = session.getMethod(calleeID).getName();
						// name = MethodInstrumentor.getMethodSignature(calleeID);
						// calledByElement.setAttribute(ILonganIO.NAME, name);
					}
					cAttrs.put(ILonganIO.NAME, transFQN(name));
				}

				out.println(tag(ILonganIO.CALLEDBY, cAttrs, true));

			}
			out.println(endTag(ILonganIO.METHOD));
			// methodsElement.addContent(methodElement);
			// Note: this adds a xmlns="" to the method nodes
			// This happens because we can't control the NamespaceStack inside outputFragment
			// saxo.outputFragment(methodElement);

		}
		out.println(endTag(ILonganIO.METHODS));
		// handler.endElement(null, null, ILonganIO.METHODS);

		// RFE: add fields

		// handler.startElement(null, null, ILonganIO.FIELDS, null);

		// for (FieldElement field : fields) {
		// Element fieldElement = new Element(ILonganIO.FIELD);
		// fieldElement.setAttribute(ILonganIO.ID, field.getId() + "");
		//
		// // make the xml files easier to manually inspect (but larger)
		// if (ILonganConstants.OUTPUT_DEBUG) {
		// fieldElement.setAttribute(ILonganIO.NAME, field.getName());
		// }
		//
		// // get traits
		// FieldTraitContainer gftc = field.getFieldGetTraitContainer();
		// FieldTraitContainer sftc = field.getFieldSetTraitContainer();
		// // Preconditions.checkArgument(!(sftc == null && gftc == null), ILonganConstants.NOT_POSSIBLE);
		//
		// if (gftc != null) {
		// // Element getsElement = new Element(ILonganIO.GETBY);
		//
		// Collection<Integer> uniqueGetters = field.getGetBy().elementSet();
		// for (Integer getById : uniqueGetters) {
		// Element getElement = new Element(ILonganIO.GET);
		//
		// getElement.setAttribute(ILonganIO.ID, getById + "");
		// getElement.setAttribute(ILonganIO.COUNT, field.getGetBy().count(getById) + "");
		//
		// if (ILonganConstants.OUTPUT_DEBUG) {
		// getElement.setAttribute(ILonganIO.NAME, session.getElementNameForID(getById));
		// }
		//
		// if (ILonganConstants.TRACK_TRAITS) {
		// for (ITrait trait : field.getFieldGetTraitContainer().getTraitsForCaller(getById)) {
		// getElement.addContent(((AbstractTrait) trait).toXML());
		// }
		// }
		//
		// // getsElement.addContent(getElement);
		// fieldElement.addContent(getElement);
		// }
		//
		// }
		//
		// // set traits
		// if (sftc != null) {
		// // Element setsElement = new Element(ILonganIO.SETBY);
		//
		// Collection<Integer> uniqueSetters = field.getSetBy().elementSet();
		// for (Integer setById : uniqueSetters) {
		// Element setElement = new Element(ILonganIO.SET);
		//
		// setElement.setAttribute(ILonganIO.ID, setById + "");
		// setElement.setAttribute(ILonganIO.COUNT, field.getSetBy().count(setById) + "");
		//
		// if (ILonganConstants.OUTPUT_DEBUG) {
		// setElement.setAttribute(ILonganIO.NAME, session.getElementNameForID(setById));
		// }
		//
		// if (ILonganConstants.TRACK_TRAITS) {
		// for (ITrait trait : field.getFieldSetTraitContainer().getTraitsForCaller(setById)) {
		// setElement.addContent(((AbstractTrait) trait).toXML());
		// }
		// }
		// // setsElement.addContent(setElement);
		// fieldElement.addContent(setElement);
		// }
		// // fieldElement.addContent(setsElement);
		// }
		//
		// saxo.outputFragment(fieldElement);
		//
		// }

		// handler.endElement(null, null, ILonganIO.FIELDS);

	}
}

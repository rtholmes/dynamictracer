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

import org.apache.log4j.Logger;

import ca.lsmr.common.util.TimeUtility;
import edu.washington.cse.longan.model.ILonganConstants;
import edu.washington.cse.longan.model.MethodElement;
import edu.washington.cse.longan.model.Session;

public class SessionXMLWriter extends ILonganIO {

	Logger _log = Logger.getLogger(this.getClass());

	PrintWriter out;

	public void write(String fName, Session session) {

		try {
			prepareSessionForPersistence(session);

			out = new PrintWriter(fName);
			
			Hashtable<String, String> rootAttrs = new Hashtable<String, String>();
			rootAttrs.put("date", TimeUtility.getCurrentLSMRDateString());

			indent().println(tag(ILonganIO.ROOT, rootAttrs, false));

			raiseIndent();

			System.out.println("genning static");
			genStatic(session, out);
			System.out.println("genning static done ");
			
			System.out.println("genning dynamic");
			genDynamic(session, out);
			System.out.println("genning dynamic done");

			// clean up
			lowerIndent();
			out.println(endTag(ILonganIO.ROOT));

			out.close();

			System.out.println("Dynamic session written to: " + new File(fName).getAbsolutePath());

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		if (ILonganConstants.OUTPUT_DEBUG) {
			// In debug mode it can be handy to send the last output to a static file name
			// for easier manual analysis

			long end = System.currentTimeMillis();
			String latestFName = ILonganConstants.OUTPUT_PATH + "dynamic_latest.xml";

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

	private void prepareSessionForPersistence(Session session) {
		collapseSyntheticAccessMethods(session);
	}

	private void collapseSyntheticAccessMethods(Session session) {
		Vector<MethodElement> accessMethodsToRemove = new Vector<MethodElement>();

		System.out.println("Checking for suspected synthetic access methods");
		for (MethodElement me : session.getMethods()) {

			// $ is restricted, but this might still be able to collide with an anonymous class name?
			if (me.getName().contains(".access$")) {
				System.out.println("Remapping / removing suspected access method: " + me.getName());
				// _log.debug("Access method encountered: " + me);

				// find all methods that call this method me
				Vector<MethodElement> callers = findMethodsThatCall(me, session);

				// remap callers so this method can be deleted
				for (MethodElement caller : callers) {
					// remove all instances
					caller.getCalledBy().elementSet().remove(me.getId());
					caller.getCalledBy().addAll(me.getCalledBy().elementSet());
				}

				accessMethodsToRemove.add(me);
			}
		}
		System.out.println("Done collapsing suspected synthetic access methods ( "+accessMethodsToRemove.size()+" removed )");

		for (MethodElement accessMethod : accessMethodsToRemove) {
			// once they're collapsed get rid of them
			session.removeMethod(accessMethod);
		}
	}

	private Vector<MethodElement> findMethodsThatCall(MethodElement me, Session session) {
		Vector<MethodElement> callers = new Vector<MethodElement>();
		for (MethodElement m : session.getMethods()) {
			for (int mid : m.getCalledBy().elementSet()) {
				if (mid == me.getId()) {
					callers.add(session.getMethod(m.getId()));
				}
			}
		}
		return callers;
	}

	private void genStatic(Session session, PrintWriter out) {

		indent().println(tag(ILonganIO.STATIC, null, false));
		raiseIndent();

		genMethods(session, out);

		lowerIndent();
		indent().println(endTag(ILonganIO.STATIC));
	}

	private void genMethods(Session session, PrintWriter out) {

		indent().println(tag(ILonganIO.METHODS, null, false));
		raiseIndent();

		Vector<String> nameList = new Vector<String>();
		nameList.addAll(session.getMethodNames());

		Collections.sort(nameList);

		for (String methodName : nameList) {

			int mID = -1;
			if (session.hasIDForElement(methodName))
				mID = session.getIdForElement(methodName);

			Hashtable<String, String> attrs = new Hashtable<String, String>();
			attrs.put(ILonganIO.ID, mID + "");
			attrs.put(ILonganIO.NAME, transFQN(methodName));

			indent().println(tag(ILonganIO.METHOD, attrs, true));
		}

		lowerIndent();
		indent().println(endTag(ILonganIO.METHODS));
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

	private void genDynamic(Session session, PrintWriter out) {
		indent().println(tag(ILonganIO.DYNAMIC, null, false));
		raiseIndent();

		indent().println(tag(ILonganIO.METHODS, null, false));
		raiseIndent();

		Vector<String> methodNames = new Vector<String>();
		methodNames.addAll(session.getMethodNames());
		Collections.sort(methodNames);

		for (String methodName : methodNames) {
			int callerID = session.getIdForElement(methodName);
			Hashtable<String, String> mAttrs = new Hashtable<String, String>();

			mAttrs.put(ILonganIO.ID, callerID + "");
			if (ILonganConstants.OUTPUT_DEBUG) {
				mAttrs.put(ILonganIO.NAME, transFQN(methodName));
			}

			indent().println(tag(ILonganIO.METHOD, mAttrs, false));
			raiseIndent();
			for (int calleeID : session.getMethod(callerID).getCalledBy().elementSet()) {
				Hashtable<String, String> cAttrs = new Hashtable<String, String>();

				cAttrs.put(ILonganIO.ID, calleeID + "");
				if (ILonganConstants.OUTPUT_DEBUG) {

					String name = "::unknownM::";
					if (calleeID >= 0) {
						if (session.methodExists(calleeID))
							name = session.getMethod(calleeID).getName();
					}
					cAttrs.put(ILonganIO.NAME, transFQN(name));
				}

				indent().println(tag(ILonganIO.CALLEDBY, cAttrs, true));

			}
			lowerIndent();
			indent().println(endTag(ILonganIO.METHOD));
		}
		lowerIndent();
		indent().println(endTag(ILonganIO.METHODS));

		// TODO: handle fields

		lowerIndent();
		indent().println(endTag(ILonganIO.DYNAMIC));
	}

	int indentLevel = 0;

	private PrintWriter indent() {
		for (int i = 0; i < indentLevel; i++) {
			out.print("   ");
		}
		return out;
	}

	private PrintWriter raiseIndent() {
		indentLevel++;
		return out;
	}

	private PrintWriter lowerIndent() {
		indentLevel--;
		return out;
	}
}

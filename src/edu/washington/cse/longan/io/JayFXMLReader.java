package edu.washington.cse.longan.io;

import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import edu.washington.cse.longan.model.FieldElement;
import edu.washington.cse.longan.model.MethodElement;
import edu.washington.cse.longan.model.Session;

public class JayFXMLReader {

	private int _nextId = 0;

	@SuppressWarnings("unchecked")
	public Session read(Document doc, String fName) {
		Session session = new Session(fName);

		Element root = doc.getRootElement();
		Element edgesElement = root.getChild("edges");
		if (edgesElement != null) {

			// get the method nodes
			for (Element callElement : (List<Element>) edgesElement.getChildren("call")) {
				String sourceName = callElement.getAttributeValue("source");
				String targetName = callElement.getAttributeValue("target");

				// both source and target are method names
				MethodElement source = getMethod(sourceName, session);
				MethodElement target = getMethod(targetName, session);

				target.getCalledBy().add(source.getId());
			}

			// get the field nodes
			for (Element callElement : (List<Element>) edgesElement.getChildren("ref")) {
				String sourceName = callElement.getAttributeValue("source");
				String targetName = callElement.getAttributeValue("target");

				// source is a method, target is a field
				MethodElement source = getMethod(sourceName, session);
				FieldElement target = convertFieldName(targetName, session);

				target.getGetBy().add(source.getId());
			}

		}

		return session;
	}

	private FieldElement convertFieldName(String fieldName, Session session) {

		fieldName = translateFieldName(fieldName);

		int id = -1;
		if (!session.hasIDForElement(fieldName)) {
			id = _nextId++;
		} else {
			id = session.getIdForElement(fieldName);
		}
		if (!session.fieldExists(id)) {
			session.addField(id, new FieldElement(id, fieldName));
		}

		return session.getFieldForName(fieldName);

	}

	private String translateFieldName(String fieldName) {

		return fieldName.replace("$", ".");
	}

	private MethodElement getMethod(String methodName, Session session) {

		// XXX: convert method name
		methodName = translateMethodName(methodName);

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

	private String translateMethodName(String fullName) {
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

}

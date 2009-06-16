package edu.washington.cse.longan.io;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import edu.washington.cse.longan.model.MethodElement;
import edu.washington.cse.longan.model.Session;

public class SessionXMLReaderHandler extends DefaultHandler {
	Logger _log = Logger.getLogger(this.getClass());

	boolean rootElem = false;
	boolean staticElem = false;
	boolean methodsElem = false;
	boolean methodElem = false;
	boolean dynamicElem = false;
	boolean dataElem = false;
	boolean traitElem = false;
	boolean paramsElem = false;
	boolean paramElem = false;
	boolean returnElem = false;

	private Session _session;

	private MethodElement _lastStaticMethod;

	public SessionXMLReaderHandler(Session session) {
		_session = session;
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();

		_log.info("startDocument");
	}

	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
		_log.info("endDocument");
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		_log.debug("startElement: " + qName);

		if (qName.equals(ILonganIO.DATA)) {
			dataElem = true;
		} else if (qName.equals(ILonganIO.TRAIT)) {
			traitElem = true;
		} else if (qName.equals(ILonganIO.PARAMETERS)) {
			paramsElem = true;
		} else if (qName.equals(ILonganIO.RETURN)) {
			returnElem = true;
		} else if (qName.equals(ILonganIO.PARAMETER)) {
			paramElem = true;
		} else if (qName.equals(ILonganIO.METHOD)) {
			methodElem = true;

			if (staticElem) {
				parseStaticMethodAttrs(attributes);
			}
			
		} else if (qName.equals(ILonganIO.ROOT)) {
			rootElem = true;
			parseRootAttributes(attributes);
		} else if (qName.equals(ILonganIO.DYNAMIC)) {
			dynamicElem = true;
		} else if (qName.equals(ILonganIO.STATIC)) {
			staticElem = true;
		}
	}

	private void parseStaticMethodAttrs(Attributes attributes) {
		String idString = attributes.getValue(ILonganIO.ID);
		String name = attributes.getValue(ILonganIO.NAME);
		_log.info("Method parsed: " + idString + " " + name);
		int id = Integer.parseInt(idString);

		MethodElement me = new MethodElement(id, name);
		_session.addIDForElement(name, id);
		_session.addMethod(id, me);

		_lastStaticMethod = me;
	}

	private void parseRootAttributes(Attributes attributes) {
		// TODO Auto-generated method stub

	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		super.endElement(uri, localName, qName);
		_log.debug("endElement: " + qName);

		if (qName.equals(ILonganIO.DATA)) {
			dataElem = false;
		} else if (qName.equals(ILonganIO.TRAIT)) {
			traitElem = false;
		} else if (qName.equals(ILonganIO.PARAMETERS)) {
			paramsElem = false;
		} else if (qName.equals(ILonganIO.RETURN)) {
			returnElem = false;
		} else if (qName.equals(ILonganIO.PARAMETER)) {
			paramElem = false;
		} else if (qName.equals(ILonganIO.METHOD)) {
			methodElem = false;
			if (staticElem)
				_lastStaticMethod = null;
		} else if (qName.equals(ILonganIO.ROOT)) {
			rootElem = false;
		} else if (qName.equals(ILonganIO.DYNAMIC)) {
			dynamicElem = false;
		} else if (qName.equals(ILonganIO.STATIC)) {
			staticElem = false;
		}
	}
}

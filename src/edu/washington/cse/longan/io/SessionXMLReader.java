package edu.washington.cse.longan.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.xml.sax.SAXException;

import ca.lsmr.common.util.TimeUtility;
import ca.lsmr.common.util.xml.XMLTools;

import com.google.common.base.Preconditions;

import edu.washington.cse.longan.model.Session;

public class SessionXMLReader implements ILonganIO {
	Logger _log = Logger.getLogger(this.getClass());

	public Session readXML(String fName) {

		long start = System.currentTimeMillis();

		_log.info("Reading session from: " + fName);

		Session session = null;

		try {

			Document d = XMLTools.readXMLDocument(fName);

			if (d == null) {
				_log.error("Session not loaded from: " + fName);
				return null;
			}

			// dispatch. this would be better with sax because we wouldn't read the whole thing first
			if (d.getRootElement().getName().equals(IGilliganStoreIO.XML_ROOT)) {
				_log.info("Reading static trace (from Gilligan)");
				GilliganXMLReader gxmlr = new GilliganXMLReader();
				session = gxmlr.read(d, fName);
			} else {
				_log.info("Reading dynamic trace (from Longan)");
				FileInputStream is = new FileInputStream(new File(fName));
				SAXParser saxp = SAXParserFactory.newInstance().newSAXParser();
				SessionXMLReaderHandler dh = new SessionXMLReaderHandler();

				saxp.parse(is, dh);

				session = dh.getSession();
			}

		} catch (FileNotFoundException fnfe) {
			_log.error(fnfe);
		} catch (ParserConfigurationException pce) {
			_log.error(pce);
		} catch (SAXException saxe) {
			_log.error(saxe);
		} catch (IOException ioe) {
			_log.error(ioe);
		}

		Preconditions.checkNotNull(session);
		_log.info("Session read (in: " + TimeUtility.msToHumanReadableDelta(start) + ")");
		return session;
	}
}

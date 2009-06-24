package edu.washington.cse.longan.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import ca.lsmr.common.util.TimeUtility;

import com.google.common.base.Preconditions;

import edu.washington.cse.longan.model.Session;

public class SessionXMLReader implements ILonganIO {
	Logger _log = Logger.getLogger(this.getClass());

	public Session readXML(String fName) {
		long start = System.currentTimeMillis();

		_log.info("Reading session from: " + fName);

		Session session = null;

		try {
			FileInputStream is = new FileInputStream(new File(fName));

			SAXParser saxp = SAXParserFactory.newInstance().newSAXParser();
			SessionXMLReaderHandler dh = new SessionXMLReaderHandler();

			saxp.parse(is, dh);

			session = dh.getSession();

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

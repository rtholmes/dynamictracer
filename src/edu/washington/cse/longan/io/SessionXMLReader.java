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
import org.xml.sax.helpers.DefaultHandler;

import edu.washington.cse.longan.model.Session;

public class SessionXMLReader implements ILonganIO {
	Logger _log = Logger.getLogger(this.getClass());

	public Session readXML(String fName) {
		_log.info("Loading session from: " + fName);
		Session session = new Session();

		try {
			FileInputStream is = new FileInputStream(new File(fName));
			
			
			SAXParser saxp = SAXParserFactory.newInstance().newSAXParser();
			DefaultHandler dh = new SessionXMLReaderHandler(session);
			saxp.parse(is, dh);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		_log.info("Document read");
		return session;
	}
}

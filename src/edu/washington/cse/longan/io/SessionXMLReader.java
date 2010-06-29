package edu.washington.cse.longan.io;

import org.apache.log4j.Logger;
import org.jdom.Document;

import ca.lsmr.common.util.TimeUtility;
import ca.lsmr.common.util.xml.XMLTools;

import com.google.common.base.Preconditions;

import edu.washington.cse.longan.model.Session;

public class SessionXMLReader extends ILonganIO {
	Logger _log = Logger.getLogger(this.getClass());

	public Session readXML(String fName) {

		long start = System.currentTimeMillis();

		_log.debug("Reading session from: " + fName);

		Session session = null;

		try {

			Document d = XMLTools.readXMLDocument(fName);

			if (d == null) {
				_log.error("Session not loaded from: " + fName);
				return null;
			}

			// dispatch. this would be better with sax because we wouldn't read the whole thing first
			if (d.getRootElement().getName().equals("root")) {
				_log.error("Shouldn't be using DSA traces, they don't contain constructors or nested calls");
				_log.debug("Reading static trace (from DSA)");
				DSAXMLReader dsaxmlr = new DSAXMLReader();
				session = dsaxmlr.read(d, fName);
			} else if (d.getRootElement().getName().equals("JayFX")) {
				_log.debug("Reading static trace (from JayFX)");
				JayFXMLReader jayfxmlr = new JayFXMLReader();
				session = jayfxmlr.read(d, fName);
			} else if (d.getRootElement().getName().equals(IGilliganStoreIO.XML_ROOT)) {
				_log.debug("Reading static trace (from Gilligan)");
				GilliganXMLReader gxmlr = new GilliganXMLReader();
				session = gxmlr.read(d, fName);
			} else {
				_log.debug("Reading dynamic trace (from Longan)");
				// FileInputStream is = new FileInputStream(new File(fName));
				// SAXParser saxp = SAXParserFactory.newInstance().newSAXParser();
				// SessionXMLReaderHandler dh = new SessionXMLReaderHandler();
				//
				// saxp.parse(is, dh);
				//
				// session = dh.getSession();

				LonganDynamicXMLReader ldxmlr = new LonganDynamicXMLReader();
				session = ldxmlr.read(d, fName);

				session.setSessionName(fName);
			}

			// } catch (FileNotFoundException fnfe) {
			// _log.error(fnfe);
			// } catch (ParserConfigurationException pce) {
			// _log.error(pce);
			// } catch (SAXException saxe) {
			// _log.error(saxe);
			// } catch (IOException ioe) {
			// _log.error(ioe);
			// }
		} catch (Exception e) {
			_log.error(e);
		}
		Preconditions.checkNotNull(session);
		_log.debug("Session read (in: " + TimeUtility.msToHumanReadableDelta(start) + ")");
		return session;
	}
}

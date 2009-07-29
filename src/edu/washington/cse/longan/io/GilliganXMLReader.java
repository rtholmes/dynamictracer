package edu.washington.cse.longan.io;

/**
 * Created on Jun 8, 2006
 * 
 * @author rtholmes
 */

import java.util.List;
import java.util.Vector;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import com.google.common.base.Preconditions;

import ca.lsmr.common.util.TimeUtility;
import edu.washington.cse.longan.model.FieldElement;
import edu.washington.cse.longan.model.ILonganConstants;
import edu.washington.cse.longan.model.MethodElement;
import edu.washington.cse.longan.model.Session;
import edu.washington.cse.longan.trace.AJFieldAgent;

public class GilliganXMLReader implements IGilliganStoreIO {

	private Logger _log = Logger.getLogger(this.getClass());

	private int _nextId = 1;

	@SuppressWarnings("unchecked")
	public Session read(Document doc, String fName) {
		_log.info("Initializing Gilligan Stores");

		// Document doc = parseFile(fName);
		Element root = doc.getRootElement();

		Session session = new Session(fName);

		Element structure = root.getChild(XML_STRUCTURE);

		// read source
		Element source = structure.getChild(XML_SOURCE);
		populateNodes(source.getChild(XML_NODES), session);

		// GilliganStore sourceStore = createStore(source);
		 populateEdges(source.getChild(XML_EDGES), session);

		// Element tasksElement = root.getChild(XML_TASKS);
		// List<Element> tasksE = tasksElement.getChildren();
		// for (Element task : tasksE)
		// tasks.add(createTaskStore(task));

		// set up manager
		// StoreManager.clearInstance();
		// StoreManager newManager = StoreManager.createInstance(sourceStore, targetStore, tasks);

		// parseDecorations(newManager, root.getChild(XML_STATE));

		// updateAlreadyProvided(newManager);

		return session;
	}

	@SuppressWarnings("unchecked")
	private void populateNodes(Element nodesElement, Session session) {
		for (Element packageElement : (List<Element>) nodesElement.getChildren(XML_PACKAGE)) {
			readElement(packageElement, session);
		}

	}

	// public void updateAlreadyProvided(StoreManager sm) {
	// // compute already provided dependencies
	// try {
	//
	// GilliganStore source = sm.getSource();
	// GilliganStore target = sm.getTarget();
	//
	// for (IElement ie : source.getElements().values()) {
	// if (ie instanceof ClassElement) {
	// if (target.getElements().containsKey(ie.getName())) {
	// markAlreadyProvided(ie);
	// for (IElement c : ie.getChildren())
	// markAlreadyProvided(c);
	// }
	// }
	// }
	// } catch (Exception e) {
	// _log.error(e);
	// }
	// }

	// private void markAlreadyProvided(IElement ie) {
	//
	// if (!ie.isDecorated(AlreadyProvidedDependencyAction.ID)) {
	// ie.decorate(new AlreadyProvidedDependencyAction());
	// }
	//
	// }

	@SuppressWarnings("unchecked")
	private void populateEdges(Element edges, Session session) {
		Element calls = edges.getChild(XML_CALLS);
		Element references = edges.getChild(XML_REFERENCES);
		Element inherits = edges.getChild(XML_INHERITS);

		for (Element call : ((List<Element>) calls.getChildren())) {
			MethodElement source = session.getMethodForName(call.getAttributeValue(XML_SOURCE));
			MethodElement target = session.getMethodForName(call.getAttributeValue(XML_TARGET));
			if (source == null || target == null) {
				_log.error("Call dropped: " + call.getAttributeValue(XML_SOURCE) + " -> " + call.getAttributeValue(XML_TARGET));
			} else {
				// XXX: do something with the call
				// session.addCall(source, target);
				target.getCalledBy().add(source.getId());
			}
			// store call
		}

		for (Element call : ((List<Element>) references.getChildren())) {
			MethodElement source = session.getMethodForName(call.getAttributeValue(XML_SOURCE));
			FieldElement field = session.getFieldForName(call.getAttributeValue(XML_TARGET));
			if (source == null || field == null) {
				_log.error("Reference dropped: " + call.getAttributeValue(XML_SOURCE) + " -> " + call.getAttributeValue(XML_TARGET));
			} else {
				// XXX: do soemthing with the reference
				// session.addReference(source, field);
				// NOTE: statically we just consider everyting gets, we don't consider sets
				field.getGetBy().add(source.getId());

			}
			// store reference
		}

		// for (Element inh : ((List<Element>) inherits.getChildren())) {
		// ClassElement child = session.getClassElement(inh.getAttributeValue(XML_CHILD));
		// ClassElement parent = session.getClassElement(inh.getAttributeValue(XML_PARENT));
		// if (parent == null || child == null)
		// _log.debug("Inheritance relationship dropped: " + inh.getAttributeValue(XML_CHILD) + " -> " +
		// inh.getAttributeValue(XML_PARENT));
		// else
		// session.addInherits(child, parent);
		// // store inh
		// }

	}

	// @SuppressWarnings("unchecked")
	// private GilliganStore createTaskStore(Element storeNode) {
	// GilliganStore store = new GilliganStore(storeNode.getAttributeValue(XML_NAME));
	// List<Element> nodes = storeNode.getChild(XML_NODES).getChildren();
	// for (Element elem : nodes) {
	// readElement(elem, store, null);
	// }
	// return store;
	// }
	//
	// @SuppressWarnings("unchecked")
	// private GilliganStore createStore(Element storeNode) {
	// GilliganStore store = new GilliganStore(storeNode.getAttributeValue(XML_NAME));
	// List<Element> taskPackages = storeNode.getChild(XML_NODES).getChildren();
	// for (Element elem : taskPackages) {
	// if (!elem.getName().equals(XML_PACKAGE))
	// _log.error("unknown child of nodes: " + elem.getName());
	// else
	// readElement(elem, store, null);
	// }
	// return store;
	// }

	@SuppressWarnings("unchecked")
	private void readElement(Element elem, Session session) {
		try {
			String elemName = elem.getName();

			boolean exists = session.hasIDForElement(elem.getAttributeValue(XML_NAME));
			// boolean exists = store.getElement(elem.getAttributeValue(XML_NAME)) != null;
			if (!exists) {
				if (elemName.equals(XML_PACKAGE)) {

					// don't care about packages but they contain classes and other packages
					for (Element child : (List<Element>) elem.getChildren()) {
						readElement(child, session);
					}

				} else if (elemName.equals(XML_CLASS)) {

					// don't care about classes but they contain interesting methods and fields
					for (Element child : (List<Element>) elem.getChildren()) {
						readElement(child, session);
					}

				} else if (elemName.equals(XML_METHOD)) {
					String methodName = elem.getAttributeValue(XML_NAME);

					// if (!session.hasIDForElement(methodName)) {
					// edu.washington.cse.longan.model.MethodElement me = new
					// edu.washington.cse.longan.model.MethodElement(_nextId++, methodName,
					// false);
					// session.addMethod(me.getId(), me);
					// } else {
					// Preconditions.checkNotNull(null, ILonganConstants.NOT_POSSIBLE);
					// }

					if (!session.hasIDForElement(methodName)) {
						session.addIDForElement(methodName, _nextId++);
					}

					int id = session.getIdForElement(methodName);
					if (!session.methodExists(id)) {
						session.addMethod(id, new MethodElement(id, methodName, false));
					}

				} else if (elemName.equals(XML_FIELD)) {
					String fieldName = elem.getAttributeValue(XML_NAME);

					if (!session.hasIDForElement(fieldName)) {
						session.addIDForElement(fieldName, _nextId++);
					}

					int id = session.getIdForElement(fieldName);
					if (!session.fieldExists(id)) {
						session.addField(id, new FieldElement(id, fieldName));
					}

					// if (!session.hasIDForElement(fieldName)) {
					// edu.washington.cse.longan.model.FieldElement fe = new
					// edu.washington.cse.longan.model.FieldElement(_nextId++, fieldName);
					// session.addField(fe.getId(), fe);
					// } else {
					// Preconditions.checkNotNull(null, ILonganConstants.NOT_POSSIBLE);
					// }

					// ie = new FieldElement(elem.getAttributeValue(XML_NAME));
					// String type = elem.getAttributeValue(XML_TYPE);
					// ((FieldElement) ie).setType(type);
				} else {
					_log.error("unknown node type: " + elemName);
				}
			} else {
				Preconditions.checkNotNull(null, ILonganConstants.NOT_POSSIBLE);
			}

			// Preconditions.checkNotNull(ie);
			// // Assert.assertNotNull(ie);
			// //
			// // if (elem.getAttribute(XML_VISIBLE) != null)
			// // ie.setVisible(Boolean.parseBoolean(elem.getAttributeValue(XML_VISIBLE)));
			// //
			// // if (elem.getAttribute(XML_EXTERNAL) != null)
			// // ie.setExternal(Boolean.parseBoolean(elem.getAttributeValue(XML_EXTERNAL)));
			//
			// // null parents should only happen for root packages
			// if (parent != null) {
			// ie.addParent(parent);
			// parent.addChild(ie);
			// }
			//
			// store.add(ie);
			//
			// List<Element> children = elem.getChildren();
			// for (Element child : children)
			// if (child.getName().equals(IStoreIO.XML_DECORATORS))
			// readDecorator(child, ie);
			// else if (child.getName().equals(IStoreIO.XML_PARAMS)) {
			// // this is already done above
			// } else
			// readElement(child, store, ie);
		} catch (Exception e) {
			_log.error(e);
		}

	}

	// @SuppressWarnings("unchecked")
	// private void readDecorator(Element child, IElement ie) {
	// for (Element decorator : (List<Element>) child.getChildren()) {
	// // RFE: load decorators
	// String decoratorID = decorator.getAttributeValue(IStoreIO.XML_NAME);
	// String decoratorValue = decorator.getAttributeValue(IStoreIO.XML_VALUE);
	// }
	// }

	private Document parseFile(String fName) {
		SAXBuilder builder = new SAXBuilder(false);
		Document doc = null;
		try {
			long start = System.currentTimeMillis();
			_log.info("Loading saved session: " + fName);
			File f = new File(fName);
			if (f.exists()) {
				doc = builder.build(f);
				_log.info("Session loaded in " + TimeUtility.msToHumanReadableDelta(start));
			} else {
				_log.error("File does not exist: " + fName);
			}
		} catch (JDOMException jdome) {
			_log.error(jdome);
		} catch (IOException ioe) {
			_log.error(ioe);
		}
		return doc;
	}

	// @SuppressWarnings("unchecked")
	// public void parseDecorations(StoreManager sm, Element stateRoot) {
	// // not the smartest spot for this, but time is short
	//
	// // XXX: REFACTOR! This should be in the reader!!!
	//
	// // Document doc = parseFile(fName);
	// // Element root = doc.getRootElement();
	//
	// Element structure = stateRoot;// root.getChild(IStoreIO.XML_STATE);
	// int count = 0;
	// // read source
	// Element decorated = structure.getChild(IStoreIO.XML_DECORATED);
	// List<Element> decoratedChildren = decorated.getChildren();
	// for (Element element : decoratedChildren) {
	// // from ModelUtilities.readLog
	// String eName = element.getAttributeValue(IStoreIO.XML_NAME);
	// count++;
	// IElement ie = sm.getSource().getElement(eName);
	// for (Element decision : (List<Element>)
	// element.getChild(IStoreIO.XML_DECORATORS).getChildren(IStoreIO.XML_DECORATOR)) {
	// String dName = decision.getAttributeValue(IStoreIO.XML_NAME);
	// String dData = decision.getAttributeValue(IStoreIO.XML_VALUE);
	//
	// // just for safety and backwards compat with older data files
	// if (dData == null)
	// dData = "";
	//
	// // if (dName.equals(NodeVisitedAction.ID)) {
	// // ie.decorate(new NodeVisitedAction());
	// // } else
	// if (dName.equals(AcceptDependencyAction.ID)) {
	// ie.decorate(new AcceptDependencyAction());
	// } else if (dName.equals(RejectDependencyAction.ID)) {
	// ie.decorate(new RejectDependencyAction());
	// } else if (dName.equals(RemapDependencyAction.ID)) {
	// RemapDependencyAction rda = new RemapDependencyAction();
	// if (dData.length() < 1)
	// _log.error("Data missing for remap on: " + dName);
	// rda.setTarget(dData);
	// ie.decorate(rda);
	// } else if (dName.equals(AlreadyProvidedDependencyAction.ID)) {
	// ie.decorate(new AlreadyProvidedDependencyAction());
	// } else if (dName.equals(InjectCodeAction.ID)) {
	// InjectCodeAction ica = new InjectCodeAction();
	// ica.setInjection(dData);
	// if (dData.length() < 1)
	// _log.error("Data missing for injection on: " + dName);
	// ie.decorate(ica);
	// } else if (dName.equals(ExtractFieldAction.ID)) {
	// ExtractFieldAction efa = new ExtractFieldAction();
	// efa.setTarget(dData);
	// if (dData.length() < 1)
	// _log.error("Data missing for field extraction on: " + dName);
	// ie.decorate(efa);
	// }
	// }
	// }
	// _log.info(count + " previously decorated elements updated");
	// }
}

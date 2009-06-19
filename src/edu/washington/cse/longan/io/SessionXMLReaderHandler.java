package edu.washington.cse.longan.io;

import java.util.Vector;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.common.base.Preconditions;

import edu.washington.cse.longan.model.MethodElement;
import edu.washington.cse.longan.model.ParamTraitContainer;
import edu.washington.cse.longan.model.ReturnTraitContainer;
import edu.washington.cse.longan.model.Session;
import edu.washington.cse.longan.trait.ITrait;
import edu.washington.cse.longan.trait.TraitFactory;
import edu.washington.cse.longan.trait.ITrait.DATA_KINDS;

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
	boolean calledByElem = false;
	private boolean supplementalDataElem = false;

	private Session _session;

	private TraitFactory _traitFactory;

	private MethodElement _currentStaticMethod;
	private MethodElement _currentDynamicMethod;
	private MethodElement _currentCalledByMethod;
	private ParamTraitContainer _currentDynamicParam;
	private ReturnTraitContainer _currentDynamicReturn;
	private ITrait _currentDynamicTrait;

	private Vector<ITrait> _currentDynamicParamTraits;
	private Vector<ITrait> _currentDynamicReturnTraits;

	public SessionXMLReaderHandler(Session session) {
		_session = session;
		_traitFactory = new TraitFactory();
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();

		_log.debug("startDocument");
	}

	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
		_log.debug("endDocument");
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		_log.trace("startElement: " + qName);

		if (qName.equals(ILonganIO.DATA)) {
			dataElem = true;
			if (dynamicElem && traitElem) {
				parseTraitData(attributes);
			}
		} else if (qName.equals(ILonganIO.SUPPLEMENTAL_DATA)) {
			supplementalDataElem = true;
			if (dynamicElem && traitElem) {
				parseSupplementalTraitData(attributes);
			}
		} else if (qName.equals(ILonganIO.TRAIT)) {
			traitElem = true;
			if (dynamicElem && calledByElem) {
				parseDynamicTrait(attributes);
			}
		} else if (qName.equals(ILonganIO.PARAMETERS)) {
			paramsElem = true;
		} else if (qName.equals(ILonganIO.RETURN)) {
			returnElem = true;
			if (staticElem && methodElem) {
				parseStaticMethodReturn(attributes);
			} else if (dynamicElem && calledByElem) {
				_currentDynamicReturn = _currentDynamicMethod.getReturnTraitContainer();
			}
		} else if (qName.equals(ILonganIO.PARAMETER)) {
			paramElem = true;

			if (staticElem && paramsElem && methodElem) {
				parseStaticMethodParam(attributes);
			} else if (dynamicElem && calledByElem) {
				parseDynamicMethodParam(attributes);
				_currentDynamicParamTraits = new Vector<ITrait>();
			}

		} else if (qName.equals(ILonganIO.METHOD)) {
			methodElem = true;

			if (staticElem) {
				parseStaticMethodAttrs(attributes);
			} else if (dynamicElem && methodsElem) {
				parseDynamicMethodAttrs(attributes);
			}

		} else if (qName.equals(ILonganIO.CALLEDBY)) {
			calledByElem = true;

			if (dynamicElem && methodElem) {
				parseDynamicCalledBy(attributes);
				_currentDynamicReturnTraits = new Vector<ITrait>();
			}
		} else if (qName.equals(ILonganIO.METHODS)) {
			methodsElem = true;
		} else if (qName.equals(ILonganIO.ROOT)) {
			rootElem = true;
			parseRootAttributes(attributes);
		} else if (qName.equals(ILonganIO.DYNAMIC)) {
			dynamicElem = true;
		} else if (qName.equals(ILonganIO.STATIC)) {
			staticElem = true;
		}
	}

	private void parseSupplementalTraitData(Attributes attributes) {
		// <sdata key="org.ulti.dev.dynamic.test.LocalType" value="2"/>

		String dataKey = attributes.getValue(ILonganIO.KEY);
		String dataValueString = attributes.getValue(ILonganIO.VALUE);
		Integer dataValue = Integer.parseInt(dataValueString);

		_currentDynamicTrait.getSupplementalData().setCount(dataKey, dataValue);
	}

	private void parseTraitData(Attributes attributes) {
		// <data key="NOT_NULL" value="2"/>
		String dataKey = attributes.getValue(ILonganIO.KEY);
		String dataValueString = attributes.getValue(ILonganIO.VALUE);
		Integer dataValue = Integer.parseInt(dataValueString);

		_currentDynamicTrait.getData().setCount(DATA_KINDS.valueOf(dataKey), dataValue);
	}

	private void parseDynamicTrait(Attributes attributes) {
		_log.trace("parseDynamicTrait");
		// <trait key="IsNullTrait">
		String traitKey = attributes.getValue(ILonganIO.KEY);

		_currentDynamicTrait = _traitFactory.createTrait(traitKey);

		if (paramElem) {
			_currentDynamicParamTraits.add(_currentDynamicTrait);
		} else if (returnElem) {
			_currentDynamicReturnTraits.add(_currentDynamicTrait);
		} else {
			throw new AssertionError("This shouldn't happen");
		}

	}

	private void parseDynamicMethodParam(Attributes attributes) {
		// <param pos="0">
		String posString = attributes.getValue(ILonganIO.POSITION);
		int pos = Integer.parseInt(posString);

		_currentDynamicParam = _currentDynamicMethod.getParamTraitContainers().get(pos);
	}

	private void parseDynamicCalledBy(Attributes attributes) {
		// <calledBy id="4" count="1">
		String idString = attributes.getValue(ILonganIO.ID);
		String countString = attributes.getValue(ILonganIO.COUNT);
		int id = Integer.parseInt(idString);
		int count = Integer.parseInt(countString);

		MethodElement me = _session.getMethod(id);

		Preconditions.checkNotNull(me, "Could not find a static method for id: " + id);

		_currentDynamicMethod.getCalledBy().setCount(id, count);
		_log.trace("ccbm set to: " + me.getName());
		_currentCalledByMethod = me;
	}

	private void parseDynamicMethodAttrs(Attributes attributes) {
		// <method id="21" time="0" xmlns="">

		String idString = attributes.getValue(ILonganIO.ID);
		String timeString = attributes.getValue(ILonganIO.TIME);
		// this is an unsatisfying hack, hopefully it can go away later
		if (timeString.equals("null"))
			timeString = "0";

		int id = Integer.parseInt(idString);
		Long time = Long.parseLong(timeString);

		MethodElement me = _session.getMethod(id);
		_session.getProfile().put(id, time);
		_currentDynamicMethod = me;
		if (me == null) {
			throw new AssertionError("Couldn't find method for id: " + id);
		}

	}

	private void parseStaticMethodReturn(Attributes attributes) {
		// <return type="void"/>
		String typeName = attributes.getValue(ILonganIO.TYPE);
		ReturnTraitContainer rtc = new ReturnTraitContainer(typeName);
		_currentStaticMethod.setReturnTraitContainer(rtc);
	}

	private void parseStaticMethodParam(Attributes attributes) {

		// <param pos="0" type="java.util.Collection" name="collection"/>
		String posString = attributes.getValue(ILonganIO.POSITION);
		String name = attributes.getValue(ILonganIO.NAME);
		String typeName = attributes.getValue(ILonganIO.TYPE);
		int pos = Integer.parseInt(posString);

		ParamTraitContainer ptc = new ParamTraitContainer(name, typeName, pos);
		_currentStaticMethod.addParamTraitContainer(ptc, pos);
	}

	private void parseStaticMethodAttrs(Attributes attributes) {
		String idString = attributes.getValue(ILonganIO.ID);
		String name = attributes.getValue(ILonganIO.NAME);
		String externalString = attributes.getValue(ILonganIO.EXTERNAL);

		_log.debug("Method parsed: " + idString + " " + name);
		int id = Integer.parseInt(idString);
		boolean external = Boolean.parseBoolean(externalString);

		MethodElement me = new MethodElement(id, name, external);
		_session.addIDForElement(name, id);
		_session.addMethod(id, me);

		_currentStaticMethod = me;
	}

	private void parseRootAttributes(Attributes attributes) {
		// TODO do something with the root attributes for a project

	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		super.endElement(uri, localName, qName);
		_log.trace("endElement: " + qName);

		if (qName.equals(ILonganIO.DATA)) {
			dataElem = false;
		} else if (qName.equals(ILonganIO.SUPPLEMENTAL_DATA)) {
			supplementalDataElem = false;
		} else if (qName.equals(ILonganIO.TRAIT)) {
			traitElem = false;
			if (dynamicElem && calledByElem && paramElem) {
				_currentDynamicTrait = null;
			}
		} else if (qName.equals(ILonganIO.PARAMETERS)) {
			paramsElem = false;
		} else if (qName.equals(ILonganIO.RETURN)) {
			returnElem = false;
		} else if (qName.equals(ILonganIO.PARAMETER)) {
			paramElem = false;
			if (dynamicElem && calledByElem) {
				ITrait[] traits = new ITrait[_currentDynamicParamTraits.size()];
				traits = _currentDynamicParamTraits.toArray(traits);
				_currentDynamicParam.addTraits(_currentCalledByMethod.getId(), traits);
				_currentDynamicParam = null;
			}
		} else if (qName.equals(ILonganIO.METHOD)) {
			methodElem = false;
			if (staticElem)
				_currentStaticMethod = null;
			if (dynamicElem)
				_currentDynamicMethod = null;
		} else if (qName.equals(ILonganIO.CALLEDBY)) {
			calledByElem = false;
			if (dynamicElem) {
				if (_currentDynamicReturnTraits.size() != 0) {
					// e.g., void returns don't have traits and they don't have dynamicReturn objects
					ITrait[] traits = new ITrait[_currentDynamicReturnTraits.size()];
					traits = _currentDynamicReturnTraits.toArray(traits);
					_currentDynamicReturn.addTraits(_currentCalledByMethod.getId(), traits);
				}
				_currentDynamicReturn = null;
				_log.trace("ccbm: " + _currentCalledByMethod.getName() + " nulled");
				_currentCalledByMethod = null;
			}
		} else if (qName.equals(ILonganIO.METHODS)) {
			methodsElem = false;
		} else if (qName.equals(ILonganIO.ROOT)) {
			rootElem = false;
		} else if (qName.equals(ILonganIO.DYNAMIC)) {
			dynamicElem = false;
		} else if (qName.equals(ILonganIO.STATIC)) {
			staticElem = false;
		}
	}
}

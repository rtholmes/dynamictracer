package edu.washington.cse.longan.io;

import java.util.Stack;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.common.base.Preconditions;

import edu.washington.cse.longan.model.FieldElement;
import edu.washington.cse.longan.model.FieldTraitContainer;
import edu.washington.cse.longan.model.ILonganConstants;
import edu.washington.cse.longan.model.MethodElement;
import edu.washington.cse.longan.model.ParamTraitContainer;
import edu.washington.cse.longan.model.ReturnTraitContainer;
import edu.washington.cse.longan.model.Session;
import edu.washington.cse.longan.trait.ExceptionTrait;
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
	boolean supplementalDataElem = false;
	boolean exceptionElem = false;
	boolean exceptionsElem = false;
	boolean fieldsElem = false;
	boolean fieldElem = false;
	boolean getElem = false;
	boolean setElem = false;

	private Session _session;

	private TraitFactory _traitFactory;

	private MethodElement _currentStaticMethod;
	private MethodElement _currentDynamicMethod;
	private MethodElement _currentCalledByMethod;
	private ParamTraitContainer _currentDynamicParam;
	private ReturnTraitContainer _currentDynamicReturn;
	private FieldTraitContainer _currentDynamicField;
	private ITrait _currentDynamicTrait;
	private FieldElement _currentDyamicField;
	private MethodElement _currentSetMethod;
	private MethodElement _currentGetMethod;

	private Vector<ITrait> _currentDynamicParamTraits;
	private Vector<ITrait> _currentDynamicReturnTraits;
	private Vector<ITrait> _currentDynamicGetTraits;
	private Vector<ITrait> _currentDynamicSetTraits;

	public SessionXMLReaderHandler() {
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

			if (dynamicElem && (getElem || setElem)) {
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
		} else if (qName.equals(ILonganIO.EXCEPTION)) {
			exceptionElem = true;
			if (exceptionsElem) {
				parseException(attributes);
			}

		} else if (qName.equals(ILonganIO.GET)) {
			getElem = true;
			parseDynamicFieldGet(attributes);
		} else if (qName.equals(ILonganIO.SET)) {
			setElem = true;
			parseDynamicFieldSet(attributes);
		} else if (qName.equals(ILonganIO.FIELD)) {
			fieldElem = true;

			if (dynamicElem) {
				parseDynamicFieldAttrs(attributes);
			}

			if (staticElem) {
				parseStaticFieldAttrs(attributes);
			}
		} else if (qName.equals(ILonganIO.EXCEPTIONS)) {
			exceptionsElem = true;
		} else if (qName.equals(ILonganIO.METHODS)) {
			methodsElem = true;
		} else if (qName.equals(ILonganIO.FIELDS)) {
			fieldsElem = true;
		} else if (qName.equals(ILonganIO.ROOT)) {
			rootElem = true;
			parseRootAttributes(attributes);
		} else if (qName.equals(ILonganIO.DYNAMIC)) {
			dynamicElem = true;
		} else if (qName.equals(ILonganIO.STATIC)) {
			staticElem = true;
		}
	}

	private void parseDynamicFieldSet(Attributes attributes) {
		String idString = attributes.getValue(ILonganIO.ID);
		String countString = attributes.getValue(ILonganIO.COUNT);

		int id = Integer.parseInt(idString);
		int count = Integer.parseInt(countString);

		_currentDyamicField.getSetBy().setCount(id, count);
		_currentSetMethod = _session.getMethod(id);

		_currentDynamicSetTraits = new Vector<ITrait>();
	}

	private void parseDynamicFieldGet(Attributes attributes) {
		String idString = attributes.getValue(ILonganIO.ID);
		String countString = attributes.getValue(ILonganIO.COUNT);

		int id = Integer.parseInt(idString);
		int count = Integer.parseInt(countString);

		_currentDyamicField.getGetBy().setCount(id, count);
		_currentGetMethod = _session.getMethod(id);

		_currentDynamicGetTraits = new Vector<ITrait>();
	}

	private void parseDynamicFieldAttrs(Attributes attributes) {
		String idString = attributes.getValue(ILonganIO.ID);

		int id = Integer.parseInt(idString);

		_currentDyamicField = _session.getField(id);

		Preconditions.checkNotNull(_currentDyamicField);
	}

	private void parseStaticFieldAttrs(Attributes attributes) {
		String idString = attributes.getValue(ILonganIO.ID);
		String name = attributes.getValue(ILonganIO.NAME);
		String type = attributes.getValue(ILonganIO.TYPE);

		name = massageName(name);
		int id = Integer.parseInt(idString);

		FieldElement fe = new FieldElement(id, name);
		_session.addIDForElement(name, id);
		_session.addField(id, fe);

		FieldTraitContainer ftcg = new FieldTraitContainer(type);
		FieldTraitContainer ftcs = new FieldTraitContainer(type);
		fe.setFieldGetTraitContainer(ftcg);
		fe.setFieldSetTraitContainer(ftcs);
	}

	// removes the type from fields and the return type from methods
	// e.g.: int a.b.FOO -> a.b.FOO
	// e.g.: int a.b.foo() -> a.b.foo()
	private String massageName(String name) {
		if (name.equals(ILonganConstants.UNKNOWN_METHOD_NAME))
			return name;

		// XXX: if the object is a constructor, don't do this, it'll trim to the first parameter this way

		int firstSpace = name.indexOf(' ');
		String newName = "";
		if (firstSpace > 0)
			newName = name.substring(firstSpace + 1);
		_log.trace("dyn name trans: " + name + " -> " + newName);
		return newName;
	}

	private void parseException(Attributes attributes) {
		String serial = attributes.getValue(ILonganIO.SERIAL);

		String[] parts = serial.split(ILonganConstants.SEPARATOR);

		Stack<Integer> exceptionStack = new Stack<Integer>();
		boolean isThrowing = false;
		boolean isReThrowing = false;
		boolean isCatching = false;
		String throwableType = "";
		String throwableMessage = "";

		for (int i = 0; i < parts.length - 5; i++) {
			exceptionStack.push(Integer.parseInt(parts[i]));
		}

		throwableType = parts[parts.length - 5];
		throwableMessage = parts[parts.length - 4];

		isThrowing = Boolean.parseBoolean(parts[parts.length - 3]);
		isReThrowing = Boolean.parseBoolean(parts[parts.length - 2]);
		isCatching = Boolean.parseBoolean(parts[parts.length - 1]);

		ExceptionTrait et = new ExceptionTrait();
		et.init(exceptionStack, throwableType, throwableMessage, isThrowing, isReThrowing, isCatching);

		Preconditions.checkArgument(et.toString().equals(serial));

		if (isThrowing) {
			_currentDynamicMethod.throwException(exceptionStack, throwableType, throwableMessage);
		} else if (isReThrowing) {
			_currentDynamicMethod.reThrowException(exceptionStack, throwableType, throwableMessage);
		} else if (isCatching) {
			_currentDynamicMethod.handleException(exceptionStack, throwableType, throwableMessage);
		} else {
			Preconditions.checkArgument(false, ILonganConstants.NOT_POSSIBLE);
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

		String traitKey = attributes.getValue(ILonganIO.KEY);

		_currentDynamicTrait = _traitFactory.createTrait(traitKey);

		if (paramElem) {
			_currentDynamicParamTraits.add(_currentDynamicTrait);
		} else if (returnElem) {
			_currentDynamicReturnTraits.add(_currentDynamicTrait);
		} else if (setElem) {
			_currentDynamicSetTraits.add(_currentDynamicTrait);
		} else if (getElem) {
			_currentDynamicGetTraits.add(_currentDynamicTrait);
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

		boolean isConstructor = isConstructor(name);

		// we don't want to massage the name if the method is a constructor
		if (!isConstructor)
			name = massageName(name);

		_log.debug("Method parsed: " + idString + " " + name);
		int id = Integer.parseInt(idString);
		boolean external = Boolean.parseBoolean(externalString);

		MethodElement me = new MethodElement(id, name, external);
		_session.addIDForElement(name, id);
		_session.addMethod(id, me);

		_currentStaticMethod = me;
	}

	// This uses a really crappy heuristic
	private boolean isConstructor(String name) {
		if (name.equals(ILonganConstants.UNKNOWN_METHOD_NAME))
			return false;

		int braceIndex = name.indexOf('(');
		int lastDot = name.lastIndexOf('.', braceIndex);

		String segmentName = name.substring(lastDot + 1, braceIndex);
		boolean isUpper = Character.isUpperCase(segmentName.charAt(0));

		_log.trace("isConstructor. ("+isUpper+") seg name: " + segmentName + " from: " + name);
		// if the last segment starts with an upper case letter, then it's a constructor
		// e.g. void org.joda.time.LongAnWriter.testWriteCollection() -> testWriteCollection -> t -> false
		// e.g. void org.joda.time.LongAnWriter() -> LongAnWriter-> L -> true
		return isUpper;
	}

	private void parseRootAttributes(Attributes attributes) {

		String sessionDate = attributes.getValue(ILonganIO.DATE);
		String sessionName = attributes.getValue(ILonganIO.NAME);

		if (sessionDate == null)
			sessionDate = "";
		if (sessionName == null)
			sessionName = "";

		_session = new Session(sessionDate + sessionName);
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

			if (dynamicElem && fieldsElem)
				_currentDynamicTrait = null;

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
		} else if (qName.equals(ILonganIO.GET)) {
			getElem = false;

			if (dynamicElem && fieldElem) {
				if (_currentDynamicGetTraits.size() != 0) {
					ITrait[] traits = new ITrait[_currentDynamicGetTraits.size()];
					traits = _currentDynamicGetTraits.toArray(traits);

					_currentDyamicField.getFieldGetTraitContainer().addTraits(_currentGetMethod.getId(), traits);
				}
				_currentDynamicGetTraits = null;
				_currentGetMethod = null;
			}

		} else if (qName.equals(ILonganIO.SET)) {
			setElem = false;
			if (dynamicElem && fieldElem) {
				if (_currentDynamicSetTraits.size() != 0) {
					ITrait[] traits = new ITrait[_currentDynamicSetTraits.size()];
					traits = _currentDynamicSetTraits.toArray(traits);

					_currentDyamicField.getFieldSetTraitContainer().addTraits(_currentSetMethod.getId(), traits);
				}
				_currentDynamicSetTraits = null;
				_currentSetMethod = null;
			}

		} else if (qName.equals(ILonganIO.FIELD)) {
			fieldElem = false;
			if (dynamicElem) {

				_currentDyamicField = null;
			}
		} else if (qName.equals(ILonganIO.EXCEPTION)) {
			exceptionElem = false;
		} else if (qName.equals(ILonganIO.EXCEPTIONS)) {
			exceptionsElem = false;
		} else if (qName.equals(ILonganIO.METHODS)) {
			methodsElem = false;
		} else if (qName.equals(ILonganIO.FIELDS)) {
			fieldsElem = false;
		} else if (qName.equals(ILonganIO.ROOT)) {
			rootElem = false;
		} else if (qName.equals(ILonganIO.DYNAMIC)) {
			dynamicElem = false;
		} else if (qName.equals(ILonganIO.STATIC)) {
			staticElem = false;
		}
	}

	public Session getSession() {
		Preconditions.checkNotNull(_session);
		return _session;
	}
}

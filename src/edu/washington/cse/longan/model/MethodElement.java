package edu.washington.cse.longan.model;

import java.util.Stack;
import java.util.Vector;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import edu.washington.cse.longan.Logger;
import edu.washington.cse.longan.trait.ExceptionTrait;

public class MethodElement {
	private Logger _log = Logger.getLogger(this.getClass());

	protected Multiset<Integer> _calledBy = HashMultiset.create();

	protected boolean _hasVoidReturn = false;

	protected int _id;

	protected boolean _isConstructor = false;

	protected String _name;

	private ReturnTraitContainer _returnTraits = null;

	private Vector<ParamTraitContainer> _paramTraits = new Vector<ParamTraitContainer>();

	private Multiset<ExceptionTrait> _exceptions = HashMultiset.create();

	private boolean _isExternal;

	public MethodElement(int id, String name, boolean isExternal) {
		_id = id;
		_name = name;
		_isExternal = isExternal;
		if (ILonganConstants.OUTPUT) {
			_log.debug("New MethodElement - " + id + ": " + name + " isExternal: " + _isExternal);
		}
	}

	public void setReturnTraitContainer(ReturnTraitContainer rtc) {
		if (_returnTraits != null) {
			throw new AssertionError("This should only be set once");
		}
		if (ILonganConstants.OUTPUT) {
			_log.debug("ReturnTraitContainer added: " + rtc.getStaticTypeName());
		}
		_returnTraits = rtc;
	}

	protected ParamTraitContainer getParamTraitContainer(int position) {
		if (position < 0 || position >= _paramTraits.size())
			return null;
		else
			return _paramTraits.get(position);
	}

	public void addParamTraitContainer(ParamTraitContainer ptc, int position) {

		Preconditions.checkArgument(position == _paramTraits.size(), "Should probably be updating a ptc, not replacing it... %s != %s", _paramTraits
				.size(), position);

		_paramTraits.add(ptc);

		if (ILonganConstants.OUTPUT) {
			_log.debug("ParamTraitContainer added - " + ptc.getPosition() + ": " + ptc.getName() + " - " + ptc.getStaticTypeName());
		}
	}

	public String getName() {
		return _name;
	}

	public int getId() {
		return _id;
	}

	public boolean isExternal() {
		return _isExternal;
	}

	public boolean hasVoidReturn() {
		return _hasVoidReturn;
	}

	public Multiset<Integer> getCalledBy() {
		return _calledBy;
	}

	public ReturnTraitContainer getReturnTraitContainer() {
		return _returnTraits;
	}

	public Vector<ParamTraitContainer> getParamTraitContainers() {
		return _paramTraits;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MethodElement) {
			MethodElement that = (MethodElement) obj;
			return Objects.equal(getName(), that.getName());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getName());
	}

	@Override
	public String toString() {
		return getName();
	}

	public void handleException(Stack<Integer> exceptionStack, String exceptionType, String exceptionMessage) {
		ExceptionTrait et = new ExceptionTrait();
		et.init(exceptionStack, exceptionType, exceptionMessage, false, false, true);
		_exceptions.add(et);
	}

	public void throwException(Stack<Integer> exceptionStack, String exceptionType, String exceptionMessage) {
		ExceptionTrait et = new ExceptionTrait();
		et.init(exceptionStack, exceptionType, exceptionMessage, true, false, false);
		_exceptions.add(et);
	}

	public void reThrowException(Stack<Integer> exceptionStack, String exceptionType, String exceptionMessage) {
		ExceptionTrait et = new ExceptionTrait();
		et.init(exceptionStack, exceptionType, exceptionMessage, false, true, false);
		_exceptions.add(et);
	}

	public Multiset<ExceptionTrait> getExceptions() {
		return _exceptions;
	}
}

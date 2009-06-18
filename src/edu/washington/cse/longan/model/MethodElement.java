package edu.washington.cse.longan.model;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public class MethodElement {
	private Logger _log = Logger.getLogger(this.getClass());

	protected Multiset<Integer> _calledBy = HashMultiset.create();

	protected boolean _hasVoidReturn = false;

	protected int _id;

	protected boolean _isConstructor = false;

	protected String _name;

	private ReturnTraitContainer _returnTraits = null;

	private Vector<ParamTraitContainer> _paramTraits = new Vector<ParamTraitContainer>();

	private boolean _isExternal;

	public MethodElement(int id, String name, boolean isExternal) {
		_id = id;
		_name = name;
		_isExternal = isExternal;
		_log.debug("New MethodElement - " + id + ": " + name + " isExternal: " + _isExternal);
	}

	public void addReturnTrait(ReturnTraitContainer rtc) {
		if (_returnTraits != null) {
			throw new AssertionError("This should only be set once");
		}
		_log.debug("ReturnTraitContainer added: " + rtc.getStaticTypeName());
		_returnTraits = rtc;
	}

	protected ParamTraitContainer getParamTrait(int position) {
		if (position >= 0 && position < _paramTraits.size())
			return _paramTraits.get(position);

		return null;
	}

	public void addParamTrait(ParamTraitContainer ptc, int position) {
		if (position != _paramTraits.size()) {
			throw new AssertionError("Should probably be updating a ptc, not replacing it...");
		} else {
			_paramTraits.add(ptc);

			_log.debug("ParamTraitContainer added - " + ptc.getPosition() + ": " + ptc.getName() + " - "
					+ ptc.getStaticTypeName());
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

	public ReturnTraitContainer getReturnTraitContainers() {
		return _returnTraits;
	}

	public Vector<ParamTraitContainer> getParamTraitContainers() {
		return _paramTraits;
	}

}

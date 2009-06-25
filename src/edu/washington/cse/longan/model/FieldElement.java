package edu.washington.cse.longan.model;

import org.apache.log4j.Logger;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public class FieldElement {

	private Logger _log = Logger.getLogger(this.getClass());

	private String _name;
	private int _id;

	protected Multiset<Integer> _getBy = HashMultiset.create();
	protected Multiset<Integer> _setBy = HashMultiset.create();

	private FieldTraitContainer _fieldGetTraits = null;
	private FieldTraitContainer _fieldSetTraits = null;

	public FieldElement(int id, String name) {
		_id = id;
		_name = name;
	}

	public String getName() {
		return _name;
	}

	public int getId() {
		return _id;
	}

	public FieldTraitContainer getFieldGetTraitContainer() {
		return _fieldGetTraits;
	}

	public void setFieldGetTraitContainer(FieldTraitContainer ftc) {

		if (_fieldGetTraits != null) {
			throw new AssertionError("This should only be set once");
		}
		_log.debug("FieldGetTraitContainer added to: " + getName() + " (" + ftc.getStaticTypeName() + ")");
		_fieldGetTraits = ftc;
	}

	public FieldTraitContainer getFieldSetTraitContainer() {
		return _fieldSetTraits;
	}

	public void setFieldSetTraitContainer(FieldTraitContainer ftc) {
		if (_fieldSetTraits != null) {
			throw new AssertionError("This should only be set once");
		}
		_log.debug("FieldSetTraitContainer added to: " + getName() + " (" + ftc.getStaticTypeName() + ")");
		_fieldSetTraits = ftc;
	}

	public Multiset<Integer> getGetBy() {
		return _getBy;
	}

	public Multiset<Integer> getSetBy() {
		return _setBy;
	}
}

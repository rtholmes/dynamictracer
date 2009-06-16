package edu.washington.cse.longan.model;

import java.util.Hashtable;

import edu.washington.cse.longan.trait.ITrait;
import edu.washington.cse.longan.trait.ITraitContainer;

public class ParamTraitContainer implements ITraitContainer {

	private int _position;
	private String _name;
	private String _staticType;
	private Hashtable<Integer, ITrait[]> _traits = new Hashtable<Integer, ITrait[]>();

	public ParamTraitContainer(String name, String staticType, int position) {
		_name = name;
		_staticType = staticType;
		_position = position;
	}

	public void addTraits(int caller, ITrait[] traits) {
		_traits.put(caller, traits);
	}

	public String getStaticTypeName() {
		return _staticType;
	}

	public String getName() {
		return _name;
	}

	public int getPosition() {
		return _position;
	}

	public ITrait[] getTraitsForCaller(int caller) {
		return _traits.get(caller);
	}
}

package edu.washington.cse.longan.model;

import java.util.Hashtable;

import edu.washington.cse.longan.trait.ITrait;
import edu.washington.cse.longan.trait.ITraitContainer;

public class ReturnTraitContainer implements ITraitContainer {

	private String _staticType;
	private Hashtable<Integer, ITrait[]> _traits = new Hashtable<Integer, ITrait[]>();

	public ReturnTraitContainer(String staticType) {
		_staticType = staticType;
	}

	public void addTraits(int caller, ITrait[] traits) {
		_traits.put(caller, traits);
	}
}

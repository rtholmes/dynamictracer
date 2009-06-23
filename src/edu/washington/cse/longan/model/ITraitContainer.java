package edu.washington.cse.longan.model;

import edu.washington.cse.longan.trait.ITrait;

public interface ITraitContainer {

	public void addTraits(int caller, ITrait[] traits);
}

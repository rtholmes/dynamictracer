package edu.washington.cse.longan.model;

import java.util.Hashtable;

import edu.washington.cse.longan.trait.ITrait;
import edu.washington.cse.longan.trait.TraitFactory;
import edu.washington.cse.longan.trait.ITrait.DATA_KINDS;

public abstract class AbstractTraitContainer implements ITraitContainer {

	public enum CONTAINER_KIND {
		PARAM, RETURN
	};

	private final CONTAINER_KIND _containerKind;
	private final String _staticType;
	protected final Hashtable<Integer, ITrait[]> _traits = new Hashtable<Integer, ITrait[]>();

	public AbstractTraitContainer(String staticType, CONTAINER_KIND containerKind) {
		_staticType = staticType;
		_containerKind = containerKind;
	}

	/**
	 * 
	 * @param caller
	 * @return traits for the specified caller
	 */
	public ITrait[] getTraitsForCaller(int caller) {
		return _traits.get(caller);
	}

	public void addTraits(int caller, ITrait[] traits) {
		_traits.put(caller, traits);
	}

	public String getStaticTypeName() {
		return _staticType;
	}

	/**
	 * 
	 * @param caller
	 * @return traits for the specified caller
	 */
	public ITrait[] getTraitsCollapsed() {

		TraitFactory factory = new TraitFactory();

		ITrait[] returnTraits;
		// if there aren't any traits, just return an empty array
		if (_traits.size() < 1) {
			return new ITrait[0];
		} else {
			// the length of the returnTrait array should be the same as the
			// length for any single caller
			returnTraits = new ITrait[_traits.values().iterator().next().length];
		}

		// deal with the traits one at a time
		for (int i = 0; i < returnTraits.length; i++) {
			ITrait trait = null;

			// for each caller
			for (ITrait[] traits : _traits.values()) {
				ITrait t = traits[i];

				if (trait == null) {
					// for the first run of each trait generate a suitable container
					// and add it to the return trait list
					trait = factory.createTrait(t.getName());
					returnTraits[i] = trait;
				}

				for (DATA_KINDS kind : t.getData().elementSet()) {
					trait.getData().add(kind, t.getData().count(kind));
				}

				for (String key : t.getSupplementalData().elementSet()) {
					trait.getSupplementalData().add(key, t.getSupplementalData().count(key));
				}

			}
		}

		return returnTraits;
	}

}

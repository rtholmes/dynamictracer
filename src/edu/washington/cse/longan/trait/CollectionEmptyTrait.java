package edu.washington.cse.longan.trait;

import java.util.Collection;

public class CollectionEmptyTrait extends AbstractTrait {

	@Override
	public String getDescription() {
		return getName();
	}

	@Override
	public String getName() {
		return "NumberTrait";
	}

	@Override
	public String toString() {

		int empty = getData().count(DATA_KINDS.EMPTY);
		int notEmpty = getData().count(DATA_KINDS.NOT_EMPTY);
		
		String ret = "CollectionEmpty - Total: " + getData().size() + " Empty" + ": " + empty + " not empty: "+notEmpty+". ";

		return ret;
	}

	@Override
	public void track(Object obj) {
		if (obj != null) {
			int i = ((Collection) obj).size();
			if (i == 0)
				getData().add(DATA_KINDS.EMPTY);
			else
				getData().add(DATA_KINDS.NOT_EMPTY);
		}

	}

}

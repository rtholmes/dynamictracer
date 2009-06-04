package edu.washington.cse.longan.trait;

import java.util.Collection;

public class StringEmptyTrait extends AbstractTrait {

	@Override
	public String getDescription() {
		return getName();
	}

	@Override
	public String getName() {
		return "StringEmptyTrait";
	}

	@Override
	public String toString() {

		int empty = getData().count(DATA_KINDS.EMPTY);
		int notEmpty = getData().count(DATA_KINDS.NOT_EMPTY);

		String ret = "StringEmpty - Total: " + getData().size() + " Empty" + ": " + empty + " not empty: " + notEmpty
				+ ". ";

		return ret;
	}

	@Override
	public void track(Object obj) {
		if (obj != null) {
			int i = ((String) obj).length();
			if (i == 0)
				getData().add(DATA_KINDS.EMPTY);
			else
				getData().add(DATA_KINDS.NOT_EMPTY);
		}

	}

}

package edu.washington.cse.longan.trait;


public class ArrayEmptyTrait extends AbstractTrait {

	@Override
	public String getDescription() {
		return getName();
	}

	@Override
	public String getName() {
		return "ArrayEmptyTrait";
	}

	@Override
	public String toString() {

		int empty = getData().count(DATA_KINDS.EMPTY);
		int notEmpty = getData().count(DATA_KINDS.NOT_EMPTY);
		
		String ret = "ArrayEmpty - Total: " + getData().size() + " Empty" + ": " + empty + " not empty: "+notEmpty+". ";

		return ret;
	}

	@Override
	public void track(Object obj) {
		if (obj != null) {
			int i = ((Object[]) obj).length;
			if (i == 0)
				getData().add(DATA_KINDS.EMPTY);
			else
				getData().add(DATA_KINDS.NOT_EMPTY);
		}

	}

}
package edu.washington.cse.longan.trait;


public class BooleanTrait extends AbstractTrait {

	public String getDescription() {
		return "Is the object true or false?";
	}

	public String getName() {
		return "BooleanTrait";
	}

	public void track(Object obj) {
		if (obj.equals(Boolean.TRUE))
			getData().add(DATA_KINDS.IS_TRUE);
		else
			getData().add(DATA_KINDS.IS_FALSE);
	}

	@Override
	public String toString() {
		int isTrue = getData().count(DATA_KINDS.IS_TRUE);
		int isFalse = getData().count(DATA_KINDS.IS_FALSE);

		String ret = "Boolean - Total: " + getData().size() + " True: " + isTrue + " False: " + isFalse+". ";
		
		return ret;
	}

}

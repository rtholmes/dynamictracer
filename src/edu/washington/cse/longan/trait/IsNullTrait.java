package edu.washington.cse.longan.trait;


public class IsNullTrait extends AbstractTrait {

	public String getDescription() {
		return "Is the object ever null?";
	}

	public String getName() {
		return "IsNull";
	}

	public void track(Object obj) {
		if (obj == null)
			getData().add(DATA_KINDS.IS_NULL);
		else
			getData().add(DATA_KINDS.NOT_NULL);
	}

	@Override
	public String toString() {
		int isNull = getData().count(DATA_KINDS.IS_NULL);
		int notNull = getData().count(DATA_KINDS.NOT_NULL);

		String ret = "IsNull \t\t Total: " + (isNull + notNull) + " IsNull: " + isNull + " NotNull: " + notNull+". ";
		
		return ret;
	}

}

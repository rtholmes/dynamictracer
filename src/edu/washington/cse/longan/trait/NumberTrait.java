package edu.washington.cse.longan.trait;

public class NumberTrait extends AbstractTrait {

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
		int isZero = getData().count(DATA_KINDS.IS_ZERO);
		int isPositive = getData().count(DATA_KINDS.IS_POSITIVE);
		int isNegative = getData().count(DATA_KINDS.IS_NEGATIVE);
		int isNull = getData().count(DATA_KINDS.IS_NULL);
		String ret = "Number - Total: " + getData().size() + " Zero" + ": " + isZero + " Positive: " + isPositive
				+ " Negative: " + isNegative + " Null:"+isNull+". ";

		return ret;
	}

	@Override
	public void track(Object obj) {
		if (obj != null) {
			int i = ((Number) obj).intValue();
			if (i == 0)
				getData().add(DATA_KINDS.IS_ZERO);
			else if (i < 0)
				getData().add(DATA_KINDS.IS_NEGATIVE);
			else
				getData().add(DATA_KINDS.IS_POSITIVE);
		} else{
			getData().add(DATA_KINDS.IS_NULL);
		}
	}

}

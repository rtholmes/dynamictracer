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

		String ret = "Number - Total: " + getData().size() + " Zero" +
				": " + isZero + " Positive: " + isPositive+ " Negative: " + isNegative+". ";
		
		return ret;	
		}

	@Override
	public void track(Object obj) {
		int i = ((Number)obj).intValue();
		if (i == 0)
			getData().add(DATA_KINDS.IS_ZERO);
		else if (i < 0)
			getData().add(DATA_KINDS.IS_NEGATIVE);
		else
			getData().add(DATA_KINDS.IS_POSITIVE);
	}

}

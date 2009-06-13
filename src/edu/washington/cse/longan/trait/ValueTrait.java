package edu.washington.cse.longan.trait;

public class ValueTrait extends AbstractTrait {

	@Override
	public String getDescription() {
		return "What values were this object?";
	}

	@Override
	public String getName() {
		return "ValueTrait";
	}

	@Override
	public void track(Object obj) {
		if (obj != null)
			getSupplementalData().add(obj.toString());
	}

	@Override
	public String toString() {
		String ret = "";

		for (String value : getSupplementalData().elementSet()) {
			int num = getSupplementalData().count(value);
			ret += value + ": " + num + "; ";
		}

		ret = "Value(s): " + getSupplementalData().elementSet().size() + ", total values: " + getSupplementalData().size() + " - " + ret+". ";

		return ret;
	}

}

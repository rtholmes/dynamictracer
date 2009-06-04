package edu.washington.cse.longan.trait;

public class TypeTrait extends AbstractTrait {

	@Override
	public String getDescription() {
		return "What types were this object?";
	}

	@Override
	public String getName() {
		return "TypeTrait";
	}

	@Override
	public void track(Object obj) {
		if (obj != null)
			getSupplementalData().add(obj.getClass().getName());
	}

	@Override
	public String toString() {
		String ret = "";

		
		for (String type : getSupplementalData().elementSet()) {
			int num = getSupplementalData().count(type);
			ret += type + ": " + num + "; ";
		}

		ret = "Type(s): " + getSupplementalData().elementSet().size() + ", total objects: " + getSupplementalData().size() + " - " + ret+". ";

		return ret;
	}

}

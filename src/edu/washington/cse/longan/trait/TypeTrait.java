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

		int count = 0;
		for (String type : getSupplementalData()) {
			int num = getSupplementalData().count(type);
			count += num;
			ret += type + ": " + num + "; ";
		}

		ret = "Unique objects: " + getSupplementalData().size() + ", total objects: " + count + " - " + ret+". ";

		return ret;
	}

}

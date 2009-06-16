package edu.washington.cse.longan.trait;

public class TraitFactory {

	public ITrait createTrait(String traitKey) {
		ITrait trait = null;

		if (traitKey.equals(ArrayEmptyTrait.ID)) {
			trait = new ArrayEmptyTrait();
		} else if (traitKey.equals(BooleanTrait.ID)) {
			trait = new BooleanTrait();
		} else if (traitKey.equals(CollectionEmptyTrait.ID)) {
			trait = new CollectionEmptyTrait();
		} else if (traitKey.equals(IsNullTrait.ID)) {
			trait = new IsNullTrait();
		} else if (traitKey.equals(LogPrinterTrait.ID)) {
			trait = new LogPrinterTrait();
		} else if (traitKey.equals(NumberTrait.ID)) {
			trait = new NumberTrait();
		} else if (traitKey.equals(StringEmptyTrait.ID)) {
			trait = new StringEmptyTrait();
		} else if (traitKey.equals(TypeTrait.ID)) {
			trait = new TypeTrait();
		} else if (traitKey.equals(ValueTrait.ID)) {
			trait = new ValueTrait();
		} else {
			throw new AssertionError("Unknown trait kind: " + traitKey);
		}

		return trait;
	}
}

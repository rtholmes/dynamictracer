package edu.washington.cse.longan;

public class ComparatorResult {

	private ExecutionDelta _v1s;
	private ExecutionDelta _v2s;
	private ExecutionDelta _v1d;
	private ExecutionDelta _v2d;

	private ExecutionDelta _r1;
	private ExecutionDelta _r2;
	private ExecutionDelta _r3;
	private ExecutionDelta _r4;
	private ExecutionDelta _r5;
	private ExecutionDelta _r6;
	private ExecutionDelta _r7;
	private ExecutionDelta _r8;
	private ExecutionDelta _r9;
	private ExecutionDelta _r10;
	private ExecutionDelta _r11;
	private ExecutionDelta _r12;
	private ExecutionDelta _r13;
	private ExecutionDelta _r14;
	private ExecutionDelta _r15;

	private ExecutionDelta _v1sPrime;
	private ExecutionDelta _v2sPrime;
	private ExecutionDelta _v1dPrime;
	private ExecutionDelta _v2dPrime;

	private ExecutionDelta _staticOnly2less1;
	private ExecutionDelta _staticOnly1less2;
	private ExecutionDelta _dynamicOnly2less1;
	private ExecutionDelta _dynamicOnly1less2;

	private ComparatorResult(Builder builder) {
		_v1s = builder.v1s;
		_v2s = builder.v2s;
		_v1d = builder.v1d;
		_v2d = builder.v2d;

		_r1 = builder.r1;
		_r2 = builder.r2;
		_r3 = builder.r3;
		_r4 = builder.r4;
		_r5 = builder.r5;
		_r6 = builder.r6;
		_r7 = builder.r7;
		_r8 = builder.r8;
		_r9 = builder.r9;
		_r10 = builder.r10;
		_r11 = builder.r11;
		_r12 = builder.r12;
		_r13 = builder.r13;
		_r14 = builder.r14;
		_r15 = builder.r15;

		// handy aliases
		_v1sPrime = builder.r1;
		_v2sPrime = builder.r2;
		_v1dPrime = builder.r8;
		_v2dPrime = builder.r4;

		_staticOnly2less1 = builder.staticOnly2less1;
		_staticOnly1less2 = builder.staticOnly1less2;
		_dynamicOnly2less1 = builder.dynamicOnly2less1;
		_dynamicOnly1less2 = builder.dynamicOnly1less2;

	}

	private String SEP = ",";

	public String generateCSVNodeRow() {
		String ret = "";

		ret += _v1s.getElements().size() + SEP;
		ret += _v2s.getElements().size() + SEP;
		ret += _v1d.getElements().size() + SEP;
		ret += _v2d.getElements().size() + SEP;

		// ret += SEP;

		ret += getValue(_staticOnly1less2.getElements().size()) + SEP;
		ret += getValue(_staticOnly2less1.getElements().size()) + SEP;
		ret += getValue(_dynamicOnly1less2.getElements().size()) + SEP;
		ret += getValue(_dynamicOnly2less1.getElements().size()) + SEP;

		// ret += SEP;

		ret += getValue(_v1sPrime.getElements().size()) + SEP;
		ret += getValue(_v2sPrime.getElements().size()) + SEP;
		ret += getValue(_v1dPrime.getElements().size()) + SEP;
		ret += getValue(_v2dPrime.getElements().size()) + SEP;

		// ret += SEP;

		// ret += _r1.getElements().size() + SEP;
		// ret += _r2.getElements().size() + SEP;
		ret += getValue(_r3.getElements().size()) + SEP;
		// ret += _r4.getElements().size() + SEP;
		ret += getValue(_r5.getElements().size()) + SEP;
		ret += getValue(_r6.getElements().size()) + SEP;
		ret += getValue(_r7.getElements().size()) + SEP;
		// ret += _r8.getElements().size() + SEP;
		ret += getValue(_r9.getElements().size()) + SEP;
		ret += getValue(_r10.getElements().size()) + SEP;
		ret += getValue(_r11.getElements().size()) + SEP;
		ret += getValue(_r12.getElements().size()) + SEP;
		ret += getValue(_r13.getElements().size()) + SEP;
		ret += getValue(_r14.getElements().size()) + SEP;
		ret += getValue(_r15.getElements().size()) + SEP;

		return ret;
	}

	public String generateCSVPathRow() {
		String ret = "";

		ret += _v1s.getPaths().size() + SEP;
		ret += _v2s.getPaths().size() + SEP;
		ret += _v1d.getPaths().size() + SEP;
		ret += _v2d.getPaths().size() + SEP;

		// ret += SEP;

		ret += getValue(_staticOnly1less2.getPaths().size()) + SEP;
		ret += getValue(_staticOnly2less1.getPaths().size()) + SEP;
		ret += getValue(_dynamicOnly1less2.getPaths().size()) + SEP;
		ret += getValue(_dynamicOnly2less1.getPaths().size()) + SEP;

		// ret += SEP;

		ret += getValue(_v1sPrime.getPaths().size()) + SEP;
		ret += getValue(_v2sPrime.getPaths().size()) + SEP;
		ret += getValue(_v1dPrime.getPaths().size()) + SEP;
		ret += getValue(_v2dPrime.getPaths().size()) + SEP;

		// ret += SEP;

		// ret += _r1.getPaths().size()+SEP;
		// ret += _r2.getPaths().size()+SEP;
		ret += getValue(_r3.getPaths().size()) + SEP;
		// ret += _r4.getPaths().size()+SEP;
		ret += getValue(_r5.getPaths().size()) + SEP;
		ret += getValue(_r6.getPaths().size()) + SEP;
		ret += getValue(_r7.getPaths().size()) + SEP;
		// ret += _r8.getPaths().size()+SEP;
		ret += getValue(_r9.getPaths().size()) + SEP;
		ret += getValue(_r10.getPaths().size()) + SEP;
		ret += getValue(_r11.getPaths().size()) + SEP;
		ret += getValue(_r12.getPaths().size()) + SEP;
		ret += getValue(_r13.getPaths().size()) + SEP;
		ret += getValue(_r14.getPaths().size()) + SEP;
		ret += getValue(_r15.getPaths().size()) + SEP;

		return ret;
	}

	public String generateCSVPathRow3() {
		String ret = "";

		ret += _v1s.getPaths().size() + SEP;
		ret += _v2s.getPaths().size() + SEP;
		ret += _v1d.getPaths().size() + SEP;
		ret += _v2d.getPaths().size() + SEP;

		ret += getValue(_staticOnly1less2.getPaths().size()) + SEP;
		ret += getValue(_r1.getPSize()) + SEP;
		ret += getValue(_r9.getPSize()) + SEP;

		ret += getValue(_staticOnly2less1.getPaths().size()) + SEP;
		ret += getValue(_r2.getPSize()) + SEP;
		ret += getValue(_r6.getPSize()) + SEP;

		ret += getValue(_dynamicOnly1less2.getPaths().size()) + SEP;
		ret += getValue(_r8.getPSize()) + SEP;
		ret += getValue(_r9.getPSize()) + SEP;

		ret += getValue(_dynamicOnly2less1.getPaths().size()) + SEP;
		ret += getValue(_r4.getPSize()) + SEP;
		ret += getValue(_r6.getPSize()) + SEP;
		ret += getValue(_r7.getPSize()) + SEP;

		// uninteresting nodes
		ret += getValue(_r3.getPSize()) + SEP;
		ret += getValue(_r12.getPSize()) + SEP;
		ret += getValue(_r15.getPSize()) + SEP;

		return ret;
	}

	public String generateCSVPathRow4() {
		String ret = "";

		ret += _v1s.getPaths().size() + SEP;
		ret += _v2s.getPaths().size() + SEP;
		ret += _v1d.getPaths().size() + SEP;
		ret += _v2d.getPaths().size() + SEP;

		ret += getValue(_r1.getPSize()) + SEP;
		ret += getValue(_r2.getPSize()) + SEP;
		ret += getValue(_r3.getPSize()) + SEP;
		ret += getValue(_r4.getPSize()) + SEP;
		ret += getValue(_r5.getPSize()) + SEP;
		ret += getValue(_r6.getPSize()) + SEP;
		ret += getValue(_r7.getPSize()) + SEP;
		ret += getValue(_r8.getPSize()) + SEP;
		ret += getValue(_r9.getPSize()) + SEP;
		ret += getValue(_r10.getPSize()) + SEP;
		ret += getValue(_r11.getPSize()) + SEP;
		ret += getValue(_r12.getPSize()) + SEP;
		ret += getValue(_r13.getPSize()) + SEP;
		ret += getValue(_r14.getPSize()) + SEP;
		ret += getValue(_r15.getPSize()) + SEP;

		return ret;
	}

	public String generateCSVPathRow2() {
		String ret = "";

		ret += _v1s.getPaths().size() + SEP;
		ret += _v2s.getPaths().size() + SEP;
		ret += _v1d.getPaths().size() + SEP;
		ret += _v2d.getPaths().size() + SEP;

		ret += getValue(_v1sPrime.getPaths().size()) + SEP;
		ret += getValuePlus(_r9.getPSize() + _r5.getPSize() + _r13.getPSize()) + SEP;

		ret += getValue(_v2sPrime.getPaths().size()) + SEP;
		ret += getValuePlus(_r6.getPSize() + _r10.getPSize() + _r14.getPSize()) + SEP;

		ret += getValue(_v1dPrime.getPaths().size()) + SEP;
		ret += getValuePlus(_r9.getPSize() + _r10.getPSize() + _r11.getPSize()) + SEP;

		ret += getValue(_v2dPrime.getPaths().size()) + SEP;
		ret += getValuePlus(_r6.getPSize() + _r7.getPSize() + _r5.getPSize()) + SEP;

		// ret += getValue(_staticOnly1less2.getPaths().size()) + SEP;
		// ret += getValue(_staticOnly2less1.getPaths().size()) + SEP;
		// ret += getValue(_dynamicOnly1less2.getPaths().size()) + SEP;
		// ret += getValue(_dynamicOnly2less1.getPaths().size()) + SEP;

		// ret += SEP;

		// ret += _r1.getPaths().size()+SEP;
		// ret += _r2.getPaths().size()+SEP;
		ret += getValue(_r3.getPaths().size()) + SEP;
		// ret += _r4.getPaths().size()+SEP;
		// ret += getValue(_r5.getPaths().size()) + SEP;
		ret += getValue(_r6.getPaths().size()) + SEP;
		ret += getValue(_r7.getPaths().size()) + SEP;
		// ret += _r8.getPaths().size()+SEP;
		ret += getValue(_r9.getPaths().size()) + SEP;
		// ret += getValue(_r10.getPaths().size()) + SEP;
		// ret += getValue(_r11.getPaths().size()) + SEP;
		ret += getValue(_r12.getPaths().size()) + SEP;
		// ret += getValue(_r13.getPaths().size()) + SEP;
		// ret += getValue(_r14.getPaths().size()) + SEP;
		ret += getValue(_r15.getPaths().size()) + SEP;

		return ret;
	}

	private String getValue(int size) {
		if (size > 0)
			return size + "";
		return "";
	}

	private String getValuePlus(int size) {
		if (size > 0)
			return "+" + size;
		return "";
	}

	public static class Builder {

		private ExecutionDelta v1s;
		private ExecutionDelta v2s;
		private ExecutionDelta v1d;
		private ExecutionDelta v2d;

		private ExecutionDelta r1;
		private ExecutionDelta r2;
		private ExecutionDelta r3;
		private ExecutionDelta r4;
		private ExecutionDelta r5;
		private ExecutionDelta r6;
		private ExecutionDelta r7;
		private ExecutionDelta r8;
		private ExecutionDelta r9;
		private ExecutionDelta r10;
		private ExecutionDelta r11;
		private ExecutionDelta r12;
		private ExecutionDelta r13;
		private ExecutionDelta r14;
		private ExecutionDelta r15;

		private ExecutionDelta v1sPrime;
		private ExecutionDelta v2sPrime;
		private ExecutionDelta v1dPrime;
		private ExecutionDelta v2dPrime;

		private ExecutionDelta staticOnly2less1;
		private ExecutionDelta staticOnly1less2;
		private ExecutionDelta dynamicOnly2less1;
		private ExecutionDelta dynamicOnly1less2;

		public Builder v1s(ExecutionDelta ed) {
			v1s = ed;
			return this;
		}

		public Builder v2s(ExecutionDelta ed) {
			v2s = ed;
			return this;
		}

		public Builder v1d(ExecutionDelta ed) {
			v1d = ed;
			return this;
		}

		public Builder v2d(ExecutionDelta ed) {
			v2d = ed;
			return this;
		}

		public Builder r1(ExecutionDelta ed) {
			r1 = ed;
			return this;
		}

		public Builder r2(ExecutionDelta ed) {
			r2 = ed;
			return this;
		}

		public Builder r3(ExecutionDelta ed) {
			r3 = ed;
			return this;
		}

		public Builder r4(ExecutionDelta ed) {
			r4 = ed;
			return this;
		}

		public Builder r5(ExecutionDelta ed) {
			r5 = ed;
			return this;
		}

		public Builder r6(ExecutionDelta ed) {
			r6 = ed;
			return this;
		}

		public Builder r7(ExecutionDelta ed) {
			r7 = ed;
			return this;
		}

		public Builder r8(ExecutionDelta ed) {
			r8 = ed;
			return this;
		}

		public Builder r9(ExecutionDelta ed) {
			r9 = ed;
			return this;
		}

		public Builder r10(ExecutionDelta ed) {
			r10 = ed;
			return this;
		}

		public Builder r11(ExecutionDelta ed) {
			r11 = ed;
			return this;
		}

		public Builder r12(ExecutionDelta ed) {
			r12 = ed;
			return this;
		}

		public Builder r13(ExecutionDelta ed) {
			r13 = ed;
			return this;
		}

		public Builder r14(ExecutionDelta ed) {
			r14 = ed;
			return this;
		}

		public Builder r15(ExecutionDelta ed) {
			r15 = ed;
			return this;
		}

		public Builder static1less2(ExecutionDelta ed) {
			staticOnly1less2 = ed;
			return this;
		}

		public Builder static2less1(ExecutionDelta ed) {
			staticOnly2less1 = ed;
			return this;
		}

		public Builder dynamic1less2(ExecutionDelta ed) {
			dynamicOnly1less2 = ed;
			return this;
		}

		public Builder dynamic2less1(ExecutionDelta ed) {
			dynamicOnly2less1 = ed;
			return this;
		}

		public ComparatorResult build() {
			return new ComparatorResult(this);
		}
	}
}

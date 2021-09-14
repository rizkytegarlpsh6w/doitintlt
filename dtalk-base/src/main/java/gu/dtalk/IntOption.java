package gu.dtalk;

public class IntOption extends BaseNumOption<Integer> {

	public IntOption() {
		super(0);
	}

	@Override
	public final OptionType getType() {
		return OptionType.INTEGER;
	}
	
}

package gu.dtalk;

public class StringOption extends BaseOption<String> {

	public StringOption() {
		super(String.class);
	}

	@Override
	public OptionType getType() {
		return OptionType.STRING;
	}

}

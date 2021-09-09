package gu.dtalk;

import static com.google.common.base.Preconditions.*;

public class StringOption extends BaseOption<String> {
	
	private OptionType type;
	public StringOption() {
		super(String.class);
	}

	@Override
	public OptionType getType() {
		return type;
	}

	StringOption setType(OptionType type) {
		this.type = checkNotNull(type,"type is null");
		return this;
	}
	
}

package gu.dtalk;

import static com.google.common.base.Preconditions.*;

/**
 * 通用字符串类型选项
 * @author guyadong
 *
 */
public class StringOption extends BaseOption<String> {
	
	private OptionType optionType;
	public StringOption() {
		super(String.class);
	}

	@Override
	public OptionType getType() {
		return null == optionType ? OptionType.STRING : optionType;
	}

	@Override
	BaseOption<String> setType(OptionType optionType) {
		this.optionType = checkNotNull(optionType,"optionType is null");
		return this;
	}
	
}

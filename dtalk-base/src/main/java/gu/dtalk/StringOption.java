package gu.dtalk;

import com.alibaba.fastjson.TypeReference;

public class StringOption extends BaseOption<String> {

	public StringOption() {
		super(new TypeReference<String>() {}.getType());
	}

	@Override
	public OptionType getType() {
		return OptionType.STRING;
	}

}

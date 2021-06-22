package gu.dtalk;

import com.alibaba.fastjson.TypeReference;

public class Base64Option extends BaseOption<byte[]> {

	public Base64Option() {
		super(new TypeReference<byte[]>() {}.getType());
	}

	@Override
	public OptionType getType() {
		return OptionType.BASE64;
	}

}

package gu.dtalk;

import java.net.URL;

import com.alibaba.fastjson.TypeReference;

/**
 * URL类型选项
 * @author guyadong
 *
 */
public class UrlOption extends BaseOption<URL> {
	
	public UrlOption() {
		super(new TypeReference<URL>() {}.getType());
	}

	@Override
	public OptionType getType() {
		return OptionType.URL;
	}

}

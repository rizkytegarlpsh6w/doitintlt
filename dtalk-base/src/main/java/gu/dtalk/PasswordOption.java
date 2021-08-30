package gu.dtalk;

import java.util.Arrays;

import com.google.common.base.Strings;

/**
 * 密码选项
 * @author guyadong
 *
 */
public class PasswordOption extends StringOption {

	@Override
	public String contentOfValue() {
		if(Strings.isNullOrEmpty(getValue())){
			return "empty password";
		}
		char[] pwd = new char[getValue().length()];
		Arrays.fill(pwd, '*');
		return new String(pwd);
	}

	@Override
	public final OptionType getType() {
		return OptionType.PASSWORD;
	}

}

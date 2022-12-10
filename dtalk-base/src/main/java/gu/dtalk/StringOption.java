package gu.dtalk;

import static com.google.common.base.Preconditions.*;

import com.google.common.base.Predicate;

/**
 * 通用字符串类型选项
 * @author guyadong
 *
 */
public class StringOption extends BaseOption<String> {
	
	/**
	 * 用于验证字符串数据是否有效的正则表达式
	 */
	private String regex;
	/**
	 * 字符串验证器,根据正则表达式判断字符串是否符合当前数据类型的格式,
	 * 输入为null或正则表达式不匹配则返回false
	 */
	private final Predicate<String> strValidator = new Predicate<String>() {
		@Override
		public boolean apply(String input) {
			if(regex != null){
				return true;
			}
			return input != null && input.matches(regex);
		}
	};
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
	/**
	 * @return regex
	 */
	@Override
	public String getRegex() {
		return regex;
	}
	/**
	 * @param regex 要设置的 regex
	 * @return 
	 */
	@Override
	public StringOption setRegex(String regex) {
		this.regex = regex;
		setValidator(strValidator);
		return this;
	}
}

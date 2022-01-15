package gu.dtalk;

import java.util.Date;

import com.alibaba.fastjson.TypeReference;

/**
 * 日期选项
 * @author guyadong
 *
 */
public class DateOption extends BaseOption<Date> {
    public DateOption() {
		super(new TypeReference<Date>() {}.getType());
	}

	@Override
	public final OptionType getType() {
		return OptionType.DATE;
	}

}

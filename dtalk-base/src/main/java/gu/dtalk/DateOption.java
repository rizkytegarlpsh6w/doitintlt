package gu.dtalk;

import java.util.Date;

import com.alibaba.fastjson.TypeReference;

public class DateOption extends BaseOption<Date> {
    public DateOption() {
		super(new TypeReference<Date>() {}.getType());
	}

	public static final String DATE_FORMATTER_STR = "yyyy-MM-dd HH:mm:ss";

	@Override
	public final OptionType getType() {
		return OptionType.DATE;
	}

}

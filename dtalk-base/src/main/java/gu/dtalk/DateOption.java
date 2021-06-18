package gu.dtalk;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateOption extends BaseOption<Date> {
    public static final String DATE_FORMATTER_STR = "yyyy-MM-dd HH:mm:ss";

	@Override
	public final OptionType getType() {
		return OptionType.DATE;
	}

	@Override
	public String toString(Date input) throws TransformException {
		if(null == input){
			throw new TransformException("NULL POINTER");
		}
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMATTER_STR);

		return formatter.format(input);
	}

	@Override
	public Date fromString(String input) throws TransformException {
		if(null == input){
			throw new TransformException("NULL POINTER");
		}
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMATTER_STR);
		try {
			return formatter.parse(input);
		} catch (ParseException e) {
			throw new TransformException(e);
		}
	}

}

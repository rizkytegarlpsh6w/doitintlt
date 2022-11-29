package gu.dtalk;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.common.base.Strings;

import static com.google.common.base.Preconditions.*;
import static gu.dtalk.CommonConstant.DATE_FORMATTER_STR;
import static gu.dtalk.CommonConstant.ISO8601_FORMATTER_STR;
import static gu.dtalk.CommonConstant.TIMESTAMP_FORMATTER_STR;
import static gu.dtalk.CommonConstant.TIME_FORMATTER_STR;

public class ISO8601Date extends Date {
	private static final long serialVersionUID = 451493751006876687L;
	public ISO8601Date() {
		super();
	}
	public ISO8601Date(Date date) {
		super(checkNotNull(date,"date is null").getTime());
	}
	public ISO8601Date(long date) {
		super(date);
	}
	public ISO8601Date(String date) throws ParseException {
		this(new SimpleDateFormat(ISO8601_FORMATTER_STR).parse(checkNotNull(Strings.emptyToNull(date),"date is null or empty")));
	}
	@Override
	public String toString() {
		return new SimpleDateFormat(ISO8601_FORMATTER_STR).format(this);
	}
	public static ISO8601Date of(Date date){
		return null == date ? null : new ISO8601Date(date);
	}
	public static ISO8601Date of(long date){
		return new ISO8601Date(date);
	}
	/**
	 * 字符串转为日期对象
	 * @param input
	 * @return
	 * @throws ParseException
	 */
	public static ISO8601Date of(String input) throws ParseException{
		ISO8601Date date = null;
        if(null != input){
        	try {
        		date = new ISO8601Date(input);
			} catch (ParseException e) {
	        	try {
	        		date = ISO8601Date.of(new SimpleDateFormat(TIMESTAMP_FORMATTER_STR).parse(input));
	        	} catch (ParseException e1) {
	        		try {
	        			date = ISO8601Date.of(new SimpleDateFormat(DATE_FORMATTER_STR).parse(input));
	        		} catch (ParseException e2) {
	        			try {
	        				date = ISO8601Date.of(new SimpleDateFormat(TIME_FORMATTER_STR).parse(input));
	        			} catch (ParseException e3) {
	        				throw new IllegalArgumentException(String.format("INVALID FORMAT '%s' FOR DATE", input));
	        			}
	        		}
	        	}
			}
        }
		return date;
	}
}

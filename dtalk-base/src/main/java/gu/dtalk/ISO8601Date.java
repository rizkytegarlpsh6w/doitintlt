package gu.dtalk;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ISO8601Date extends Date {
	private static final long serialVersionUID = 451493751006876687L;
	/** ISO8601时间格式 */
    private static final String ISO8601_FORMATTER_STR = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
	public ISO8601Date() {
	}

	public ISO8601Date(long date) {
		super(date);
	}
	@Override
	public String toString() {
		return new SimpleDateFormat(ISO8601_FORMATTER_STR).format(this);}
}

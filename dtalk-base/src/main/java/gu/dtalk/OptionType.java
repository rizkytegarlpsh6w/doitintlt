package gu.dtalk;

public enum OptionType {
	/** 字符串  */STRING,
	/** 整数     */INTEGER,
	/** 浮点数 */FLOAT,
	/** 布尔型 true/false 0/1 yes/no,on/off */BOOL,
	/** 日期  yyyy-MM-dd HH:mm:ss  */DATE,
	/** 16进制数据 */ HEX,
	/** base64 格式二进制数据 */BASE64,
	/** base64 格式JPEG/BMP/PNG格式图像 */IMAGE,
	/** 多选项(n>1) */OPTION,
	/** 单选开关(n>2) */SWITCH;
}

package gu.dtalk;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;

public enum OptionType {
	AUTO,
	/** 字符串  */STRING,
	/** 整数     */INTEGER,
	/** 浮点数 */FLOAT,
	/** 布尔型 true/false 0/1 yes/no,on/off */BOOL,
	/** 日期  yyyy-MM-dd HH:mm:ss  */DATE,
	/** url字符串 */URL,
	/** base64 格式二进制数据 */BASE64,
	/** base64 格式JPEG/BMP/PNG格式图像 */IMAGE,
	/** 多选项(n>1) */MULTICHECK,
	/** 单选开关(n>2) */SWITCH;
	public static BaseOption<?> parseOption(JSONObject t){
		OptionType optionType = OptionType.valueOf(t.getString("type"));
		switch(optionType){
		case STRING:
			return TypeUtils.castToJavaBean(t, StringOption.class);
		case INTEGER:
			return TypeUtils.castToJavaBean(t, IntOption.class);
		case FLOAT:
			return TypeUtils.castToJavaBean(t, FloatOption.class);
		case BOOL:
			return TypeUtils.castToJavaBean(t, BoolOption.class);
		case DATE:
			return TypeUtils.castToJavaBean(t, DateOption.class);
		case URL:
			return TypeUtils.castToJavaBean(t, UrlOption.class);
		case BASE64:
			return TypeUtils.castToJavaBean(t, Base64Option.class);
		case IMAGE:
			return TypeUtils.castToJavaBean(t, ImageOption.class);
		case MULTICHECK:
			return TypeUtils.castToJavaBean(t, CheckOption.class);
		case SWITCH:
			return TypeUtils.castToJavaBean(t, SwitchOption.class);
		default :
			throw new IllegalArgumentException("INVALID OptionType");
		}		
	}
}

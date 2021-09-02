package gu.dtalk;

import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.util.TypeUtils;
import com.google.common.collect.Sets;

import net.gdface.utils.FaceUtilits;

import static gu.dtalk.CommonConstant.*;
import static com.google.common.base.Preconditions.*;

public enum OptionType {
	AUTO,
	/** 字符串  */STRING,
	/** 整数     */INTEGER,
	/** 浮点数 */FLOAT,
	/** 布尔型 true/false 0/1 yes/no,on/off */BOOL,
	/** 日期  yyyy-MM-dd HH:mm:ss  */DATE,
	/** url字符串 */URL,
	/** 密码字符串 */PASSWORD,
	/** base64 格式二进制数据 */BASE64,
	/** MAC地址二进制数据 */MAC,
	/** IP地址二进制数据 */IP,
	/** base64 格式JPEG/BMP/PNG格式图像 */IMAGE,
	/** 多选项(n>1) */MULTICHECK,
	/** 单选开关(n>2) */SWITCH;
	public static BaseOption<?> parseOption(Map<String,Object> json){
		OptionType optionType = OptionType.valueOf((String) json.get(OPTION_FIELD_TYPE));
		switch(optionType){
		case STRING:
			return TypeUtils.castToJavaBean(json, StringOption.class);
		case INTEGER:
			return TypeUtils.castToJavaBean(json, IntOption.class);
		case FLOAT:
			return TypeUtils.castToJavaBean(json, FloatOption.class);
		case BOOL:
			return TypeUtils.castToJavaBean(json, BoolOption.class);
		case DATE:
			return TypeUtils.castToJavaBean(json, DateOption.class);
		case URL:
			return TypeUtils.castToJavaBean(json, UrlOption.class);
		case PASSWORD:
			return TypeUtils.castToJavaBean(json, PasswordOption.class);
		case BASE64:
			return TypeUtils.castToJavaBean(json, Base64Option.class);
		case MAC:{
			refreshValue4MAC(json);
			return TypeUtils.castToJavaBean(json, MACOption.class);
		}
		case IP:{
			refreshValue4Ipv4(json);
			return TypeUtils.castToJavaBean(json, IPv4Option.class);
		}
		case IMAGE:
			return TypeUtils.castToJavaBean(json, ImageOption.class);
		case MULTICHECK:{
			refreshValue4IntSet(json);
			return TypeUtils.castToJavaBean(json, CheckOption.class);
		}
		case SWITCH:{
			refreshValue4IntSet(json);
			return TypeUtils.castToJavaBean(json, SwitchOption.class);
		}
		default :
			throw new IllegalArgumentException("INVALID OptionType");
		}
	}
	
	/**
	 * 重新解析value字段为Set<Integer>类型
	 * @param json
	 */
	private static void refreshValue4IntSet(Map<String,Object> json){
		String value = json.get(OPTION_FIELD_VALUE).toString();
		try{
			Set<Integer> parsed = JSON.parseObject(value,
					new TypeReference<Set<Integer>>(){}.getType());
			json.put(OPTION_FIELD_VALUE, parsed);
			return ;
		}catch (JSONException e) {
			try {
				json.put(OPTION_FIELD_VALUE, Sets.newHashSet(JSON.parseObject(value,Integer.class)));	
			} catch (JSONException e2) {
				String[] list = value.split("[,;\\s]+");
				try{
					Set<Integer> parsed = JSON.parseObject(JSON.toJSONString(list),
							new TypeReference<Set<Integer>>(){}.getType());
					json.put(OPTION_FIELD_VALUE, parsed);
				}catch (JSONException e3) {
					throw e;
				}
			}
		}
	}

	/**
	 * 重新解析value字段为ipv4类型,允许输入格式为127.0.0.1格式的ip地址
	 * @param json
	 */
	private static void refreshValue4Ipv4(Map<String,Object> json){		
		
		Object obj = json.get(OPTION_FIELD_VALUE);
		try{
			if(IPv4Option.STR_VALIDATOR.apply(obj.toString())){
				String ip = "[" + obj.toString().replace(".", ",") + "]";
				// [192,168,1,1]这样的数组大于127的值直接解析为byte会溢出，所以要先解析为int[]再转为byte[]
				int[] parsedInt = JSON.parseObject(ip,	int[].class);
				byte[] parseByte = {0,0,0,0};
				for(int i = 0; i < parseByte.length; ++i){
					parseByte[i] = (byte) (parsedInt[i] & 0xff);
				}
				json.put(OPTION_FIELD_VALUE, parseByte);
			}else{
				byte[] parsed = TypeUtils.castToBytes(obj);
				checkArgument(IPv4Option.VALIDATOR.apply(parsed),"INVALID IPv4 address");
			}
		}catch (JSONException e) {
			throw new IllegalArgumentException("INVALID IPv4 address");
		}
	}
	/**
	 * 重新解析value字段为MAC类型,允许输入格式为ff:20:20:20:20:20格式的ip地址
	 * @param json
	 */
	private static void refreshValue4MAC(Map<String,Object> json){		
		
		Object obj = json.get(OPTION_FIELD_VALUE);
		try{
			if(MACOption.STR_VALIDATOR.apply(obj.toString())){
				String hex = obj.toString().replace(":", "");
				json.put(OPTION_FIELD_VALUE, FaceUtilits.hex2Bytes(hex));
			}else{
				byte[] parsed = TypeUtils.castToBytes(obj);
				checkArgument(MACOption.VALIDATOR.apply(parsed),"INVALID MAC address");
			}
		}catch (JSONException e) {
			throw new IllegalArgumentException("INVALID MAC address");
		}
	}
}

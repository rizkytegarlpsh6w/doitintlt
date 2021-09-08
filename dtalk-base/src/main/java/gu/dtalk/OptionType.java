package gu.dtalk;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import com.alibaba.fastjson.util.TypeUtils;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Sets;

import gu.simplemq.json.BaseJsonEncoder;
import net.gdface.utils.FaceUtilits;

import static gu.dtalk.CommonConstant.*;
import static com.google.common.base.Preconditions.*;

public enum OptionType {
	/** 字符串  */STRING(StringOption.class),
	/** 整数     */INTEGER(IntOption.class),
	/** 浮点数 */FLOAT(FloatOption.class),
	/** 布尔型 true/false 0/1 yes/no,on/off */BOOL(BoolOption.class),
	/** 日期  yyyy-MM-dd HH:mm:ss  */DATE(DateOption.class),
	/** url字符串 */URL(UrlOption.class),
	/** 密码字符串 */PASSWORD(PasswordOption.class),
	/** base64 格式二进制数据 */BASE64(Base64Option.class),
	/** MAC地址二进制数据 */MAC(MACOption.class, "^([a-fA-F0-9]{2}:){5}[a-fA-F0-9]{2}$"),
	/** IP地址二进制数据 */IP(IPv4Option.class,"^((?:(?:25[0-5]|2[0-4]\\d|[01]?\\d?\\d)\\.){3}(?:25[0-5]|2[0-4]\\d|[01]?\\d?\\d))$"),
	/** base64 格式JPEG/BMP/PNG格式图像 */IMAGE(ImageOption.class),
	/** 多选项(n>1) */@SuppressWarnings("unchecked")
	MULTICHECK(CheckOption.class,"^\\s*(\\d+)?([,;\\s]+\\d+)?\\s*$"),
	/** 单选开关(n>1) */@SuppressWarnings("unchecked")
	SWITCH(SwitchOption.class,"^\\s*\\d+\\s*$");
	final String regex;
	private volatile Type targetType;
	@SuppressWarnings("rawtypes")
	final Class<? extends BaseOption> implClass;
	private <T,B extends BaseOption<T>>OptionType(Class<B> implClass) {
		this(implClass,"");
	}
	private <T,B extends BaseOption<T>>OptionType(Class<B> implClass,String regex) {
		this.implClass = checkNotNull(implClass,"implClass is null");
		this.regex = checkNotNull(regex,"regex is null");
	}
	final Predicate<String> strValidator = new Predicate<String>() {
		@Override
		public boolean apply(String input) {
			if(regex.isEmpty()){
				return true;
			}
			return input != null && input.matches(regex);
		}
	};
	private static final Cache<OptionType, Function<String, ?>> cache = CacheBuilder.newBuilder().build();
	@SuppressWarnings("unchecked")
	private <T> Function<String, T> internalTrans(){

		switch(this){
		case MAC:
			// 解析value字段为MAC类型,允许输入格式为ff:20:20:20:20:20格式的ip地址
			return (Function<String, T>) new Function<String, byte[]>(){

				@Override
				public byte[] apply(String input) {
					if(strValidator.apply(input)){
						String hex = input.replace(":", "");
						return FaceUtilits.hex2Bytes(hex);
					}
					return null;
				}};
		case IP:
			// 解析value字段为ipv4类型,允许输入格式为127.0.0.1格式的ip地址
			return (Function<String, T>) new Function<String, byte[]>() {
				
				@Override
				public byte[] apply(String input) {
					if(strValidator.apply(input)){
						String[] ip = input.split("\\.");
						// [192,168,1,1]这样的数组大于127的值直接解析为byte会溢出，所以要先解析为int[]再转为byte[]
						byte[] parseByte = new byte[ip.length];
						for(int i = 0; i < parseByte.length; ++i){
							parseByte[i] = (byte) (Integer.valueOf(ip[i]) & 0xff);
						}
						return parseByte;
					}
					return null;
				}
			};
		case MULTICHECK:
		case SWITCH:
			// 解析value字段为Set<Integer>类型
			return (Function<String, T>) new Function<String, Set<Integer>>(){

				@Override
				public Set<Integer> apply(String input) {
					if(strValidator.apply(input)){
						String[] numlist = input.split("[;,\\s]+");
						HashSet<Integer> set = Sets.newHashSet();
						for(String num:numlist){
							if(num.trim().isEmpty()){
								set.add(Integer.valueOf(num.trim()));
							}
						}
						return set;
					}
					return null;
				}}; 		
		default:
			return new DefaultStringTransformer<>(getTargetType());
		
		}
	} 
	
	/**
	 * 返回对应类型String到目标数据类型的转换器
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> Function<String, T> trans(){
		try {
			return (Function<String, T>) cache.get(this, new Callable<Function<String, T>>(){

				@Override
				public Function<String, T> call() throws Exception {
					return internalTrans();
				}});
		} catch (ExecutionException e) {
        	Throwables.throwIfUnchecked(e.getCause());
            throw new RuntimeException(e.getCause());
		}
	}
	/**
	 * 重新解析name指定的字段
	 * @param json
	 * @param name 字段名
	 */
	private void refreshValue(Map<String,Object> json,String name){
		Function<String, Object> t = trans();
		// 使用默认transformer的类型直接跳过
		if(!(t instanceof DefaultStringTransformer)){
			if(null != json.get(name)){

				String valuestr = json.get(name).toString();

				Object parsed = t.apply(valuestr);
				if(parsed != null){
					json.put(name, parsed);
				}
			}
		}
	}
	private class DefaultStringTransformer<T> implements Function<String, T>{
		
		private final Type type;
		public DefaultStringTransformer(Type type) {
			super();
			this.type = checkNotNull(type,"type is null");
		}
		@Override
		public T apply(String input) {
			return BaseJsonEncoder.getEncoder().fromJson(input, type);
		}		
	}
	public static BaseOption<?> parseOption(Map<String,Object> json){
		OptionType optionType = OptionType.valueOf((String) json.get(OPTION_FIELD_TYPE));
		optionType.refreshValue(json, OPTION_FIELD_VALUE);
		optionType.refreshValue(json, OPTION_FIELD_DEFAULT);
		return TypeUtils.castToJavaBean(json, optionType.implClass);
	}
	
	public Type getTargetType() {
		if(null == targetType){
			synchronized (this) {
				if (targetType != null) {
					try {
						targetType = implClass.newInstance().type;
					} catch (Exception e) {
						Throwables.throwIfUnchecked(e);
						throw new RuntimeException(e);
					}	
				}
			}
		}
		return targetType;
	}
}

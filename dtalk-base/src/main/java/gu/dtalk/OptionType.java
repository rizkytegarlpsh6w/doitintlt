package gu.dtalk;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.parser.ParserConfig;
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

/**
 * 选项类型
 * @author guyadong
 *
 */
public enum OptionType {
	/** 字符串  */
	STRING(StringOption.class),
	/** 整数     */
	INTEGER(IntOption.class),
	/** 浮点数 */
	FLOAT(FloatOption.class),
	/** 布尔型 true/false 0/1 */
	BOOL(BoolOption.class),
	/** 日期  yyyy-MM-dd HH:mm:ss  */
	DATE(DateOption.class),
	/** url字符串 */
	URL(UrlOption.class),
	/** 密码字符串 */
	PASSWORD(PasswordOption.class),
	/** e-mail地址 */
	EMAIL(StringOption.class,"^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$"),
	/** 手机号码(11位) */
	MPHONE(StringOption.class,"^(13[0-9]|14[5|7]|15[0|1|2|3|5|6|7|8|9]|18[0|1|2|3|5|6|7|8|9])\\d{8}$"),
	/** 身份证号(15位、18位数字)，最后一位是校验位，可能为数字或字符X */
	IDNUM(StringOption.class,"(^\\d{15}$)|(^\\d{18}$)|(^\\d{17}(\\d|X|x)$)"),
	/** base64 格式二进制数据 */
	BASE64(Base64Option.class),
	/** (6字节)MAC地址二进制数据,允许用':','-'分隔或不分隔 */
	MAC(MACOption.class, "^([a-fA-F0-9]{2}[:-]?){5}[a-fA-F0-9]{2}$"),
	/** IP地址二进制数据 */
	IP(IPv4Option.class,"^((?:(?:25[0-5]|2[0-4]\\d|[01]?\\d?\\d)\\.){3}(?:25[0-5]|2[0-4]\\d|[01]?\\d?\\d))$"),
	/** base64 格式JPEG/BMP/PNG格式图像 */
	IMAGE(ImageOption.class),
	/** 多选项{@code (n>1)} */
	@SuppressWarnings("unchecked")
	MULTICHECK(CheckOption.class),
	/** 单选开关{@code (n>1)} */
	@SuppressWarnings("unchecked")
	SWITCH(SwitchOption.class);
	final String regex;
	private volatile Type targetType;
	/**
	 * 实现数据选项的类
	 */
	@SuppressWarnings("rawtypes")
	final Class<? extends BaseOption> optClass;
	private <T,B extends BaseOption<T>>OptionType(Class<B> implClass) {
		this(implClass,"");
	}
	private <T,B extends BaseOption<T>>OptionType(Class<B> implClass,String regex) {
		this.optClass = checkNotNull(implClass,"implClass is null");
		this.regex = checkNotNull(regex,"regex is null");
	}
	/**
	 * 字符串验证器,根据正则表达式判断字符串是否符合当前数据类型的格式,
	 * 输入为null或正则表达式不匹配则返回false
	 */
	public final Predicate<String> strValidator = new Predicate<String>() {
		@Override
		public boolean apply(String input) {
			if(regex.isEmpty()){
				return true;
			}
			return input != null && input.matches(regex);
		}
	};
	private static final Cache<OptionType, Function<String, ?>> cache = CacheBuilder.newBuilder().build();
	/**
	 * 返回从字符串转到指定类型的转换器实例<br>
	 * 返回的转换器特征：将字符串转换为指定的类型，如果输入的字符串格式无效则抛出异常
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private <T> Function<String, T> internalTrans(){

		switch(this){
		case EMAIL:
		case MPHONE:
		case IDNUM:
			// 解析字符串为EMAIL类型,允许输入格式为ff:20:20:20:20:20格式的ip地址
			return (Function<String, T>) new Function<String, String>(){
				@Override
				public String apply(String input) {
					checkArgument(strValidator.apply(input),
							"INVALID FORMAT '%s' FOR %s", input,OptionType.this.name());
					return input;
				}};
		case DATE:
			// 解析字符串为DATE类型,允许日期，时间，日期+时间 三种格式
			return (Function<String, T>) new Function<String, Date>(){

				@Override
				public Date apply(String input) {
			        Date date = null;
			        if(null != input){
			        	try {
			        		date = new SimpleDateFormat(ISO8601_FORMATTER_STR).parse(input);
						} catch (ParseException e) {
				        	try {
				        		date = new SimpleDateFormat(TIMESTAMP_FORMATTER_STR).parse(input);
				        	} catch (ParseException e1) {
				        		try {
				        			date = new SimpleDateFormat(DATE_FORMATTER_STR).parse(input);
				        		} catch (ParseException e2) {
				        			try {
				        				date = new SimpleDateFormat(TIME_FORMATTER_STR).parse(input);
				        			} catch (ParseException e3) {
				        				throw new IllegalArgumentException(String.format("INVALID FORMAT '%s' FOR %s", input,OptionType.this.name()));
				        			}
				        		}
				        	}
						}
			        }
					return date;
				}};
		case MAC:
			// 解析字符串为MAC类型,允许输入格式为ff:20:20:20:20:20格式的ip地址
			return (Function<String, T>) new Function<String, byte[]>(){

				@Override
				public byte[] apply(String input) {
					checkArgument(strValidator.apply(input),
							"INVALID FORMAT '%s' FOR %s", input,OptionType.this.name());
					String hex = input.replace(":", "");
					return FaceUtilits.hex2Bytes(hex);
				}};
		case IP:
			// 解析字符串为ipv4类型,允许输入格式为127.0.0.1格式的ip地址
			return (Function<String, T>) new Function<String, byte[]>() {
				
				@Override
				public byte[] apply(String input) {
					checkArgument(strValidator.apply(input),
							"INVALID FORMAT '%s' FOR %s", input,OptionType.this.name());

					String[] ip = input.split("\\.");
					// [192,168,1,1]这样的数组大于127的值直接解析为byte会溢出，所以要先解析为int[]再转为byte[]
					byte[] parseByte = new byte[ip.length];
					for(int i = 0; i < parseByte.length; ++i){
						parseByte[i] = (byte) (Integer.valueOf(ip[i]) & 0xff);
					}
					return parseByte;
				
				}
			};
		case MULTICHECK:
		case SWITCH:
			// 解析字符串为Set<Integer>类型
			return (Function<String, T>) new Function<String, Set<Integer>>(){

				@Override
				public Set<Integer> apply(String input) {
					checkArgument(strValidator.apply(input),
							"INVALID FORMAT '%s' FOR %s", input,OptionType.this.name());

					String[] numlist = input.split("[;,\\s]+");
					HashSet<Integer> set = Sets.newHashSet();
					for(String num:numlist){
						if(!num.isEmpty()){
							set.add(Integer.valueOf(num));
						}
					}
					return set;
				}}; 		
		default:
			return new DefaultStringTransformer<>(getTargetType());
		}
	} 
	
	/**
	 * 返回对应类型String到目标数据类型的转换器<br>
	 * 返回的转器实例将字符器转换为当前类型的数据，转换失败则抛出异常
	 * @param <T> 目标数据类型
	 * @return {@link Function }对象
	 * @see #internalTrans()
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
	 * 如果有定制转换器(非{@link DefaultStringTransformer}),则尝试重新解析name指定的字段
	 * {@value CommonConstant#OPTION_FIELD_VALUE},
	 * {@value CommonConstant#OPTION_FIELD_DEFAULT}
	 * @param json
	 * @param name 字段名
	 */
	private void refreshValueIfTransPresent(Map<String,Object> json,String name){
		Function<String, Object> t = trans();
		// 使用默认transformer的类型直接跳过
		if(!(t instanceof DefaultStringTransformer)){
			if(null != json.get(name)){

				String valuestr = json.get(name).toString();
				try {
					Object parsed = t.apply(valuestr);
					if(parsed != null){
						json.put(name, parsed);
					}	
				} catch (Exception e) {
				}
				
			}
		}
	}
	public <T, O extends BaseOption<T>>OptionBuilder<T, O> builder() {
		return OptionBuilder.<T,O>builder(this);
	}
	/**
	 * 默认字符串到T类型的转换器
	 * @author guyadong
	 *
	 * @param <T>
	 */
	private class DefaultStringTransformer<T> implements Function<String, T>{
		
		private final Type type;
		public DefaultStringTransformer(Type type) {
			super();
			this.type = checkNotNull(type,"type is null");
		}
		/**
		 * 调用fastjson对输入字符串解析返回指定类型的对象，如果格式不对则抛出异常
		 * @see com.google.common.base.Function#apply(java.lang.Object)
		 */
		@Override
		public T apply(String input) {
			try {
				return BaseJsonEncoder.getEncoder().fromJson(input, type);
			} catch (JSONException e) {
				if( !input.startsWith("\"") && !input.endsWith("\"")){
					try {
						return BaseJsonEncoder.getEncoder().fromJson("\"" + input + "\"", type);
					} catch (JSONException e2) {
					}
				}
				throw e;
			}
		}		
	}
	public static BaseOption<?> parseOption(Map<String,Object> json){
		OptionType optionType =TypeUtils.cast(
				checkNotNull(json.get(OPTION_FIELD_TYPE),"NOT FOUND %s field",OPTION_FIELD_TYPE), 
				OptionType.class, 
				ParserConfig.getGlobalInstance());
		optionType.refreshValueIfTransPresent(json, OPTION_FIELD_VALUE);
		optionType.refreshValueIfTransPresent(json, OPTION_FIELD_DEFAULT);
		return BaseJsonEncoder.getEncoder().fromJson(	JSON.toJSONString(json), optionType.optClass);
	}
	
	private Type getTargetType() {
		if(null == targetType){
			synchronized (this) {
				if (targetType == null) {
					try {
						targetType = optClass.newInstance().type;
					} catch (Exception e) {
						Throwables.throwIfUnchecked(e);
						throw new RuntimeException(e);
					}	
				}
			}
		}
		return targetType;
	}
	/**
	 * @return 返回实现类
	 */
	@SuppressWarnings("rawtypes")
	public Class<? extends BaseOption> optionClass() {
		return optClass;
	}
}

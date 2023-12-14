package gu.dtalk;

/**
 * 格式化的响应数据对象用于 {@link Ack}处理响应数据<br>
 * 设备端可以将响应数据封装在此对象中，通过contentType,howtodo字段告诉前端数据类型和希望前端处理数据的方式
 * @author guyadong
 *
 */
public class FormatedValue {
	/** 响应对象的原始数据 */
	public final Object value;
	/** MIME格式表示的数据类型  */
	public final String contentType;
	/** 希望前端处理处理的方式,参见{@link HowtodoPredefineType}预定义类型 */
	public final String howtodo;
	/**
	 * 构造方法
	 * @param value 响应对象的原始数据
	 * @param contentType MIME格式表示的数据类型 
	 * @param howtodo 希望前端处理处理的方式,参见{@link HowtodoPredefineType}预定义类型
	 */
	public FormatedValue(Object value, String contentType, String howtodo) {
		this.value = value;
		this.contentType = contentType;
		this.howtodo = howtodo;
	}
	public static class HowtodoPredefineType{
		/** 保存数据为本地文件 */
		public static final String SAVE =  "SAVE";
		/** 以适当的方式显示数据 */
		public static final String SHOW =  "SHOW";
	}
}
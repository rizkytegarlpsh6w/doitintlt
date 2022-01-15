package gu.dtalk;

import com.google.common.base.Throwables;

import net.gdface.utils.FaceUtilits;

/**
 * 二进制数据选项基类
 * @author guyadong
 *
 */
public abstract class BaseBinary extends BaseOption<byte[]> {

	public BaseBinary() {
		super(byte[].class);
	}

	@Override
	public String contentOfValue() {
		byte[] value = (byte[])getValue();
		if(value == null){
			return super.contentOfValue();
		}
		if(value.length <=32){
			return FaceUtilits.toHex(value);
		}
		return "BINARY";
	}
	/**
	 * 从input中读取字节流转为byte[]调用{@link #setValue(Object)}
	 * @param <T> 参见 {@link FaceUtilits#getBytes(Object)}
	 */
	protected <T>BaseOption<byte[]> asValue(T input) {
		try {
			return setValue(FaceUtilits.getBytes(input));
		} catch (Exception e) {
			Throwables.throwIfUnchecked(e);
			throw new RuntimeException(e);
		}
	}
	/**
	 * 从input中读取字节流转为byte[]调用{@link #setDefaultValue(Object)}
	 * @param <T> 参见 {@link FaceUtilits#getBytes(Object)}
	 */
	protected <T>BaseOption<byte[]> asDefaultValue(T input) {
		try {
			return setDefaultValue(FaceUtilits.getBytes(input));
		} catch (Exception e) {
			Throwables.throwIfUnchecked(e);
			throw new RuntimeException(e);
		}
	}
}

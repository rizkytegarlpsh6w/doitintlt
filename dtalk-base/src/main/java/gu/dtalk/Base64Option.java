package gu.dtalk;

import com.google.common.base.Throwables;

import net.gdface.utils.BinaryUtils;

/**
 * base64格式二进制数据选项
 * @author guyadong
 *
 */
public class Base64Option extends BaseBinary {

	public Base64Option() {
	}

	@Override
	public OptionType getType() {
		return OptionType.BASE64;
	}
	@Override
	public Base64Option asValue(String input) {
		try {
			setValue(BinaryUtils.getBytes(input));
			return this;
		} catch (Throwable e) {
			Throwables.throwIfUnchecked(e);
			throw new RuntimeException(e);
		}
	}
	/**
	 * 从input中读取字节流转为byte[]调用{@link #setValue(Object)}
	 * @param <T> 参见 {@link BinaryUtils#getBytes(Object)}
	 */
	public <T>Base64Option asValue(T input) {
		try {
			setValue(BinaryUtils.getBytes(input));
			return this;
		} catch (Throwable e) {
			Throwables.throwIfUnchecked(e);
			throw new RuntimeException(e);
		}
	}
	@Override
	public Base64Option asDefaultValue(String input) {
		try {
			setDefaultValue(BinaryUtils.getBytes(input));
			return this;
		} catch (Throwable e) {
			Throwables.throwIfUnchecked(e);
			throw new RuntimeException(e);
		}
	}
	/**
	 * 从input中读取字节流转为byte[]调用{@link #setDefaultValue(Object)}
	 * @param <T> 参见 {@link BinaryUtils#getBytes(Object)}
	 */
	public <T>Base64Option asDefaultValue(T input) {
		try {
			setDefaultValue(BinaryUtils.getBytes(input));
			return this;
		} catch (Throwable e) {
			Throwables.throwIfUnchecked(e);
			throw new RuntimeException(e);
		}
	}
}

package gu.dtalk;

import com.google.common.base.Throwables;

import net.gdface.utils.FaceUtilits;

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
			setValue(FaceUtilits.getBytes(input));
			return this;
		} catch (Exception e) {
			Throwables.throwIfUnchecked(e);
			throw new RuntimeException(e);
		}
	}
	/**
	 * 从input中读取字节流转为byte[]调用{@link #setValue(byte[])}
	 * @param <T> 参见 {@link FaceUtilits#getBytes(Object)}
	 */
	public <T>Base64Option asValue(T input) {
		try {
			setValue(FaceUtilits.getBytes(input));
			return this;
		} catch (Exception e) {
			Throwables.throwIfUnchecked(e);
			throw new RuntimeException(e);
		}
	}
	@Override
	public Base64Option asDefaultValue(String input) {
		try {
			setDefaultValue(FaceUtilits.getBytes(input));
			return this;
		} catch (Exception e) {
			Throwables.throwIfUnchecked(e);
			throw new RuntimeException(e);
		}
	}
	/**
	 * 从input中读取字节流转为byte[]调用{@link #setDefaultValue(byte[])}
	 * @param <T> 参见 {@link FaceUtilits#getBytes(Object)}
	 */
	public <T>Base64Option asDefaultValue(T input) {
		try {
			setDefaultValue(FaceUtilits.getBytes(input));
			return this;
		} catch (Exception e) {
			Throwables.throwIfUnchecked(e);
			throw new RuntimeException(e);
		}
	}
}

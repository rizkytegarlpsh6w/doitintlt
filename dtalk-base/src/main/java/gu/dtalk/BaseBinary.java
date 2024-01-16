package gu.dtalk;

import java.util.Collections;
import java.util.List;

import com.google.common.base.Throwables;

import net.gdface.utils.BinaryUtils;

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
			return BinaryUtils.toHex(value);
		}
		return "BINARY";
	}
	/**
	 * 从input中读取字节流转为byte[]调用{@link #setValue(Object)}
	 * @param <T> 参见 {@link BinaryUtils#getBytes(Object)}
	 * @param input 输入数据
	 * @return 当前对象
	 */
	protected <T>BaseOption<byte[]> asValue(T input) {
		try {
			return setValue(BinaryUtils.getBytes(input));
		} catch (Throwable e) {
			Throwables.throwIfUnchecked(e);
			throw new RuntimeException(e);
		}
	}
	/**
	 * 从input中读取字节流转为byte[]调用{@link #setDefaultValue(Object)}
	 * @param <T> 参见 {@link BinaryUtils#getBytes(Object)}
	 * @param input 输入数据
	 * @return 当前对象
	 */
	protected <T>BaseOption<byte[]> asDefaultValue(T input) {
		try {
			return setDefaultValue(BinaryUtils.getBytes(input));
		} catch (Throwable e) {
			Throwables.throwIfUnchecked(e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<byte[]> getAvailable() {
		return Collections.emptyList();
	}
}

package gu.dtalk;

import com.google.common.base.Throwables;

import net.gdface.utils.FaceUtilits;

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
	@Override
	public BaseOption<byte[]> asValue(String input) {
		try {
			return setValue(FaceUtilits.getBytes(input));
		} catch (Exception e) {
			Throwables.throwIfUnchecked(e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public BaseOption<byte[]> asDefaultValue(String input) {
		try {
			return setDefaultValue(FaceUtilits.getBytes(input));
		} catch (Exception e) {
			Throwables.throwIfUnchecked(e);
			throw new RuntimeException(e);
		}
	}
	public <T>BaseOption<byte[]> asValue(T input) {
		try {
			return setValue(FaceUtilits.getBytes(input));
		} catch (Exception e) {
			Throwables.throwIfUnchecked(e);
			throw new RuntimeException(e);
		}
	}

	public <T>BaseOption<byte[]> asDefaultValue(T input) {
		try {
			return setDefaultValue(FaceUtilits.getBytes(input));
		} catch (Exception e) {
			Throwables.throwIfUnchecked(e);
			throw new RuntimeException(e);
		}
	}
}

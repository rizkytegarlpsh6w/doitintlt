package gu.dtalk;

import java.net.NetworkInterface;

import com.google.common.base.Predicate;
import com.google.common.base.Throwables;

import net.gdface.utils.NetworkUtil;

/**
 * mac(6 bytes)地址选项类型
 * @author guyadong
 *
 */
public class MACOption extends BaseBinary {
	public static final Predicate<byte[]> VALIDATOR = new Predicate<byte[]>() {
		@Override
		public boolean apply(byte[] input) {
			return input != null && input.length == 6;
		}
	};
	public MACOption() {
		setValidator(VALIDATOR);
	}
	@Override
	public OptionType getType() {
		return OptionType.MAC;
	}

	@Override
	public String contentOfValue() {
		if(getValue() == null){
			return super.contentOfValue();
		}
		return NetworkUtil.formatMac((byte[]) getValue(), ":");		
	}
	public MACOption asValue(NetworkInterface input) {
		try {
			setValue(input.getHardwareAddress());
			return this;
		} catch (Exception e) {
			Throwables.throwIfUnchecked(e);
			throw new RuntimeException(e);
		}
	}
	public MACOption asDefaultValue(NetworkInterface input) {
		try {
			setDefaultValue(input.getHardwareAddress());
			return this;
		} catch (Exception e) {
			Throwables.throwIfUnchecked(e);
			throw new RuntimeException(e);
		}
	}
}

package gu.dtalk;

import com.google.common.base.Predicate;

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
	public static final Predicate<String> STR_VALIDATOR = new Predicate<String>() {
		@Override
		public boolean apply(String input) {
			return input != null && input.matches("^([a-fA-F0-9]{2}:){5}[a-fA-F0-9]{2}$");
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

}

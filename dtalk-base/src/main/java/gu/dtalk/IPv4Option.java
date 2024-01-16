package gu.dtalk;

import java.net.InetAddress;

import com.google.common.base.Predicate;
import com.google.common.base.Throwables;

import net.gdface.utils.NetworkUtil;

/**
 * IP(ipv4)地址选项类型
 * @author guyadong
 *
 */
public class IPv4Option extends BaseBinary {
	public static final Predicate<byte[]> VALIDATOR = new Predicate<byte[]>() {
		@Override
		public boolean apply(byte[] input) {
			return input != null && input.length==4;
		}
	};
	public IPv4Option() {
		setValidator(VALIDATOR);
	}
	@Override
	public OptionType getType() {
		return OptionType.IP;
	}
	@Override
	public String contentOfValue() {
		if(getValue() == null){
			return super.contentOfValue();
		}
		return NetworkUtil.formatIp((byte[]) getValue());		
	}
	public IPv4Option asValue(InetAddress input) {
		try {
			setValue(input.getAddress());
			return this;
		} catch (Throwable e) {
			Throwables.throwIfUnchecked(e);
			throw new RuntimeException(e);
		}
	}
	public IPv4Option asDefaultValue(InetAddress input) {
		try {
			setDefaultValue(input.getAddress());
			return this;
		} catch (Throwable e) {
			Throwables.throwIfUnchecked(e);
			throw new RuntimeException(e);
		}
	}
}

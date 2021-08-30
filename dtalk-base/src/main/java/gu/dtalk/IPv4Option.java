package gu.dtalk;

import com.google.common.base.Predicate;

import net.gdface.utils.NetworkUtil;

/**
 * IP(ipv4)地址选项类型
 * @author guyadong
 *
 */
public class IPv4Option extends BaseBinary {

	public IPv4Option() {
		setValidator(new Predicate<byte[]>() {
			@Override
			public boolean apply(byte[] input) {
				return input != null && input.length==4;
			}
		});
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
}

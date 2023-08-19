package gu.dtalk.engine;

import java.io.IOException;

import gu.dtalk.DeviceInfoProvider;
import net.gdface.utils.BinaryUtils;
import net.gdface.utils.NetworkUtil;

public class DefaultDevInfoProvider implements DeviceInfoProvider {

	public static DeviceInfoProvider INSTANCE = new DefaultDevInfoProvider();
	private byte[] mac={0,0,0,0,0,0};
	private byte[] ip = {127,0,0,1};
	public DefaultDevInfoProvider() {
		this("www.google.com:80","www.baidu.com:80","www.qq.com:80","www.aliyun.com:80");
	}
	protected DefaultDevInfoProvider(String host,int port) {
		try {
			// 使用localhost获取本机MAC地址会返回空数组，所以这里使用一个互联地址来获取
			if(host == null || "127.0.0.1".equals(host) || "localhost".equalsIgnoreCase(host)){
				host = "www.baidu.com";
				port = 80;
			}
			mac = NetworkUtil.getCurrentMac(host, port);
			ip  = NetworkUtil.getLocalIp(host, port).getAddress();
		} catch (IOException e) {
		}
	}
	protected DefaultDevInfoProvider(String ...hostAndPorts){
		try {
			mac = NetworkUtil.getCurrentMac(hostAndPorts);
			ip  = NetworkUtil.getLocalIp(hostAndPorts).getAddress();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	@Override
	public String getPassword() {
		// 返回mac地址后4位做默认密码
		return BinaryUtils.toHex(mac).substring(8);
	}

	@Override
	public void savePassword(String password) {

	}

	@Override
	public byte[] getMac() {
		return mac;
	}

	@Override
	public String getMacAsString() {
		return NetworkUtil.formatMac(getMac(), null);
	}
	@Override
	public byte[] getIp() {
		return ip;
	}

	@Override
	public String getIpAsString() {
		return NetworkUtil.formatIp(getIp());
	}
}

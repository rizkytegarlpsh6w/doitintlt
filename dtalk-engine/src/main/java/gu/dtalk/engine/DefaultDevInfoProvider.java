package gu.dtalk.engine;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import gu.dtalk.DeviceInfoProvider;
import net.gdface.utils.FaceUtilits;
import net.gdface.utils.NetworkUtil;

public class DefaultDevInfoProvider implements DeviceInfoProvider {

	public static DeviceInfoProvider INSTANCE = new DefaultDevInfoProvider();
	private byte[] mac={0,0,0,0,0,0};
	private byte[] ip = {127,0,0,1};
	public DefaultDevInfoProvider() {
		this("www.baidu.com", 80);
	}
	protected DefaultDevInfoProvider(String host,int port) {		
		try {
			// 使用localhost获取本机MAC地址会返回空数组，所以这里使用一个互联地址来获取
			if(host == null || "127.0.0.1".equals(host) || "localhost".equalsIgnoreCase(host)){
				host = "www.baidu.com";
				port = 80;
			}
			mac = NetworkUtil.getCurrentMac(host, port);
			ip  = getLocalIp(host, port);
		} catch (IOException e) {			
		}
	}
	@Override
	public String getPassword() {
		// 返回mac地址后4位做默认密码
		return FaceUtilits.toHex(mac).substring(8);
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
	/**
	 * 获取访问指定host的本地IP地址
	 * @param host
	 * @param port
	 * @return
	 * @throws IOException
	 */
	public static byte[] getLocalIp(String host,int port) throws IOException {
		Socket socket = null;
		try {
			socket = new Socket(host,port);
			InetAddress address = socket.getLocalAddress();
			return address.getAddress();
		} finally{
			if(socket != null){
				socket.close();
			}
		}
	}
}

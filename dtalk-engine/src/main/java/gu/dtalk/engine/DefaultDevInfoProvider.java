package gu.dtalk.engine;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import gu.dtalk.DeviceInfoProvider;
import net.gdface.utils.FaceUtilits;
import net.gdface.utils.NetworkUtil;

public class DefaultDevInfoProvider implements DeviceInfoProvider {

	public static DefaultDevInfoProvider INSTANCE = new DefaultDevInfoProvider();
	private byte[] mac={0,0,0,0,0,0};
	private byte[] ip = {127,0,0,1};
	public DefaultDevInfoProvider() {
		
		try {
			mac = NetworkUtil.getCurrentMac("www.cnnic.net.cn", 80);
			ip  = getLocalIp("www.cnnic.net.cn", 80);
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
	public byte[] getIp() {
		return ip;
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

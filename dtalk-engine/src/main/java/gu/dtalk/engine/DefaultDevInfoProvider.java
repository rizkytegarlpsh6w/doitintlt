package gu.dtalk.engine;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;

import gu.dtalk.DeviceInfoProvider;
import net.gdface.utils.FaceUtilits;

public class DefaultDevInfoProvider implements DeviceInfoProvider {

	public static DefaultDevInfoProvider INSTANCE = new DefaultDevInfoProvider();
	private byte[] mac={0,0,0,0,0,0};
	public DefaultDevInfoProvider() {
		
		try {
			mac = getCurrentMac();
		} catch (IOException e) {			
		}
	}

	@Override
	public String getPassword() {
		return FaceUtilits.toHex(mac);
	}

	@Override
	public void savePassword(String password) {

	}
	public byte[] getCurrentMac() throws IOException {
		Socket socket = null;
		try {
			// 获取当前网络地址
			socket = new Socket("www.cnnic.net.cn", 80);
			InetAddress address = socket.getLocalAddress();
			NetworkInterface nic = NetworkInterface.getByInetAddress(address);
			return nic.getHardwareAddress();
		} finally{
			if(socket != null){
				socket.close();
			}
		}
	}
	@Override
	public byte[] getMac() {
		return mac;
	}

}

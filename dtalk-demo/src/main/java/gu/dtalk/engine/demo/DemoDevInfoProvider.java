package gu.dtalk.engine.demo;

import java.io.IOException;
import gu.dtalk.DeviceInfoProvider;
import net.gdface.utils.FaceUtilits;
import net.gdface.utils.NetworkUtil;

public class DemoDevInfoProvider implements DeviceInfoProvider {

	private byte[] mac={0,0,0,0,0,0};
	public DemoDevInfoProvider() {
		
		try {
			mac = NetworkUtil.getCurrentMac("www.cnnic.net.cn", 80);
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

}

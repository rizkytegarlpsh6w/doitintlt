package gu.dtalk;

import static gu.dtalk.CommonConstant.*;

import net.gdface.utils.FaceUtilits;

public class CommonUtils {

	/**
	 * 返回响应通道名
	 * @param mac
	 * @return
	 */
	public static String getAckChannel(String mac){
		return mac + ACK_SUFFIX;
	}
	public static String getAckChannel(byte[] mac){
		return getAckChannel(FaceUtilits.toHex(mac));
	}
	public static String getConnChannel(String mac){
		return mac + CONNECT_SUFFIX;
	}
	public static String getConnChannel(byte[] mac){
		return getConnChannel(FaceUtilits.toHex(mac));
	}
}

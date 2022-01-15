package gu.dtalk;

/**
 * 基于密码验证的连接请求
 * @author guyadong
 *
 */
public class ConnectReq {
	/**
	 * 本机mac地址
	 */
	public String mac;
	/**
	 * 密码的MD5校验码
	 */
	public String pwd;
	public ConnectReq() {
	}
	public ConnectReq(String mac, String pwd) {
		super();
		this.mac = mac;
		this.pwd = pwd;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ConnectReq [");
		if (mac != null)
			builder.append("mac=").append(mac).append(", ");
		if (pwd != null)
			builder.append("pwd=").append(pwd);
		builder.append("]");
		return builder.toString();
	}

}

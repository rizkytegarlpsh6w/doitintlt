package gu.dtalk;

public class ConnectReq {
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

}

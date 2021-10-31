package gu.dtalk.engine;

/**
 * 验证连接请求合法性接口
 * @author guyadong
 *
 */
public interface RequestValidator {

	/**
	 * 验证连接请求合法性
	 * @param connstr 连接请求字符串
	 * @return 验证通过返回发送连接请求CLIENT的MAC地址(HEX格式)
	 * @throws Exception 连接请求验证不通过
	 */
	String validate(String connstr) throws Exception;

}
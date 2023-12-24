package gu.dtalk.engine;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 验证连接请求合法性接口
 * @author guyadong
 *
 */
public interface RequestValidator {

	/**
	 * 验证连接请求合法性
	 * @param connstr 连接请求字符串
	 * @param clientID 用于返回发送连接请求CLIENT的MAC地址(HEX格式)或其他可以唯一识别CLIENT的字符串,返回{@code null}或空视为验证失败
	 * @throws Exception 连接请求验证不通过
	 */
	void validate(String connstr, AtomicReference<String> clientID) throws Exception;

}
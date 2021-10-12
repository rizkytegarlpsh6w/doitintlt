package gu.dtalk.redis;

/**
 * redis服务器参数SPI(Service Provider Interface)加载接口
 * @author guyadong
 *
 */
public interface RedisConfigProvider {
	/**
	 * @return 返回redis主机名,不可为{@code null}
	 */
	String getHost();
	/**
	 * 保存redis主机名
	 * @param host
	 */
	void setHost(String host);
	/**
	 * 
	 * @return 返回redis端口号,<=0使用默认redis端口6379
	 */
	int getPort();
	/**
	 * 保存redis端口号
	 * @param port
	 */
	void setPort(int port);
	/**
	 * 
	 * @return 返回redis连接密码,为{@code null}使用默认密码
	 */
	String getPassword();
	/**
	 * 保存redis连接密码
	 * @param password
	 */
	void setPassword(String password);
	/**
	 * 
	 * @return 返回redis数据库id,<=0使用默认id(0)
	 */
	int getDatabase();
	/**
	 * 保存redis数据库id
	 * @param database
	 */
	void setDatabase(int database);
	/**
	 * 
	 * @return 返回redis超时连接参数,<=0使用默认值
	 */
	long getTimeout();
	void setTimeout(long timeout);
	RedisConfigType type();
}

package gu.dtalk.redis;

import java.net.URI;

/**
 * redis服务器参数SPI(Service Provider Interface)读写接口
 * @author guyadong
 *
 */
public interface RedisConfigProvider {
	/**
	 * @return 返回redis主机名,当{@link #getURI()}返回{@code null}时，不可为{@code null}
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
	int getTimeout();
	/**
	 * 保存redis超时连接参数
	 * @param timeout
	 */
	void setTimeout(int timeout);
	/**
	 * 以{@link URI}对象返回连接redis数据库所需要的host/port/password/database参数，
	 * 此方法不为{@code null}时，忽略{@link #getHost()},{@link #getPort()},{@link #getPassword()},{@link #getDatabase()}方法返回的参数
	 * @return
	 */
	URI getURI();
	/**
	 * 以{@link URI}对象保存接redis数据库所需要的host/port/password/database参数，
	 * @param uri
	 */
	void setURI(URI uri);
	/**
	 * 返回当前配置的连接类型,不可为{@code null}
	 * @return
	 */
	RedisConfigType type();
}

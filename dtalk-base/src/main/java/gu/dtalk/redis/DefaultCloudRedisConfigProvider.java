package gu.dtalk.redis;

import java.net.URI;
import java.net.URISyntaxException;

import redis.clients.util.JedisURIHelper;

import static com.google.common.base.Preconditions.*;
/**
 * 公有云配置
 * @author guyadong
 *
 */
public class DefaultCloudRedisConfigProvider implements RedisConfigProvider {
	/** 默认的公有云redis连接 */
	public static final URI DEFAULT_CLOUD_REDIS_URI;	
	static{
		try {
			DEFAULT_CLOUD_REDIS_URI = new URI("jedis://:86a1b907d54bf7010394bf316e183e67@dtalk.facelib.net:26416/0");
		} catch (URISyntaxException e) {
			throw new ExceptionInInitializerError(e);
		}
	}
	private static URI uri = DEFAULT_CLOUD_REDIS_URI;
	@Override
	public String getHost() {
		return uri.getHost();
	}

	@Override
	public void setHost(String host) {

	}

	@Override
	public int getPort() {
		return uri.getPort();
	}

	@Override
	public void setPort(int port) {

	}

	@Override
	public String getPassword() {
		return JedisURIHelper.getPassword(uri);
	}

	@Override
	public void setPassword(String password) {

	}

	@Override
	public int getDatabase() {
		return JedisURIHelper.getDBIndex(uri);
	}

	@Override
	public void setDatabase(int database) {

	}

	@Override
	public int getTimeout() {
		return 0;
	}

	@Override
	public void setTimeout(int timeout) {

	}
	
	@Override
	public URI getURI() {
		return uri;
	}

	@Override
	public void setURI(URI uri) {
	}

	@Override
	public final RedisConfigType type(){
		return RedisConfigType.CLOUD;
	}
	/**
	 * 初始化 uri 
	 * @param uri 不可为{@code null}
	 */
	public static void initURI(URI uri) {
		DefaultCloudRedisConfigProvider.uri = checkNotNull(uri,"uri is null");
	}
}

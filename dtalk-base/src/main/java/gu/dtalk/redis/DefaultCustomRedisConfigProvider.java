package gu.dtalk.redis;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

import com.google.common.base.MoreObjects;
import com.google.common.base.Predicates;
import com.google.common.collect.Maps;

import gu.simplemq.redis.JedisPoolLazy.PropName;

/**
 * 自定义配置默认实现
 * @author guyadong
 *
 */
public class DefaultCustomRedisConfigProvider implements RedisConfigProvider {

	private static final Map<PropName, Object> redisParameters = Maps.newHashMap();

	public DefaultCustomRedisConfigProvider() {
	}

	@Override
	public String getHost() {
		return (String) redisParameters.get(PropName.host);
	}

	@Override
	public void setHost(String host) {
		redisParameters.put(PropName.host, host);
	}

	@Override
	public int getPort() {
		return (int) MoreObjects.firstNonNull(redisParameters.get(PropName.port),0);
	}

	@Override
	public void setPort(int port) {
		redisParameters.put(PropName.port, port);
	}

	@Override
	public String getPassword() {
		return (String) redisParameters.get(PropName.password);
	}

	@Override
	public void setPassword(String password) {
		redisParameters.put(PropName.password, password);
	}

	@Override
	public int getDatabase() {
		return  (int) MoreObjects.firstNonNull(redisParameters.get(PropName.database),0);
	}

	@Override
	public void setDatabase(int database) {
		redisParameters.put(PropName.database, database);
	}

	@Override
	public int getTimeout() {
		return (int) MoreObjects.firstNonNull(redisParameters.get(PropName.timeout),0);
	}

	@Override
	public void setTimeout(int timeout) {
		redisParameters.put(PropName.timeout, timeout);
	}

	@Override
	public URI getURI() {
		return (URI) redisParameters.get(PropName.uri);
	}

	@Override
	public void setURI(URI uri) {
		redisParameters.put(PropName.uri, uri);
	}

	@Override
	public final RedisConfigType type() {
		return RedisConfigType.CUSTOM;
	}

	/**
	 * 初始化redis参数
	 * @param redisParameters
	 */
	public static void initredisParameters(Map<PropName, Object> redisParameters){
		redisParameters = MoreObjects.firstNonNull(redisParameters, Collections.<PropName, Object>emptyMap());
		DefaultCustomRedisConfigProvider.redisParameters.clear();
		DefaultCustomRedisConfigProvider.redisParameters.putAll(redisParameters);
	}

	/**
	 * @return redisparameters
	 */
	public static Map<PropName, Object> readRedisparameters() {
		return Maps.filterValues(redisParameters, Predicates.notNull());
	}
}

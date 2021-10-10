package gu.dtalk.redis;

import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

import static com.google.common.base.Preconditions.*;
import gu.simplemq.redis.JedisPoolLazy.PropName;

public class RedisConfig {
	/**
	 * SPI(Service Provider Interface)机制加载 {@link RedisConfigProvider}实例,
	 * 没有找到返回{@link DefaultRedisConfigProvider}实例
	 * @return
	 */
	public static RedisConfigProvider getRedisConfigProvider(){
		ServiceLoader<RedisConfigProvider> providers = ServiceLoader.load(RedisConfigProvider.class);
		Iterator<RedisConfigProvider> itor = providers.iterator();
		if(!itor.hasNext()){
			return new DefaultRedisConfigProvider();
		}
		return itor.next();
	}
	/**
	 *根据SPI加载的{@link RedisConfigProvider}实例提供的参数创建Redis连接参数
	 * @return
	 */
	public static Map<PropName, Object> readRedisParam(){
		RedisConfigProvider config = getRedisConfigProvider();
		String host = config.getHost();
		int port =config.getPort();
		String password = config.getPassword();
		int database = config.getDatabase();
		long timeout = config.getTimeout();
		checkArgument(!Strings.isNullOrEmpty(host),"INVALID REDIS HOST");
		Builder<PropName, Object> builder = ImmutableMap.builder();
		builder.put(PropName.host, host);
		if(port >0){
			builder.put(PropName.port, port);
		}
		if(!Strings.isNullOrEmpty(password)){
			builder.put(PropName.password, password);
		}
		if(database > 0){
			builder.put(PropName.database, database);
		}
		if(timeout >0){
			builder.put(PropName.timeout, timeout);
		}
		return builder.build();
	}
	public static void saveRedisParam(Map<PropName, Object> param){
		RedisConfigProvider config = getRedisConfigProvider();
		String host = (String) param.get(PropName.host);
		if(host != null){
			config.setHost(host);
		}
		Integer port = (Integer)param.get(PropName.port);
		if(port != null){
			config.setPort(port);
		}
		String password = (String)param.get(PropName.password);
		if(password != null){
			config.setPassword(password);
		}
		Integer database = (Integer)param.get(PropName.database);
		if(database != null){
			config.setDatabase(database);
		}
		Long timeout = (Long)param.get(PropName.timeout);
		if(timeout != null){
			config.setTimeout(timeout);
		}
	}
}

package gu.dtalk.redis;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.ImmutableMap.Builder;

import gu.simplemq.redis.JedisPoolLazy.PropName;

public enum RedisConfigType{
	/** 本机配置(仅用于测试) */LOCALHOST(new LocalhostRedisConfigProvider())
	/** 局域网配置 */,LAN(new LocalRedisConfigProvider())
	/** 公有云配置 */,CLOUD(new CloudRedisConfigProvider())
	/** 私有云配置 */,WAN;
	private final RedisConfigProvider defImpl;
	private RedisConfigProvider instance;
	private RedisConfigType(){
		this(null);
	}
	private RedisConfigType(RedisConfigProvider defImpl) {
		this.defImpl = defImpl;
	}
	private synchronized RedisConfigProvider findRedisConfigProvider(){
		if(instance == null && defImpl != instance){
			ServiceLoader<RedisConfigProvider> providers = ServiceLoader.load(RedisConfigProvider.class);
			Iterator<RedisConfigProvider> itor = providers.iterator();
			Optional<RedisConfigProvider> find = Iterators.tryFind(itor, new Predicate<RedisConfigProvider>() {

				@Override
				public boolean apply(RedisConfigProvider input) {
					return input.type() == RedisConfigType.this;
				}
			});
			instance =  find.isPresent() ? find.get() : this.defImpl;
		}
		return instance;
	}
	/**
	 *根据SPI加载的{@link RedisConfigProvider}实例提供的参数创建Redis连接参数
	 * @return
	 */
	public Map<PropName, Object> readRedisParam(){
		RedisConfigProvider config = findRedisConfigProvider();
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
	public void saveRedisParam(Map<PropName, Object> param){
		RedisConfigProvider config = findRedisConfigProvider();
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
package gu.dtalk.redis;

import static com.google.common.base.Preconditions.checkArgument;

import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

import org.slf4j.Logger;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.ImmutableMap.Builder;

import gu.dtalk.exception.DtalkException;
import gu.simplemq.redis.JedisPoolLazy;
import gu.simplemq.redis.JedisPoolLazy.PropName;
import gu.simplemq.redis.JedisUtils;

public enum RedisConfigType{
	/** 本机配置(仅用于测试) */LOCALHOST(new LocalhostRedisConfigProvider())
	/** 局域网配置 */,LAN(new LocalRedisConfigProvider())
	/** 公有云配置 */,CLOUD(new CloudRedisConfigProvider())
	/** 自定义配置 */,CUSTOM;
	private final RedisConfigProvider defImpl;
	private volatile RedisConfigProvider instance;
	private volatile Map<PropName, Object> parameters;
	private RedisConfigType(){
		this(null);
	}
	private RedisConfigType(RedisConfigProvider defImpl) {
		this.defImpl = defImpl;
	}
	private RedisConfigProvider findRedisConfigProvider(){
		// double checking
		if(instance == null){
			synchronized (this) {
				if(instance == null){
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
			}
		}
		return instance;
	}
	/**
	 * 根据SPI加载的{@link RedisConfigProvider}实例提供的参数创建Redis连接参数<b>
	 * 如果{@link #findRedisConfigProvider()}返回{@code null}则返回{@code null}
	 * @return
	 */
	public Map<PropName, Object> readRedisParam(){
		// double checking
		if(parameters == null){
			synchronized (this) {
				if(parameters == null){
					RedisConfigProvider config = findRedisConfigProvider();		
					
					if(config != null){
						Builder<PropName, Object> builder = ImmutableMap.builder();
						URI uri = config.getURI();
						if(uri != null){
							builder.put(PropName.uri, uri);
						}else{
							String host = config.getHost();
							int port =config.getPort();
							String password = config.getPassword();
							int database = config.getDatabase();
							checkArgument(!Strings.isNullOrEmpty(host),"INVALID REDIS HOST");

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
						}
						int timeout = config.getTimeout();
						if(timeout >0){
							builder.put(PropName.timeout, timeout);
						}
						parameters = builder.build();
					}
					
				}
			}
		}
		return parameters;
	}
	/**
	 * 保存redis参数到当前类型对应的{@link RedisConfigProvider}实例
	 * @param param
	 */
	public void saveRedisParam(Map<PropName, Object> param){
		RedisConfigProvider config = findRedisConfigProvider();
		Object host = param.get(PropName.host);
		if(host instanceof String){
			config.setHost((String) host);
		}
		Object port = param.get(PropName.port);
		if(port instanceof Integer){
			config.setPort((int) port);
		}
		Object password = param.get(PropName.password);
		if(password instanceof String){
			config.setPassword((String) password);
		}
		Object database = param.get(PropName.database);
		if(database instanceof Integer){
			config.setDatabase((int) database);
		}
		Object timeout = param.get(PropName.timeout);
		if(timeout instanceof Integer){
			config.setTimeout((int) timeout);
		}
		Object uri = param.get(PropName.uri);
		if(uri instanceof URI){
			config.setURI( (URI) uri);
		}
	}
	/**
	 * @param logger 日志对象,可为{@code null}
	 * @return
	 */
	public boolean testConnect(Logger logger){
		// 创建redis连接实例
		Map<PropName, Object> props = readRedisParam();
		if(props == null){
			return false;
		}
		if(logger != null){
			logger.info("try to connect {}...", this);
		}else{
			System.out.printf("try to connect %s...", this);
		}
		
		boolean connectable = JedisUtils.testConnect(JedisPoolLazy.initParameters(props));
		if(logger != null){
			logger.info(connectable?"OK":"FAIL");
		}else{
			System.out.println(connectable?"OK":"FAIL");
		}
		return connectable;
	}

	/**
	 * 按照如下优先顺序测试配置的redis连接，返回第一个能建立有效连接的配置，否则抛出异常<br>
	 * <li>{@link RedisConfigType#CUSTOM}</li>
	 * <li>{@link RedisConfigType#LAN}</li>
	 * <li>{@link RedisConfigType#CLOUD}</li>
	 * <li>{@link RedisConfigType#LOCALHOST}</li>
	 * @param logger 日志对象,可为{@code null}
	 * @return
	 * @throws DtalkException 没有找到有效redis连接
	 */
	public static RedisConfigType lookupRedisConnect(Logger logger) throws DtalkException{
		if(RedisConfigType.CUSTOM.testConnect(logger)){
			return RedisConfigType.CUSTOM;
		}else if(RedisConfigType.LAN.testConnect(logger)){
			return RedisConfigType.LAN;
		}else if(RedisConfigType.CLOUD.testConnect(logger)){
			return RedisConfigType.CLOUD;
		}else if(RedisConfigType.LOCALHOST.testConnect(logger)){
			return RedisConfigType.LOCALHOST;
		}
		throw new DtalkException("NOT FOUND VALID REDIS SERVER");
	}

	/**
	 * 与{@link #lookupRedisConnect(Logger)}功能相似,不同的时当没有找到有效redis连接时,不抛出异常,返回{@code null}
	 * @param logger
	 * @return 返回第一个能建立有效连接的配置,否则返回{@code null}
	 */
	public static RedisConfigType lookupRedisConnectUnchecked(Logger logger) {
		try {
			return lookupRedisConnect(logger);
		} catch (DtalkException e) {
			return null;
		}
	}
	@Override
	public	String toString(){
		StringBuffer buffer = new StringBuffer();
		buffer.append(name()).append(" : ");
		Map<PropName, Object> param = readRedisParam();
		if(param==null){
			buffer.append("null");
		}else{
			param = JedisPoolLazy.initParameters(param);
			buffer.append(JedisUtils.getCanonicalURI(param).toString());
		}
		return buffer.toString();
	}
}
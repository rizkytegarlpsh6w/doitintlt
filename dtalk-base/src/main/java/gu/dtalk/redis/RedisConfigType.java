package gu.dtalk.redis;

import static com.google.common.base.Preconditions.checkArgument;

import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.ImmutableMap.Builder;

import gu.dtalk.exception.DtalkException;
import gu.simplemq.redis.JedisPoolLazy.PropName;
import gu.simplemq.redis.JedisUtils;

/**
 * redis连接配置参数
 * @author guyadong
 *
 */
public enum RedisConfigType{
	/** 自定义配置 */CUSTOM
	/** 局域网配置 */,LAN(new DefaultLocalRedisConfigProvider())
	/** 公有云配置 */,CLOUD(new DefaultCloudRedisConfigProvider())
	/** 本机配置(仅用于测试) */,LOCALHOST(new DefaultLocalhostRedisConfigProvider());
	/**
	 * 接口实例的默认实现
	 */
	private final RedisConfigProvider defImpl;
	/**
	 * 接口实例
	 */
	private volatile RedisConfigProvider instance;
	/**
	 * redis连接配置参数
	 */
	private volatile Map<PropName, Object> parameters;
	/**
	 * 当前配置是否可连接
	 */
	private boolean connectable = false;
	private RedisConfigType(){
		this(null);
	}
	private RedisConfigType(RedisConfigProvider defImpl) {
		this.defImpl = defImpl;
	}
	/**
	 * SPI(Server Load Interface)方式加载当前类型的{@link RedisConfigProvider}实例,
	 * 没找到则用默认{@link #defImpl}实例代替
	 * @return
	 */
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
	public synchronized void saveRedisParam(Map<PropName, Object> param){
		RedisConfigProvider config = findRedisConfigProvider();
		Object host = param.get(PropName.host);
		if(host instanceof String){
			config.setHost((String) host);
		}
		Object port = param.get(PropName.port);
		if(port instanceof Number){
			config.setPort(((Number) port).intValue());
		}
		Object password = param.get(PropName.password);
		if(password instanceof String){
			config.setPassword((String) password);
		}
		Object database = param.get(PropName.database);
		if(database instanceof Number){
			config.setDatabase(((Number) database).intValue());
		}
		Object timeout = param.get(PropName.timeout);
		if(timeout instanceof Number){
			config.setTimeout(((Number) timeout).intValue());
		}
		Object uri = param.get(PropName.uri);
		if(uri instanceof URI){
			config.setURI( (URI) uri);
		}
	}
	/**
	 * 测试redis连接
	 * @return 连接成功返回{@code true},否则返回{@code false}
	 */
	public synchronized boolean testConnect(){
		Map<PropName, Object> props = readRedisParam();
		connectable = false;
		if(props != null){
//			System.out.printf("try to connect %s...\n", this);
			try{
				connectable = JedisUtils.testConnect(props);
			}catch (Exception e) {
			}
			System.out.printf("%s connect %s\n",this.toString(),connectable?"OK":"FAIL");
		}
		return connectable;
	}

	/**
	 * 按照如下优先顺序测试配置的redis连接，返回第一个能建立有效连接的配置，否则抛出异常<br>
	 * <li>{@link RedisConfigType#CUSTOM}</li>
	 * <li>{@link RedisConfigType#LAN}</li>
	 * <li>{@link RedisConfigType#CLOUD}</li>
	 * <li>{@link RedisConfigType#LOCALHOST}</li>
	 * @return
	 * @throws DtalkException 没有找到有效redis连接
	 */
	public static RedisConfigType lookupRedisConnect() throws DtalkException{
		// 并发执行连接测试，以减少等待时间
		Thread[] threads = new Thread[values().length];
		int index = 0;
		for (final RedisConfigType type : values()) {
			threads[index] = new Thread(){

				@Override
				public void run() {
					type.testConnect();
				}
				
			};
			threads[index].start();
			index++;
		}
		// 等待所有子线程结束
		try {
			for(Thread thread:threads){
				thread.join();
			}
		} catch (InterruptedException e) {
		}
		// 以枚举变量定义的顺序为优先级查找第一个connectable为true的对象返回
		// 都为false则抛出异常
		for (final RedisConfigType type : values()) {
			if(type.connectable){
				return type;
			}
		}
		throw new DtalkException("NOT FOUND VALID REDIS SERVER");
	}

	/**
	 * 与{@link #lookupRedisConnect()}功能相似,不同的时当没有找到有效redis连接时,不抛出异常,返回{@code null}
	 * @param logger
	 * @return 返回第一个能建立有效连接的配置,否则返回{@code null}
	 */
	public static RedisConfigType lookupRedisConnectUnchecked() {
		try {
			return lookupRedisConnect();
		} catch (DtalkException e) {
			return null;
		}
	}
	@Override
	public	String toString(){
		StringBuffer buffer = new StringBuffer();
		buffer.append(name());
		Map<PropName, Object> param = readRedisParam();
		if(param==null){
			buffer.append("(UNDEFINED)");
		}else{
			buffer.append("(").append(JedisUtils.getCanonicalURI(param).toString()).append(")");
		}
		return buffer.toString();
	}
}
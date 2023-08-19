package gu.dtalk.activemq;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.net.HostAndPort;

import gu.simplemq.IMQConnParameterSupplier;
import gu.simplemq.MessageQueueType;
import gu.simplemq.activemq.ActivemqConstants;
import gu.simplemq.activemq.PropertiesHelper;
import gu.simplemq.exceptions.SmqNotFoundConnectionException;
import gu.simplemq.utils.MQProperties;

/**
 * ActiveMQ 连接配置参数
 * @author guyadong
 *
 */
public enum ActivemqConfigType  implements IMQConnParameterSupplier,ActivemqConstants{
	/** 自定义配置 */CUSTOM(new DefaultCustomActivemqConfigProvider())
	/** 局域网配置 */,LAN(new DefaultLocalActivemqConfigProvider())
	/** 公有云配置 */,CLOUD(new DefaultCloudActivemqConfigProvider())
	/** 本机配置(仅用于测试) */,LOCALHOST(new DefaultLocalhostActivemqConfigProvider());
	/**
	 * 接口实例的默认实现
	 */
	private final ActivemqConfigProvider defImpl;
	/**
	 * 接口实例
	 */
	private volatile ActivemqConfigProvider instance;
	/**
	 * ActiveMQ 连接配置参数
	 */
	private volatile MQProperties parameters;
	/**
	 * 当前配置是否可连接
	 */
	private volatile boolean connectable = false;
	private ActivemqConfigType(){
		this(null);
	}
	private ActivemqConfigType(ActivemqConfigProvider defImpl) {
		this.defImpl = defImpl;
	}
	/**
	 * SPI(Server Load Interface)方式加载当前类型的{@link ActivemqConfigProvider}实例,
	 * 没找到则用默认{@link #defImpl}实例代替
	 * @return
	 */
	private ActivemqConfigProvider findConfigProvider(){
		// double checking
		if(instance == null){
			synchronized (this) {
				if(instance == null){
					ServiceLoader<ActivemqConfigProvider> providers = ServiceLoader.load(ActivemqConfigProvider.class);
					Iterator<ActivemqConfigProvider> itor = providers.iterator();
					Optional<ActivemqConfigProvider> find = Iterators.tryFind(itor, new Predicate<ActivemqConfigProvider>() {

						@Override
						public boolean apply(ActivemqConfigProvider input) {
							return input.type() == ActivemqConfigType.this;
						}
					});
					instance = find.isPresent() ? find.get() : this.defImpl;
					MQProperties props = instance.getProperties();
					String location = props.getLocationAsString();
					// 如果实例提供的参数中不包含最起始的连接地址参数则视为无效
					if(location == null){
						instance = null;
					}
				}
			}
		}
		return instance;
	}
	@Override
	public HostAndPort getHostAndPort(){		
		return parameters.getHostAndPort();
	}
	/**
	 * 根据SPI加载的{@link ActivemqConfigProvider}实例提供的参数创建activemq连接参数<br>
	 * 如果{@link #findConfigProvider()}返回{@code null}则返回{@code null}
	 * @return ActiveMQ 连接参数
	 */
	private MQProperties readParam(){
		// double checking
		if(parameters == null){
			synchronized (this) {
				if(parameters == null){
					ActivemqConfigProvider config = findConfigProvider();
					if(config != null){
						parameters = config.getProperties();
					}
				}
			}
		}
		return parameters;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Map<String, Object> getMQConnParameters(){
		MQProperties props = readParam();
		return (Map)props;
	}
	@Override
	public final MessageQueueType getImplType() {
		return MessageQueueType.ACTIVEMQ;
	}
	/**
	 * 保存 ActiveMQ 参数到当前类型对应的{@link ActivemqConfigProvider}实例
	 * @param param ActiveMQ 参数
	 */
	public synchronized void saveParam(Properties param){
		ActivemqConfigProvider config = findConfigProvider();
		config.setProperties(param);
	}
	/**
	 * 当前可用的的配置类型
	 */
	private static volatile ActivemqConfigType activeConfigType = null;
	/**
	 * 复位{@link #activeConfigType}为{@code null}<br>
	 * 为避免{@link #lookupConnect(Integer)}方法被多次执行，
	 * 当{@link #activeConfigType}不为{@code null}时直接返回{@link #activeConfigType}的值，
	 * 如果希望再次执行{@link #lookupConnect(Integer)}方法，可先调用此方法设置{@link #activeConfigType}为{@code null}
	 */
	public static void resetActiveConfigType(){
		activeConfigType = null;
	}
	/**
	 * 测试 ActiveMQ 连接
	 * @param timeoutMills 连接超时(毫秒),为{@code null}或小于0使用默认值
	 * @return 连接成功返回{@code true},否则返回{@code false}
	 */
	public synchronized boolean testConnect(Integer timeoutMills){
		Properties props = readParam();
		connectable = false;
		if(props != null && !props.stringPropertyNames().isEmpty()){
//			System.out.printf("try to connect %s...\n", this);
			try{
				if(timeoutMills != null && timeoutMills > 0){
					props.setProperty(ACON_connectResponseTimeout, timeoutMills.toString());
				}
				connectable = PropertiesHelper.AHELPER.testConnect(props);
			}catch (Exception e) {
			}
//			if(connectable){
//				System.out.println(toString() + " connect OK\n");
//			}
//			System.out.printf("%s connect %s\n",this.toString(),connectable?"OK":"FAIL");
		}
		return connectable;
	}

	/**
	 * 按照如下优先顺序测试配置的 ActiveMQ 连接，返回第一个能建立有效连接的配置，否则抛出异常<br>
	 * <ul>
	 * <li>{@link ActivemqConfigType#CUSTOM}</li>
	 * <li>{@link ActivemqConfigType#LAN}</li>
	 * <li>{@link ActivemqConfigType#CLOUD}</li>
	 * <li>{@link ActivemqConfigType#LOCALHOST}</li>
	 * </ul>
	 * @param timeoutMills 连接超时(毫秒),为{@code null}或小于0使用默认值
	 * @return {@link #activeConfigType}不为{@code null}时直接返回{@link #activeConfigType}的值
	 * @throws SmqNotFoundConnectionException 没有找到有效 ActiveMQ 连接
	 */
	public static ActivemqConfigType lookupConnect(final Integer timeoutMills) throws SmqNotFoundConnectionException{
		if(activeConfigType == null){
			synchronized(ActivemqConfigType.class){
				if(activeConfigType == null){
					// 并发执行连接测试，以减少等待时间
					Thread[] threads = new Thread[values().length];
					int index = 0;
					for (final ActivemqConfigType type : values()) {
						threads[index] = new Thread(){

							@Override
							public void run() {
								type.testConnect(timeoutMills);
							}

						};
						threads[index].start();
						index++;
					}
					// 等待所有子线程结束
					// 以枚举变量定义的顺序为优先级查找第一个connectable为true的对象返回
					// 都为false则抛出异常
					try {
						for(int i =0;i<threads.length;++i){
							threads[i].join();
							ActivemqConfigType type = values()[i];
							if(type.connectable){
								return type;
							}
						}
					} catch (InterruptedException e) {
					}
					throw new SmqNotFoundConnectionException("NOT FOUND VALID ACTIVEMQ SERVER");
				}
			}
		}
		return activeConfigType;
	}

	/**
	 * 与{@link #lookupConnect(Integer)}功能相似,不同的时当没有找到有效 ActiveMQ 连接时,不抛出异常,返回{@code null}
	 * @param timeoutMills 连接超时(毫秒),为{@code null}或小于0使用默认值
	 * @return 返回第一个能建立有效连接的配置,否则返回{@code null}
	 */
	public static ActivemqConfigType lookupConnectUnchecked(Integer timeoutMills) {
		try {
			return lookupConnect(timeoutMills);
		} catch (SmqNotFoundConnectionException e) {
			return null;
		}
	}
	@Override
	public	String toString(){
		StringBuffer buffer = new StringBuffer();
		buffer.append(getClass().getSimpleName()).append(":");
		buffer.append(name());
		Properties param = readParam();
		if(param==null){
			buffer.append("(UNDEFINED)");
		}else{
			buffer.append("(").append(PropertiesHelper.AHELPER.getLocationlURI(param).toString()).append(")");
		}
		return buffer.toString();
	}
}
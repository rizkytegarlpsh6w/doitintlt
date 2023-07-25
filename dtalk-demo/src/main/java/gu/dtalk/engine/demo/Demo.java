package gu.dtalk.engine.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gu.dtalk.engine.DeviceUtils;
import gu.dtalk.engine.ItemAdapter;
import gu.dtalk.engine.ItemEngineRedisImpl;
import gu.dtalk.engine.SampleConnector;
import gu.dtalk.engine.demo.DemoListener;
import gu.dtalk.engine.demo.DemoMenu;
import gu.dtalk.redis.DefaultCustomRedisConfigProvider;
import gu.simplemq.ISubscriber;
import gu.simplemq.MessageQueueConfigManagers;
import gu.simplemq.MessageQueueFactorys;
import gu.simplemq.MessageQueueType;
import gu.simplemq.exceptions.SmqNotFoundConnectionException;
import gu.simplemq.Channel;
import gu.simplemq.IMQConnParameterSupplier;
import gu.simplemq.IMessageQueueConfigManager;
import gu.simplemq.IMessageQueueFactory;
import gu.simplemq.IPublisher;
import net.gdface.utils.FaceUtilits;
import net.gdface.utils.NetworkUtil;

import static gu.dtalk.CommonUtils.*;
import static gu.dtalk.engine.demo.DemoConfig.DEMO_CONFIG;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * dtalk引擎演示基类
 * @author guyadong
 *
 */
public class Demo {
	protected static final Logger logger = LoggerFactory.getLogger(Demo.class);

	private final SampleConnector connAdapter;
	private final ISubscriber subscriber;
	private final byte[] devMac;
	public Demo(IMessageQueueConfigManager manager) throws SmqNotFoundConnectionException {
		// 创建消息系统连接实例
		IMQConnParameterSupplier config = checkNotNull(manager,"manager is null").lookupMessageQueueConnect(null);
		IMessageQueueFactory factory = MessageQueueFactorys.getFactory(config.getImplType())
				.init(config.getMQConnParameters())
				.asDefaultFactory();
		logger.info("use config={}",config);

		subscriber = factory.getSubscriber();	
		IPublisher publisher = factory.getPublisher();
		DemoMenu root = new DemoMenu(config).init().register(DemoListener.INSTANCE);
		devMac = DeviceUtils.DEVINFO_PROVIDER.getMac();
		connAdapter = new SampleConnector(publisher,subscriber)
				.setSelfMac(FaceUtilits.toHex(devMac))
				.setItemAdapter((ItemAdapter) new ItemEngineRedisImpl(publisher).setRoot(root));
	}
	/**
	 * 启动连接
	 */
	protected void start(){
		System.out.printf("DEVICE MAC address(设备地址): %s\n",NetworkUtil.formatMac(devMac, ":"));
		String connchname = getConnChannel(devMac);
		Channel<String> connch = new Channel<>(connchname, String.class)
				.setAdapter(connAdapter);
		subscriber.register(connch);
		System.out.printf("Connect channel registered(连接频道注册) : %s \n",connchname);
	}
	protected static void waitquit(){
		System.out.println("PRESS 'quit' OR 'CTRL-C' to exit");
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in)); 
		try{
			while(!"quit".equalsIgnoreCase(reader.readLine())){				
			}
			System.exit(0);
		} catch (IOException e) {
	
		}finally {
	
		}
	}
	public static void main(String []args){		
		try{
			DEMO_CONFIG.parseCommandLine(args);
			MessageQueueType implType = DEMO_CONFIG.getImplType();
			switch (implType) {
			case REDIS:
				DefaultCustomRedisConfigProvider.initredisParameters(DEMO_CONFIG.getRedisParameters());
				break;
			default:
				throw new UnsupportedOperationException("UNSUPPORTED Message queue type:" + implType);
			}
	
			System.out.printf("Device talk %s Demo starting(设备%s模拟器启动)",implType,implType);
			new Demo(MessageQueueConfigManagers.getManager(implType)).start();
			waitquit();
		}catch (Exception e) {
			if(DEMO_CONFIG.isTrace()){
				e.printStackTrace();
			}else{
				System.out.println(e.getMessage());
			}
		}
	}

}

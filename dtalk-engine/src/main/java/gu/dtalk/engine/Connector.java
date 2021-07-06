package gu.dtalk.engine;

import java.util.Arrays;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.Timer;
import java.util.TimerTask;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.base.Strings;

import gu.dtalk.Ack;
import gu.dtalk.ConnectReq;
import gu.dtalk.DeviceInfoProvider;
import gu.simplemq.Channel;
import gu.simplemq.IMessageAdapter;
import gu.simplemq.exceptions.SmqUnsubscribeException;
import gu.simplemq.redis.JedisPoolLazy;
import gu.simplemq.redis.RedisFactory;
import gu.simplemq.redis.RedisPublisher;
import gu.simplemq.redis.RedisSubscriber;
import net.gdface.utils.FaceUtilits;
import static gu.dtalk.CommonConstant.*;
import static com.google.common.base.Preconditions.*;

/**
 * 管理端连接控制器
 * @author guyadong
 *
 */
public class Connector implements IMessageAdapter<ConnectReq> {
	private static class SingletonTimer{
		private static final Timer instnace = new Timer(true);
	}
	private Timer timer;
	private Timer getTimer(){
		if(timer == null){
			timer = SingletonTimer.instnace;
		}
		return timer;
	}
	
	private ConnectReq curconnect;
	private final RedisPublisher ackPublisher;
	private String workChannel;
	private long idleTimeLimit = DEFAULT_IDLE_TIME_MILLS;
	private long timerPeriod = 2000;
	private ItemAdapter itemAdapter;
	private final RedisSubscriber subscriber;
	private final static DeviceInfoProvider DEVINFO_PROVIDER = getDeviceInfoProvider();
	/**
	 * SPI(Service Provider Interface)机制加载 {@link DeviceInfoProvider}实例,没有找到返回默认实例
	 * @return
	 */
	private static DeviceInfoProvider getDeviceInfoProvider() {		
		ServiceLoader<DeviceInfoProvider> providers = ServiceLoader.load(DeviceInfoProvider.class);
		Iterator<DeviceInfoProvider> itor = providers.iterator();
		if(!itor.hasNext()){
			return DefaultDevInfoProvider.INSTANCE;
		}
		return itor.next();
	}
	
	public Connector(JedisPoolLazy pool) {
		ackPublisher = RedisFactory.getPublisher(pool);
		subscriber = RedisFactory.getSubscriber(pool);
		
		// 定时检查itemAdapter工作状态，当itemAdapter 空闲超时，则中止频道
		getTimer().schedule(new TimerTask() {
			
			@Override
			public void run() {
				Channel<?> c = subscriber.getChannel(workChannel);
				if(null != c){
					ItemAdapter adapter = (ItemAdapter) c.getAdapter();
					long lasthit = adapter.getLastHit();
					if(System.currentTimeMillis() - lasthit > idleTimeLimit){
						subscriber.unregister(workChannel);
						workChannel = null;
					}
				}
			}
		}, 0, timerPeriod);

	}
	/** 
	 * 处理来自管理端的连接请求<br>
	 * 如果连接密码不匹配或其他管理端已经连接返回错误信息,否则返回随机生成的管理操作频道名。
	 * @param req 连接请求
	 */
	@Override
	public void onSubscribe(ConnectReq req) throws SmqUnsubscribeException {
		Ack<String> ack = new Ack<String>().setStatus(Ack.Status.OK);
		try{
			String admPwd = DEVINFO_PROVIDER.getPassword();
			checkArgument(!Strings.isNullOrEmpty(req.pwd),"NULL REQUEST PASSWORD");
			checkArgument(!Strings.isNullOrEmpty(admPwd),"NULL ADMIN PASSWORD");
			byte[] pwdmd5 = FaceUtilits.getMD5(admPwd.getBytes());

			checkState(Arrays.equals(req.pwd.toLowerCase().getBytes(), pwdmd5),"INVALID PASSWORD");
			checkState(curconnect ==null || curconnect.mac.equals(req.mac),"ANOTHER CLIENT LOCKED");
			// 密码匹配则发送工作频道名
			if(workChannel == null){
				workChannel = String.format("%s_%d", 
						FaceUtilits.toHex(DEVINFO_PROVIDER.getMac()),
						System.currentTimeMillis()&0xffff);
			}

			ack.setValue(workChannel);
			if(null == subscriber.getChannel(workChannel)){
				Channel<JSONObject> c = new Channel<JSONObject> (workChannel,JSONObject.class,itemAdapter);
				subscriber.register(c);
				System.currentTimeMillis();
			}
		}catch(Exception e){
			ack.setStatus(Ack.Status.ERROR).setErrorMessage(e.getMessage());
		}
		// 向响应频道发送响应消息
		String channelName = req.mac + ACK_SUFFIX;
		Channel<Ack<String>> channel = new Channel<Ack<String>>(
				channelName,
				new TypeReference<Ack<String>>() {}.getType());
		ackPublisher.publish(channel, ack);
	}

	public Connector setItemAdapter(ItemAdapter adapter) {
		this.itemAdapter = adapter;
		return this;
	}
	public Connector setTimer(Timer timer) {
		if(timer != null){
			this.timer = timer;
		}
		return this;
	}

	/**
	 * 空闲时间限制(毫秒),超过此时间,自动中断连接
	 * @param idleTimeLimit
	 * @return
	 */
	public Connector setIdleTimeLimit(long idleTimeLimit) {
		if(idleTimeLimit >0){
			this.idleTimeLimit = idleTimeLimit;
		}
		return this;
	}

	public Connector setTimerPeriod(long timerPeriod) {
		if(timerPeriod > 0){
		this.timerPeriod = timerPeriod;
		}
		return this;
	}

}

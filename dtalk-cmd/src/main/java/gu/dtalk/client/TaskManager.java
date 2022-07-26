package gu.dtalk.client;

import com.google.common.base.Supplier;

import gu.dtalk.DeviceInstruction;
import gu.simplemq.Channel;
import gu.simplemq.redis.JedisPoolLazy;
import gu.simplemq.redis.RedisConsumer;
import gu.simplemq.redis.RedisFactory;
import gu.simplemq.redis.RedisProducer;

/**
 * 
 * (任务队列)设备命令发送管理模块<br>
 * @author guyadong
 *
 */
public class TaskManager extends BaseCmdManager {
    private final RedisProducer producer;
	private final FreshedChannelSupplier<DeviceInstruction> channelSupplier;
    /**
     * 构造方法
     * @param poolLazy 
     * @param taskQueueSupplier
     */
    public TaskManager(JedisPoolLazy poolLazy, Supplier<String> taskQueueSupplier) {
    	super(poolLazy);
        this.producer = RedisFactory.getProducer(poolLazy);
        this.channelSupplier = new FreshedChannelSupplier<DeviceInstruction>(taskQueueSupplier);
    }
    
    /**
     * 发送设备命令
     * @param cmd
     * @return 收到命令的客户端数目
     */
    @Override
    protected long doSendCmd(DeviceInstruction cmd){
    	Channel<DeviceInstruction> channel = channelSupplier.get();
    	int numSub = RedisConsumer.countOf(channel.name);
    	if(numSub >0){    		
    		producer.produce(channel, cmd);
    		return 1;
    	}
    	return 0;
    }
}

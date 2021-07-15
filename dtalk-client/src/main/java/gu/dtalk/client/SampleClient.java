package gu.dtalk.client;

import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

import gu.dtalk.Ack;
import gu.dtalk.ConnectReq;
import gu.simplemq.Channel;
import gu.simplemq.IUnregistedListener;
import gu.simplemq.redis.JedisPoolLazy;
import gu.simplemq.redis.RedisConsumer;
import gu.simplemq.redis.RedisFactory;
import gu.simplemq.redis.RedisPublisher;
import gu.simplemq.redis.JedisPoolLazy.PropName;
import net.gdface.utils.FaceUtilits;
import net.gdface.utils.NetworkUtil;

import static gu.dtalk.CommonConstant.*;
import static gu.dtalk.CommonUtils.*;

public class SampleClient {
	/** redis 连接参数 */
	final static Map<PropName, Object> redisParam = 
			ImmutableMap.<PropName, Object>of(
					/** redis 主机名 */PropName.host,REDIS_HOST,
					/** redis 端口号 */PropName.port,REDIS_PORT
					);
	private String reqChannel;
	final RedisConsumer consumer;
	final RedisPublisher publisher;
	final byte[] mymac;
	private final String ackchname;
	public SampleClient() {
		// 根据连接参数创建默认实例 
		JedisPoolLazy.createDefaultInstance( redisParam);
		consumer = RedisFactory.getConsumer(JedisPoolLazy.getDefaultInstance());
		publisher = RedisFactory.getPublisher(JedisPoolLazy.getDefaultInstance());
		final ConnectorAdapter connectorAdapter = new ConnectorAdapter();
		try {
			mymac = NetworkUtil.getCurrentMac(REDIS_HOST, REDIS_PORT);
			ackchname = getAckChannel(mymac);
			final Channel<Ack<String>> ackChannel = new Channel<Ack<String>>(
					ackchname, 
					new TypeReference<Ack<String>>(){}.getType())
					.setAdapter(connectorAdapter);
			// 连接成功时根据返回的
			ackChannel.addUnregistedListener(new IUnregistedListener<Ack<String>>() {

				@Override
				public void apply(Channel<Ack<String>> channel) {
					reqChannel = connectorAdapter.getReqChannel();
					if(!Strings.isNullOrEmpty(reqChannel)){
						Channel<JSONObject> c = new Channel<JSONObject>(ackchname,JSONObject.class)
								.setAdapter(new TextRenderEngine());	
						consumer.register(c);
					}

				}
			});
			consumer.register(ackChannel);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}		
	}
	public void connect() {
		final ConnectorAdapter connectorAdapter = new ConnectorAdapter();
		final Channel<Ack<String>> ackChannel = new Channel<Ack<String>>(
				ackchname, 
				new TypeReference<Ack<String>>(){}.getType())
				.setAdapter(connectorAdapter);
		// 连接成功时根据返回的
		ackChannel.addUnregistedListener(new IUnregistedListener<Ack<String>>() {

			@Override
			public void apply(Channel<Ack<String>> channel) {
				reqChannel = connectorAdapter.getReqChannel();
				if(!Strings.isNullOrEmpty(reqChannel)){
					Channel<JSONObject> c = new Channel<JSONObject>(ackchname,JSONObject.class)
							.setAdapter(new TextRenderEngine());	
					consumer.register(c);
				}
			}
		});
		consumer.register(ackChannel);		
	}
	public String getReqChannel() {
		return reqChannel;
	}
	public void scankey(){
        ConnectReq req = new ConnectReq();
        req.mac = FaceUtilits.toHex(mymac);
        Scanner sc = new Scanner(System.in);
        while (sc.hasNext()) {
            String str = sc.next();
            System.out.println(str);
        }		
	}
}

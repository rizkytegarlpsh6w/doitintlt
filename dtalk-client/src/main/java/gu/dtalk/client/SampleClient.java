package gu.dtalk.client;

import java.io.IOException;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.base.MoreObjects;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

import gu.dtalk.Ack;
import gu.dtalk.BaseOption;
import gu.dtalk.CmdItem;
import gu.dtalk.ConnectReq;
import gu.dtalk.MenuItem;
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
	private String reqChannel = null;
	final RedisConsumer consumer;
	final RedisPublisher publisher;
	final byte[] mymac;
	private final String ackchname;
	private final String connchname;
	private final TextRenderEngine renderEngine = new TextRenderEngine();
	private Channel<Object> reqCh = new Channel<Object>(reqChannel, Object.class);
	private Channel<MenuItem> menuCh = new Channel<MenuItem>(reqChannel, MenuItem.class);
	private Channel<CmdItem> cmdCh = new Channel<CmdItem>(reqChannel, CmdItem.class);
	private Channel<BaseOption<Object>> optCh = new Channel<BaseOption<Object>>(reqChannel, 
			new TypeReference<BaseOption<Object>>(){}.getType());
	public SampleClient(String devmac) {
		// 根据连接参数创建默认实例 
		JedisPoolLazy.createDefaultInstance( redisParam);
		consumer = RedisFactory.getConsumer(JedisPoolLazy.getDefaultInstance());
		publisher = RedisFactory.getPublisher(JedisPoolLazy.getDefaultInstance());
		try {
			mymac = NetworkUtil.getCurrentMac(REDIS_HOST, REDIS_PORT);
			ackchname = getAckChannel(mymac);
			connchname = getConnChannel(devmac);
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
							.setAdapter(renderEngine.reset());	
					consumer.register(c);
				}
			}
		});
		consumer.register(ackChannel);		
		
	}
	public void scanLine(Predicate<String>consumer){
        ConnectReq req = new ConnectReq();
        req.mac = FaceUtilits.toHex(mymac);
        Scanner scaner = new Scanner(System.in);
        try{
        	while (scaner.hasNextLine()) {
        		consumer.apply(scaner.next());
        	}
        }finally {
			scaner.close();
		}
	}
	public static boolean scan4Mac(AtomicReference<String>output){
		System.out.println("Input MAC address of Device,such as '00:00:7f:2a:39:4A' or '00e8992730FF':");

        Scanner scaner = new Scanner(System.in);
        try{
        	while (scaner.hasNextLine()) {
        		String str = scaner.next();
        		if(str.isEmpty()){
        			return false;
        		}
        		String mac = parseMac(str);
        		if(!mac.isEmpty()){
        			output.set(mac);
        			return true;
        		}
        		System.out.println("ERROR:Invalid mac adress");
        	}
        }finally {
			scaner.close();
		}
        return false;
	}
	public boolean scan4Pwd(){
		System.out.println("Input password  of Device,default password is last 4 character of device MAC address(lowercase):");
		ConnectReq req = new ConnectReq();
		req.mac = FaceUtilits.toHex(mymac);
		Channel<ConnectReq> reqch = new Channel<>(connchname, ConnectReq.class);
        Scanner scaner = new Scanner(System.in);
        try{
        	while (reqChannel == null &&scaner.hasNextLine()) {
        		req.pwd = scaner.next();
        		if(req.pwd.isEmpty()){
        			return false;
        		}
        		publisher.publish(reqch, req);
        		waitResp(System.currentTimeMillis());
        	}
        	return true;
        }finally {
			scaner.close();
		}
	}
	private void waitResp(long timestamp){
		int waitCount = 30;
		while(renderEngine.getLastResp()< timestamp && waitCount > 0){
			try {
				Thread.sleep(100);
				waitCount --;
			} catch (InterruptedException e) {
				System.exit(-1);
			}
		}
		if(waitCount ==0){
			System.out.println("TIMEOUT for response");
			System.exit(-1);
		}
	}
	private void selectMenu(String path){
		MenuItem menuItem = new MenuItem();
		menuItem.setPath(path);
		publisher.publish(reqCh, menuItem);
		waitResp(System.currentTimeMillis());
	}
	public boolean scan4Cmd(){
		selectMenu("/");
	    Scanner scaner = new Scanner(System.in);
	    try{
	    	while (scaner.hasNextLine()) {
	    		//String str = scaner.next();
	    		
	    		return true;
	    	}
	    }finally {
			scaner.close();
		}
	    return false;
	}
	private static String parseMac(String input){
		input = MoreObjects.firstNonNull(input, "").trim();
		if(input.matches(MAC_REG)){
			return input.replace(":", "").toLowerCase();
		}
		return "";
	}
	public static void main(String []args){
		String mac;
		if(args.length > 1){
			mac = parseMac(args[0]);
			if(mac.isEmpty()){
				System.out.printf("ERROR:Invalid mac adress %s\n",mac);
				return ;
			}				
		}
		AtomicReference<String> macRef = new AtomicReference<String>();
		if(!scan4Mac(macRef)){
			return ;
		}
		try{
			SampleClient client = new SampleClient(macRef.get());
			client.connect();
			if(!client.scan4Pwd()){
				return;
			}
		}catch (Exception e) {
			System.out.println(e.getMessage());
			return ;
		}
	}
}

package gu.dtalk.client;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.MoreObjects;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.net.HostAndPort;

import gu.dtalk.CmdItem;
import gu.dtalk.BaseItem;
import gu.dtalk.BaseOption;
import gu.dtalk.ItemType;
import gu.dtalk.redis.RedisConfigType;
import gu.dtalk.Ack;
import gu.dtalk.Ack.Status;
import gu.simplemq.Channel;
import gu.simplemq.redis.JedisPoolLazy;
import gu.simplemq.redis.RedisFactory;
import gu.simplemq.redis.RedisPublisher;
import gu.simplemq.redis.RedisSubscriber;
import net.gdface.utils.FaceUtilits;
import net.gdface.utils.NetworkUtil;
import static gu.dtalk.CommonConstant.*;
import static gu.dtalk.CommonUtils.*;
import static com.google.common.base.Preconditions.*;

public abstract class BaseConsole {
	protected static final Logger logger = LoggerFactory.getLogger(BaseConsole.class);
	protected String reqChannel = null;
	final RedisSubscriber subscriber;
	final RedisPublisher publisher;
	protected final byte[] temminalMac;
	private final String ackchname;
	protected final String connchname;
	private final RenderEngine renderEngine = new RenderEngine();
	private final Channel<JSONObject> ackChannel;
	/**
	 * 构造方法
	 * @param devmac 要连接的设备MAC地址,测试设备程序在本地运行时可为空。
	 * @param config TODO
	 */
	public BaseConsole(String devmac, RedisConfigType config) {
		JedisPoolLazy pool = JedisPoolLazy.getInstance(config.readRedisParam(),false);
		subscriber = RedisFactory.getSubscriber(pool);
		publisher = RedisFactory.getPublisher(pool);
		temminalMac = getSelfMac(config);
		System.out.printf("TERMINAL MAC address: %s\n", NetworkUtil.formatMac(temminalMac, ":"));

		ackchname = getAckChannel(temminalMac);
		ConnectorAdapter msgAdapter = new ConnectorAdapter().setOnValidPwd(new Predicate<String>() {

			@Override
			public boolean apply(String input) {
				reqChannel = input;
				ackChannel.setAdapter(renderEngine);
				return false;
			}
		});		
		ackChannel = new Channel<JSONObject>(	ackchname,	JSONObject.class).setAdapter(msgAdapter);

		if(Strings.isNullOrEmpty(devmac)){
			// 使用本地地址做为设备MAC地址
			devmac = FaceUtilits.toHex(temminalMac);
			System.out.println("use local MAC for target DEVICE");
		}
		System.out.printf("DEVICE MAC address: %s\n", devmac);

		connchname = getConnChannel(devmac);
		Channel<String> testch = new Channel<String>(connchname, String.class);
		long rc = publisher.publish(testch, "\"hello\"");
		checkState(rc != 0,"TARGET DEVICE NOT online");
		if(rc>1){
			System.out.println("DUPLIDATED TARGET DEVICE WITH same MAC address");
		}		

	}
	protected static byte[] getSelfMac(RedisConfigType type){
		try {
			HostAndPort hostAndPort = type.getHostAndPort();
			String host = hostAndPort.getHost();
			int port = hostAndPort.getPort();
			// 使用localhost获取本机MAC地址会返回空数组，所以这里使用一个互联地址来获取
			if(host.equals("127.0.0.1") || host.equalsIgnoreCase("localhost")){
				return NetworkUtil.getCurrentMac("www.cnnic.net.cn", 80);
			}
			return NetworkUtil.getCurrentMac(host, port);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * 尝试连接目标设备
	 */
	public void connect() {		

		subscriber.register(ackChannel);		
		
	}
	protected static String scanLine(Predicate<String>validate){
		Scanner scaner = new Scanner(System.in);
		try{
			
			return scanLine(validate,scaner);
		}finally {
			//scaner.close();
		}
	}
	private static String scanLine(Predicate<String>validate,Scanner scaner){
		scaner.reset();
		scaner.useDelimiter("\r?\n");		
		while (scaner.hasNextLine()) {
			String str = scaner.next();
			if(str.isEmpty()){
				return "";
			}
			try{
				if(validate.apply(str)){
					return str;
				}
			}catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		return "";
	}

	/**
	 * 输入目标设备的MAC地址
	 * @return
	 */
	protected static String inputMac(){
		System.out.println("Input MAC address of Device,such as '00:00:7f:2a:39:4A' or '00e8992730FF':"
			+ "(input empty string if target device demo running on localhost)"
				);
		return scanLine(new Predicate<String>() {
			@Override
			public boolean apply(String input) {
				String mac = parseMac(input);
				if(!mac.isEmpty()){
					return true;
				}
				System.out.println("ERROR:Invalid mac adress");
				return false;
			}
		});

	}
	private void waitResp(long timestamp){
		int waitCount = 30;
		TextMessageAdapter<?> adapter = (TextMessageAdapter<?>) ackChannel.getAdapter();
		while(adapter.getLastResp() < timestamp && waitCount > 0){
			try {
				Thread.sleep(100);
				waitCount --;
			} catch (InterruptedException e) {
				System.exit(-1);
			}
		}
		if(waitCount ==0 ){
			System.out.println("TIMEOUT for response");
			System.exit(-1);
		}
	}
	private JSONObject makeItemJSON(String path){
		checkArgument(!Strings.isNullOrEmpty(path));
		JSONObject json = new JSONObject();
		if(path.equals("/")){
			json.fluentPut(ITEM_FIELD_PATH, path)
				.fluentPut(ITEM_FIELD_CATALOG, ItemType.MENU);			
		}else{
			BaseItem currentLevel = checkNotNull(renderEngine.getCurrentLevel(),"currentLevel is null");
			// 如果没有根据path找到对应的item则抛出异常
			BaseItem item = checkNotNull(currentLevel.find(path),"NOT FOUND item %s",path);
			json.fluentPut(ITEM_FIELD_NAME, item.getName())
				.fluentPut(ITEM_FIELD_PATH,path)
				.fluentPut(ITEM_FIELD_CATALOG, item.getCatalog());
			if(item instanceof BaseOption<?>){
				json.put(OPTION_FIELD_TYPE, ((BaseOption<?>)item).getType());
			}
		}

		return json;
	}
	protected <T>boolean syncPublish(Channel<T>channel,T json){
		try{
			long timestamp = System.currentTimeMillis();
			long rc = publisher.publish(channel, json);
			// 没有接收端则抛出异常
			checkState(rc != 0,"target device DISCONNECT");
			waitResp(timestamp);
			return true;
		}catch(Exception e){
			System.out.println(e.getMessage());
			System.exit(0);
		}
		return false;
	}
	private boolean syncPublishReq(Object json){
		Channel<Object> reqCh = new Channel<Object>(checkNotNull(reqChannel), Object.class);
		return syncPublish(reqCh, json);
	}

	/**
	 * 接受键盘输入选项内容
	 * @param scaner
	 * @param json
	 * @return 输入不为空返回true，否则返回false
	 */
	private boolean inputOption(Scanner scaner,final JSONObject json){
		checkArgument(json !=null && ItemType.OPTION == json.getObject(ITEM_FIELD_CATALOG, ItemType.class));
		BaseItem item = renderEngine.getCurrentLevel().getChildByPath(json.getString(ITEM_FIELD_PATH));
		checkArgument(item instanceof BaseOption<?>);
		BaseOption<?> option = (BaseOption<?>)item;
		String desc = Strings.isNullOrEmpty(option.getDescription()) ? "" : "("+option.getDescription()+")"; 
		System.out.printf("INPUT VALUE for %s%s:", option.getUiName(),desc);
		String value = scanLine(new Predicate<String>() {

			@Override
			public boolean apply(String input) {
				if(isImage(json)){
					try {
						json.fluentPut(OPTION_FIELD_VALUE, FaceUtilits.getByteBufferNotEmpty(new File(input)));
					} catch (Exception e) {
						Throwables.throwIfUnchecked(e);
						throw new RuntimeException(e);
					}
				}else{
					json.fluentPut(OPTION_FIELD_VALUE, input);
				}
				return true;
			}
		}, scaner);
		return !value.isEmpty();
	}
	private boolean inputCmd(Scanner scaner,JSONObject json){
		checkArgument(json !=null && ItemType.CMD == json.getObject(ITEM_FIELD_CATALOG, ItemType.class));
		BaseItem item = renderEngine.getCurrentLevel().getChildByPath(json.getString(ITEM_FIELD_PATH));
		checkArgument(item instanceof CmdItem);
		CmdItem cmd = (CmdItem)item;
		for(BaseOption<?> param:cmd.getParameters()){
			JSONObject optjson = makeItemJSON(param.getPath());
			while(inputOption(scaner,optjson)){
				if(syncPublishReq(optjson)){
					checkState(isAck(renderEngine.getLastRespObj()));
					Status status = ((JSONObject)renderEngine.getLastRespObj()).getObject(ACK_FIELD_STATUS, Status.class);
					if(status != Status.OK){
						// 参数值无效，继续提示输入
						continue;
					}
					break;
				}
				return false;
			}
			//  继续下一个参数
		}		
		return true;
	}
	/**
	 * 键盘命令交互
	 */
	protected void cmdInteractive(){
		
		// 第一次进入发送命令显示根菜单
		if(!syncPublishReq(makeItemJSON("/"))){
			return ;
		}
	    Scanner scaner = new Scanner(System.in);
	    try{
	    	while (scaner.hasNextLine()) {
	    		String str = scaner.next();
	    		if(str.isEmpty()){
	    			continue;
	    		}
	    		try{
	    			JSONObject json = makeItemJSON(str);
	    			switch (json.getObject(ITEM_FIELD_CATALOG,ItemType.class)) {
	    			case MENU:
	    				// 进入菜单 
	    				syncPublishReq(json);
	    				break;
	    			case OPTION:{
	    				Ack<?> ack=null;
	    				// 修改参数
	    				do{
	    					if(inputOption(scaner,json)){
	    						syncPublishReq(json);
	    					}else{
	    						// 输入空行则返回
	    						break;
	    					}
	    					// 获取响应消息内容,如果输入响应错误则提示继续
	    					ack = renderEngine.getLastAck();
	    					
	    				}while(ack != null && !Status.OK.equals(ack.getStatus()));
	    	    		// 刷新当前菜单
	    				syncPublishReq(makeItemJSON(renderEngine.getCurrentLevel().getPath()));
	    				break;
	    			}
					case CMD:{
						Ack<?> ack = null;
						do{
							// 执行命令
							if(inputCmd(scaner,json)){
								syncPublishReq(json);
							}else{
								// 输入空行则返回
								break;
							}
							if(isQuit(json)){
								return;
							}
							// 获取响应消息内容,如果输入响应错误则提示继续
	    					ack = renderEngine.getLastAck();
	    					
						}while(ack != null && !Status.OK.equals(ack.getStatus()));
						break;
					}
					default:
						break;
					}
	    		}catch (Exception e) {
	    			System.out.println(e.getMessage());
				}
	    	}
	    }finally {
			scaner.close();
		}
	    return;
	}
	protected static String parseMac(String input){
		input = MoreObjects.firstNonNull(input, "").trim();
		if(input.matches(MAC_REG)){
			return input.replace(":", "").toLowerCase();
		}
		return "";
	}
	protected void waitTextRenderEngine(){
		int waitCount = 30;
		TextMessageAdapter<?> adapter = (TextMessageAdapter<?>) ackChannel.getAdapter();
		while( !(adapter instanceof RenderEngine) && waitCount > 0){
			try {
				Thread.sleep(100);
				waitCount --;
			} catch (InterruptedException e) {
				System.exit(-1);
			}
		}
		if(waitCount ==0 ){
			System.out.println("TIMEOUT for response");
			System.exit(-1);
		}
	}
	public void start(){
		try{
			connect();
			if(authorize()){
				waitTextRenderEngine();
				cmdInteractive();
			}
		}catch (Exception e) {
			System.out.println(e.getMessage());
			//			logger.error(e.getMessage(),e);
			return ;
		}
	}
	/**
	 * 安全验证,
	 * 用于实现连接dtalk引擎的安全验证过程
	 * @return 验证通过返回{@code true}，否则返回{@code false}
	 */
	protected abstract boolean authorize();
}
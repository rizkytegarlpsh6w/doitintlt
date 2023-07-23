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
import gu.dtalk.MenuItem;
import gu.dtalk.Ack;
import gu.dtalk.Ack.Status;
import gu.simplemq.Channel;
import gu.simplemq.IMQConnParameterSupplier;
import gu.simplemq.IMessageQueueConfigManager;
import gu.simplemq.IMessageQueueFactory;
import gu.simplemq.IPublisher;
import gu.simplemq.ISubscriber;
import gu.simplemq.MessageQueueFactorys;
import gu.simplemq.exceptions.SmqNotFoundConnectionException;
import net.gdface.utils.FaceUtilits;
import net.gdface.utils.NetworkUtil;
import static gu.dtalk.CommonConstant.*;
import static gu.dtalk.CommonUtils.*;
import static com.google.common.base.Preconditions.*;

/**
 * 字符终端实现基类
 * @author guyadong
 *
 */
public abstract class BaseConsole {
	protected static final Logger logger = LoggerFactory.getLogger(BaseConsole.class);
	final ISubscriber subscriber;
	final IPublisher publisher;
	/**
	 * 请求频道名,用于终端向设备端发送菜单命令(item)请求
	 * 这个频道名,在与设备端成功连接后,由设备端提供
	 */
	protected String reqChannel = null;
	/**
	 * 终端的MAC地址
	 */
	protected final byte[] temminalMac;
	/**
	 * 响应频道名,用于终端接收设备端的响应消息
	 * 这是个与终端MAC地址相关的常量,设备端只要知道终端的MAC就能得到它的响应频道名
	 */
	private final String ackchname;
	/**
	 * 连接频道名,用于终端向设备端发送连接请求
	 * 这是个与设备端MAC地址相关的常量,终端只要知道设备端的MAC就能得到它的连接频道名
	 */
	protected final String connchname;
	private final RenderEngine renderEngine = new RenderEngine();
	private final Channel<JSONObject> ackChannel;
	/**
	 * 出错时是否显示详细调用堆栈
	 */
	private boolean stackTrace = false;
	/**
	 * 构造方法
	 * @param devmac 要连接的设备MAC地址,测试设备程序在本地运行时可为空。
	 * @param manager 消息配置管理器类实例
	 * @throws SmqNotFoundConnectionException 
	 */
	public BaseConsole(String devmac, IMessageQueueFactory factory) {

		// 创建消息系统连接实例
		this.temminalMac = getSelfMac(factory.getHostAndPort());

		this.subscriber = checkNotNull(factory,"factory is null").getSubscriber();
		this.publisher = factory.getPublisher();
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
		// 删除非字母数字的分隔符
		devmac = devmac.replaceAll("[^\\w]", "");
		System.out.printf("DEVICE MAC address: %s\n", devmac);

		connchname = getConnChannel(devmac);

	}
	protected static byte[] getSelfMac(HostAndPort hostAndPort){
		try {
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
	 * @return MAC地址
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
			BaseItem item;
			BaseItem currentLevel = checkNotNull(renderEngine.getCurrentLevel(),"currentLevel is null");
			if(".".equals(path)){
				// 如果没有根据path找到对应的item则抛出异常
				item = currentLevel;
				path = item.getPath();
			}else{
				// 如果没有根据path找到对应的item则抛出异常
				item = checkNotNull(currentLevel.getChildByPath(path),"NOT FOUND item %s",path);
			}
			json.fluentPut(ITEM_FIELD_PATH,path)
				.fluentPut(ITEM_FIELD_CATALOG, item.getCatalog());
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
		// 显示提示信息
		System.out.printf("INPUT VALUE for %s(%s)%s(input empty for skip):",option.getUiName(),option.getName(),desc);
		String value = scanLine(new Predicate<String>() {

			@Override
			public boolean apply(String input) {
				if(isImage(json,renderEngine.getCurrentLevel())){
					try {
						json.fluentPut(OPTION_FIELD_VALUE, FaceUtilits.getBytesNotEmpty(new File(input)));
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
	    				// 执行命令前先保存当前菜单，因为执行命令后当前菜单会变化，
	    				// 而isQuit需要的参数是执行命令前的菜单位置
	    				MenuItem lastLevel = renderEngine.getCurrentLevel();
	    				// 执行命令
	    				if(inputCmd(scaner,json)){
	    					syncPublishReq(json);
	    				}else{
	    					// 输入空行则返回
	    					break;
	    				}
	    				if(isQuit(json,lastLevel)){
	    					return;
	    				}

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
	/**
	 * 启动终端
	 */
	public void start(){
		try{
			Channel<String> testch = new Channel<String>(connchname, String.class);
			long rc = publisher.publish(testch, "\"hello,dtalk\"");
			// 目标设备没有上线
			checkState(rc != 0,"TARGET DEVICE NOT online");
			if(rc>1){
				// 有两个设备侦听同一个连接频道
				System.out.println("WARN:DUPLICATED TARGET DEVICE WITH same MAC address");
			}		
			connect();
			if(authorize()){
				waitTextRenderEngine();
				cmdInteractive();
			}
		}catch (Exception e) {
			if(stackTrace){
				logger.error(e.getMessage(),e);	
			}else{
				System.out.println(e.getMessage());
			}
			return ;
		}
	}
	/**
	 * 安全验证,
	 * 用于实现连接dtalk引擎的安全验证过程
	 * @return 验证通过返回{@code true}，否则返回{@code false}
	 */
	protected abstract boolean authorize();
	/**
	 * @param stackTrace 要设置的 stackTrace
	 * @return 当前对象
	 */
	public BaseConsole setStackTrace(boolean stackTrace) {
		this.stackTrace = stackTrace;
		return this;
	}

	/**
	 * 根据消息系统配置管理器实例创建targetClass指定的终端实例
	 * @param targetClass dtalk终端实例
	 * @param devmac 目标设备MAC地址
	 * @param manager 消息系统配置管理器实例
	 * @return targetClass 实例
	 * @throws SmqNotFoundConnectionException 没有找到有效消息系统连接
	 */
	public static <T extends BaseConsole> T 
	makeConsole(Class<T> targetClass,String devmac,IMessageQueueConfigManager manager) throws SmqNotFoundConnectionException{
		IMQConnParameterSupplier config = checkNotNull(manager,"manager is null").lookupMessageQueueConnect();

		logger.info("use config={}",config);
		// 创建消息系统连接实例
		IMessageQueueFactory factory = MessageQueueFactorys.getFactory(config.getImplType())
				.init(config.getMQConnParameters())
				.asDefaultFactory();
		try {
			return targetClass.getConstructor(String.class,IMessageQueueFactory.class).newInstance(devmac,factory);
		} catch (Exception e) {
			Throwables.throwIfUnchecked(e);
			throw new RuntimeException(e);
		}
	}
}

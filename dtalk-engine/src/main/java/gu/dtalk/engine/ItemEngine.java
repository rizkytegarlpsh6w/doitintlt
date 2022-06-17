package gu.dtalk.engine;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.base.MoreObjects;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import gu.dtalk.Ack;
import gu.dtalk.Ack.Status;
import gu.dtalk.CmdItem;
import gu.dtalk.CommonUtils;
import gu.dtalk.ICmdInteractiveStatusListener;
import gu.dtalk.BaseItem;
import gu.dtalk.MenuItem;
import gu.dtalk.BaseOption;
import gu.dtalk.ItemType;
import gu.dtalk.RootMenu;
import gu.dtalk.exception.InteractiveCmdStartException;
import gu.simplemq.Channel;
import gu.simplemq.IPublisher;
import gu.simplemq.exceptions.SmqUnsubscribeException;
import gu.simplemq.redis.JedisPoolLazy;
import gu.simplemq.redis.RedisFactory;

import static com.google.common.base.Preconditions.*;
import static gu.dtalk.CommonConstant.*;
import static gu.dtalk.CommonUtils.*;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 消息驱动的菜单引擎，根据收到的请求执行对应的动作<br>
 * <li>OPTION：修改指定的参数</li>
 * <li>CMD:执行指定的命令</li>
 * <li>MENU:输出菜单内容</li>
 * @author guyadong
 *
 */
public class ItemEngine implements ItemAdapter{
	private static final Logger logger = LoggerFactory.getLogger(ItemEngine.class);
	private MenuItem root = new RootMenu(); 
	private IPublisher ackPublisher;
	private Channel<Ack<Object>> ackChannel;
	private BaseItem currentLevel;
	/**
	 * 当前设备的MAC地址(HEX字符串)
	 */
	private String selfMac;
	/**
	 * 最近一次操作的时间戳
	 */
	private long lasthit;
	private Channel<MenuItem> menuChannel;
	
	/**
	 * 当前执行的设备命令
	 */
	private String cmdLock=null;
	private final DtalkListener listener = new DtalkListener();
	private final ScheduledThreadPoolExecutor scheduledExecutor;
	private final ScheduledExecutorService timerExecutor;
	private ScheduledFuture<?> future;
	/** 定时检查任务，如果超时没有收到进度报告，则视为超时 */
	private final Runnable timerTask = new Runnable(){
		long progressInternal = TimeUnit.SECONDS.toMillis(listener.getProgressInternal());
		@Override
		public void run() {
				// cmdLock 解锁状态下自动中断定时任务
				checkState(cmdLock != null);
				long lastInternel = System.currentTimeMillis() - listener.lastProgress;
				if(lastInternel > progressInternal*4){
					listener.onTimeout();
				}
		}};
	public ItemEngine(JedisPoolLazy pool) {
		ackPublisher = RedisFactory.getPublisher(pool);
		this.scheduledExecutor =new ScheduledThreadPoolExecutor(1,
				new ThreadFactoryBuilder().setNameFormat("cmddog-pool-%d").build());	
		this.timerExecutor = MoreExecutors.getExitingScheduledExecutorService(	scheduledExecutor);
	}

	/** 
	 * 响应菜单命令
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void onSubscribe(JSONObject jsonObject) throws SmqUnsubscribeException {
		lasthit = System.currentTimeMillis();
		boolean isQuit = false;
		Ack<Object> ack = new Ack<Object>().setStatus(Ack.Status.OK).setDeviceMac(selfMac);
		try{
			BaseItem req = ItemType.parseItem(CommonUtils.normalize(jsonObject, MoreObjects.firstNonNull(currentLevel, root)));
			BaseItem found = null;
			if(currentLevel != null){
				found = currentLevel.getChild(req.getName());
				if(found == null){
					found = currentLevel.getChild(req.getPath());
				}
			}
			if(root.getPath().equals(req.getPath())){
				found = root;
			}
			if(found == null){
				found = root.getChildByPath(req.getPath());
			}
			checkArgument(null != found,"UNSUPPORTED ITEM");
			checkArgument(!found.isDisable(),"DISABLE ITEM [%s]",found.getPath());
			/** 交互设备命令没执行完，则抛出异常 */
			checkState(cmdLock == null || cmdLock.equals(found.getPath()),"CMD LOCKED %s",cmdLock);
			ack.setItem(found.getPath());
			switch(found.getCatalog()){
			case OPTION:{
				((BaseOption<Object>)found).updateFrom((BaseOption<Object>)req);
				break;
			}
			case CMD:{
				if(isBack(found)){
					//  输出上一级菜单
					currentLevel = MoreObjects.firstNonNull(found.getParent(),root);
					ackPublisher.publish(menuChannel, (MenuItem)currentLevel);
					return;
				}else if(isQuit(found)){
					isQuit = true;
				}else{
					// 执行命令
					CmdItem cmd = (CmdItem)found;
					CmdItem reqCmd = (CmdItem)req;
					if(cmdLock == null){
						for(BaseOption<Object> param:cmd.getParameters()){
							param.updateFrom(reqCmd.getParameter(param.getName()));
						}
						if(cmd.isInteractiveCmd()){
							// 启动设备交互命令执行
							cmd.startInteractiveCmd(listener.setAck(ack));
							// 设置为正常启动状态
							ack.setStatus(Status.ACCEPTED);
							// 命令加锁
							cmdLock = cmd.getPath();
							// 启动超时检查，避免设备死机造成的锁死
							startInternalCmdChecker();
						}else{
							// 启动设备命令执行
							ack.setValue(cmd.runImmediateCmd());
						}
					}else{
						// 只有交互设备命令会加锁，加锁状态下只能执行取消命令
						checkState(cmd.isInteractiveCmd(),"NOT INTERACTIVE CMD %s",cmd.getPath());
						/** 上一个命令没执行完，则抛出异常 */
						checkState(Boolean.TRUE.equals(reqCmd.getCanceled()),"CMD REENTRANT  %s",cmdLock);
						cmd.cancelInteractiveCmd();
						// 命令解锁
						cmdLock = null;
					}
				}
				break;
			}
			case MENU:{
				//  输出当前菜单后直接返回
				currentLevel = found;
				ackPublisher.publish(menuChannel, (MenuItem)currentLevel);
				return;
			}
			default:
				throw new IllegalArgumentException(String.format("UNSUPPORTED CATALOG [%s] of ITEM [%s]",found.getCatalog().name(),found.getPath()));
			}

		}catch (InteractiveCmdStartException e) {
			
			ack.setStatus(e.getStatus()).setStatusMessage(e.getMessage());
		}catch(Exception e){
			e.printStackTrace();
			ack.setStatus(Ack.Status.ERROR).setStatusMessage(e.getMessage());
		}
		// 向ack频道发送返回值消息
		if(ackChannel != null){
			ackPublisher.publish(ackChannel, ack);
		}
		if(isQuit){
			// 取消频道订阅,中断连接
			throw new SmqUnsubscribeException(true);
		}
	}

	@Override
	public MenuItem getRoot() {
		return root;
	}

	public ItemEngine setRoot(MenuItem root) {
		this.root = checkNotNull(root);
		// 自动添加退出命令在最后
		if(this.root.getChild(QUIT_NAME)==null){
			CmdItem quit = CommonUtils.makeQuit();
			((MenuItem)root).addChilds(quit);
		}
		return this;
	}

	@Override
	public long lastHitTime() {
		return lasthit;
	}

	@Override
	public void setAckChannel(String name){
		ackChannel = new Channel<Ack<Object>>(
				name,
				new TypeReference<Ack<Object>>() {}.getType());
		menuChannel = new Channel<MenuItem>(
				name,
				MenuItem.class);	
	}
	@Override
	public String getAckChannel(){
		return ackChannel == null ? null : ackChannel.name;
	}

	/**
	 * @return 返回当前设备的MAC地址(HEX字符串)
	 */
	public String getSelfMac() {
		return selfMac;
	}

	@Override
	public ItemEngine setSelfMac(String selfMac) {
		this.selfMac = selfMac;
		return this;
	}
	
	/**
	 * 启动交互设备命令超时检查定时任务
	 */
	private synchronized void startInternalCmdChecker(){
		if(null != future){
			this.scheduledExecutor.remove((Runnable) future);
		}
		/** 返回 RunnableScheduledFuture<?>实例  */
		future = this.timerExecutor.scheduleAtFixedRate(timerTask, listener.getProgressInternal(), listener.getProgressInternal(), TimeUnit.SECONDS);
	}
	private final class DtalkListener implements ICmdInteractiveStatusListener{

		private Ack<Object> ack;
		private long lastProgress;
		@Override
		public void onProgress(Integer progress, String statusMessage) {
			try {
				lastProgress = System.currentTimeMillis();
				ack.setStatus(Status.PROGRESS).setValue(progress).setStatusMessage(statusMessage);
				ackPublisher.publish(ackChannel, ack);
			} catch (Exception e) {
				logger.error(e.getMessage());
			}		
		}
		
		@Override
		public void onFinished(Object value) {
			try{
				// 命令解锁
				cmdLock = null;
				ack.setStatus(Status.OK).setValue(value);
				ackPublisher.publish(ackChannel, ack);
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		}

		@Override
		public void onCaneled() {
			try{
				// 命令解锁
				cmdLock = null;
				ack.setStatus(Status.CANCELED);
				ackPublisher.publish(ackChannel, ack);
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		}

		@Override
		public void onError(String errorMessage, Throwable throwable) {
			try{
				// 命令解锁
				cmdLock = null;
				ack.setStatus(Status.ERROR);
				StringBuffer buffer = new StringBuffer();
				if(null != errorMessage){
					buffer.append(errorMessage);
				}
				if(throwable != null){
					buffer.append(":").append(throwable.getMessage());
				}
				if(buffer.length() >0){
					ack.setStatusMessage(buffer.toString());
				}
				ackPublisher.publish(ackChannel, ack);	
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		}

		@Override
		public int getProgressInternal(){
			return 2;
		}

		/**
		 * 超时处理
		 */
		public void onTimeout(){
			try{
				// 命令解锁
				cmdLock = null;
				ack.setStatus(Status.TIMEOUT);
				ackPublisher.publish(ackChannel, ack);
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		}
		/**
		 * @param ack 要设置的 ack
		 * @return 
		 */
		public DtalkListener setAck(Ack<Object> ack) {
			this.ack = new Ack<Object>().setItem(ack.getItem()).setDeviceMac(ack.getDeviceMac());
			this.lastProgress = System.currentTimeMillis();
			return this;
		}

	}
}

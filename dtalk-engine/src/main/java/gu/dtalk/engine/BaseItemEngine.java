package gu.dtalk.engine;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.MoreObjects;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import gu.dtalk.Ack;
import gu.dtalk.Ack.Status;
import gu.dtalk.CmdItem;
import gu.dtalk.CommonConstant.ReqCmdType;
import gu.dtalk.CommonUtils;
import gu.dtalk.ICmdInteractiveStatusListener;
import gu.dtalk.BaseItem;
import gu.dtalk.MenuItem;
import gu.dtalk.BaseOption;
import gu.dtalk.ItemType;
import gu.dtalk.RootMenu;
import gu.dtalk.exception.InteractiveCmdStartException;
import gu.simplemq.exceptions.SmqUnsubscribeException;
import gu.simplemq.json.JSONObjectDecorator;
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
 * <ul>
 * <li>OPTION：修改指定的参数</li>
 * <li>CMD:执行指定的命令</li>
 * <li>MENU:输出菜单内容</li>
 * </ul>
 * @author guyadong
 *
 */
public abstract class BaseItemEngine implements BaseItemDriver{
	protected static final Logger logger = LoggerFactory.getLogger(BaseItemEngine.class);
	private MenuItem root = new RootMenu(); 
	private BaseItem currentLevel;
	/**
	 * 当前设备的MAC地址(HEX字符串)
	 */
	private String selfMac;
	/**
	 * 最近一次操作的时间戳
	 */
	private long lasthit;
	/**
	 * 当前执行的设备命令
	 */
	private String cmdLock=null;
	private final ScheduledThreadPoolExecutor scheduledExecutor;
	private final ScheduledExecutorService timerExecutor;
	private ScheduledFuture<?> future;
	/** 定时检查任务，如果超时没有收到进度报告，则视为超时 */
	private final Runnable timerTask = new Runnable(){
		long progressInternal = TimeUnit.SECONDS.toMillis(getDtalkListener().getProgressInternal());
		@Override
		public void run() {
				// cmdLock 解锁状态下自动中断定时任务
				checkState(cmdLock != null);
				long lastInternel = System.currentTimeMillis() - getDtalkListener().lastProgress;
				if(lastInternel > progressInternal*4){
					getDtalkListener().onTimeout();
				}
		}};
	protected static final ThreadLocal<ReqCmdType> reqType = new  ThreadLocal<ReqCmdType>();

	public BaseItemEngine() {
		this.scheduledExecutor =new ScheduledThreadPoolExecutor(1,
				new ThreadFactoryBuilder().setNameFormat("cmddog-pool-%d").build());	
		this.timerExecutor = MoreExecutors.getExitingScheduledExecutorService(	scheduledExecutor);
	}

	/**
	 * 发送菜单数据
	 * @param object 菜单实例
	 */
	protected abstract void responseMenu(MenuItem object);
	/**
	 * 发送ack消息
	 * @param ack 响应实例
	 */
	protected abstract void responseAck(Ack<Object> ack);
	protected abstract DtalkListener getDtalkListener();
	protected void beforeSubscribe(JSONObjectDecorator jsonObject){
		reqType.set(MoreObjects.firstNonNull(
				jsonObject.getObjectOrNull(REQ_FIELD_REQTYPE, ReqCmdType.class),
				ReqCmdType.DEFAULT));
	}
	protected void afterSubscribe(){
		reqType.remove();
	}
	/** 
	 * 响应菜单命令
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void onSubscribe(JSONObject jsonObject) throws SmqUnsubscribeException {
		lasthit = System.currentTimeMillis();
		boolean isQuit = false;
		JSONObjectDecorator decorator = JSONObjectDecorator.wrap(jsonObject);
		beforeSubscribe(decorator);
		boolean multiTarget = ReqCmdType.MULTI == reqType.get();
		boolean taskQueue = ReqCmdType.TASKQUEUE == reqType.get();

		final JSONObject parameters = decorator.getJSONObjectOrNull(REQ_FIELD_PARAMETERS);
		// 删除参数字段以避免解析为item对象时抛出异常
		decorator.remove(REQ_FIELD_PARAMETERS);
		
		Ack<Object> ack = new Ack<Object>().
				setStatus(Ack.Status.OK)
				.setDeviceMac(selfMac)
				/* 如果 jsonObject中定义了命令序列号则使用该值初始化ack */
				.setCmdSn(decorator.getIntegerOrNull(REQ_FIELD_CMDSN));
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
				checkState(!taskQueue,"OPTION item unsupport task request");
				((BaseOption<Object>)found).updateFrom((BaseOption<Object>)req);
				break;
			}
			case CMD:{
				if(isBack(found)){
					checkState(!multiTarget,"'back' cmd unsupport multi-target cmd request");
					checkState(!taskQueue,"'back' cmd unsupport task request");

					//  输出上一级菜单
					currentLevel = MoreObjects.firstNonNull(found.getParent(),root);
					responseMenu((MenuItem) currentLevel);
					return;
				}else if(isQuit(found)){
					checkState(!multiTarget,"'quit' cmd unsupport multi-target cmd request");
					checkState(!taskQueue,"'quit' cmd unsupport task request");

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
							checkState(!multiTarget,"interactive cmd unsupport multi-target cmd request");
							checkState(!taskQueue,"interactive cmd  unsupport task request");

							// 启动设备交互命令执行
							cmd.startInteractiveCmd(getDtalkListener().init(ack));
							// 设置为正常启动状态
							ack.setStatus(Status.ACCEPTED);
							// 命令加锁
							cmdLock = cmd.getPath();
							// 启动超时检查，避免设备死机造成的锁死
							startInternalCmdChecker();
						}else{
							// 启动设备命令执行
							ack.setValue(cmd.runImmediateCmd(parameters));
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
				checkState(!multiTarget,"MENU item unsupport multi-target cmd request");
				checkState(!taskQueue,"MENU item unsupport task request");
				//  输出当前菜单后直接返回
				currentLevel = found;
				responseMenu((MenuItem) currentLevel);
				return;
			}
			default:
				throw new IllegalArgumentException(String.format("UNSUPPORTED CATALOG [%s] of ITEM [%s]",found.getCatalog().name(),found.getPath()));
			}

		} catch (InteractiveCmdStartException e) {			
			ack.writeError(e).setStatus(e.getStatus());
		} catch(Throwable e){
			e.printStackTrace();
			ack.writeError(e);
		} 
		try {
			// 向ack频道发送返回值消息
			responseAck(ack);
		} catch (Throwable e) {
			e.printStackTrace();
		} 
		afterSubscribe();
		if(isQuit){
			// 取消频道订阅,中断连接
			throw new SmqUnsubscribeException(true);
		}
	}

	@Override
	public MenuItem getRoot() {
		return root;
	}

	public BaseItemEngine setRoot(MenuItem root) {
		this.root = checkNotNull(root,"root is null");
		// 自动添加退出命令在最后
		if(this.root.getChild(QUIT_NAME)==null){
			CmdItem quit = CommonUtils.makeQuit();
			root.addChilds(quit);
		}
		return this;
	}

	@Override
	public long lastHitTime() {
		return lasthit;
	}

	public void setLastHitTime(long lastHit){
		this.lasthit = lastHit;
	}
	/**
	 * @return 返回当前设备的MAC地址(HEX字符串)
	 */
	public String getSelfMac() {
		return selfMac;
	}

	@Override
	public BaseItemEngine setSelfMac(String selfMac) {
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
		future = this.timerExecutor.scheduleAtFixedRate(timerTask, getDtalkListener().getProgressInternal(), getDtalkListener().getProgressInternal(), TimeUnit.SECONDS);
	}
	protected abstract class DtalkListener implements ICmdInteractiveStatusListener{

		private Ack<Object> ack;
		private long lastProgress;
		@Override
		public void onProgress(Integer progress, String statusMessage) {
			try {
				lastProgress = System.currentTimeMillis();
				ack.setStatus(Status.PROGRESS).setValue(progress).setStatusMessage(statusMessage);
				responseAck(ack);
			} catch (Throwable e) {
				logger.error(e.getMessage());
			}		
		}
		
		@Override
		public void onFinished(Object value) {
			try{
				// 命令解锁
				cmdLock = null;
				ack.setStatus(Status.OK).setValue(value);
				responseAck(ack);
			} catch (Throwable e) {
				logger.error(e.getMessage());
			}
		}

		@Override
		public void onCaneled() {
			try{
				// 命令解锁
				cmdLock = null;
				ack.setStatus(Status.CANCELED);
				responseAck(ack);
			} catch (Throwable e) {
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
				responseAck(ack);	
			} catch (Throwable e) {
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
				responseAck(ack);
			} catch (Throwable e) {
				logger.error(e.getMessage());
			}
		}
		/**
		 * 使用ack初始化当前对象
		 * @param ack 要设置的 ack
		 * @return 返回当前对象
		 */
		private DtalkListener init(Ack<Object> ack) {
			this.ack = new Ack<Object>().setItem(ack.getItem()).setDeviceMac(ack.getDeviceMac());
			this.lastProgress = System.currentTimeMillis();
			return this;
		}
		/**
		 * 发送ack消息
		 * @param ack 响应实例
		 */
		protected abstract void responseAck(Ack<Object> ack);
	}
}

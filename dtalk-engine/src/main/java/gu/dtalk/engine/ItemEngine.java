package gu.dtalk.engine;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.base.MoreObjects;
import gu.dtalk.Ack;
import gu.dtalk.CmdItem;
import gu.dtalk.CommonUtils;
import gu.dtalk.BaseItem;
import gu.dtalk.MenuItem;
import gu.dtalk.BaseOption;
import gu.dtalk.ItemType;
import gu.dtalk.RootMenu;
import gu.simplemq.Channel;
import gu.simplemq.IPublisher;
import gu.simplemq.exceptions.SmqUnsubscribeException;
import gu.simplemq.redis.JedisPoolLazy;
import gu.simplemq.redis.RedisFactory;

import static com.google.common.base.Preconditions.*;
import static gu.dtalk.CommonConstant.*;
import static gu.dtalk.CommonUtils.*;


/**
 * 消息驱动的菜单引擎，根据收到的请求执行对应的动作<br>
 * <li>OPTION：修改指定的参数</li>
 * <li>CMD:执行指定的命令</li>
 * <li>MENU:输出菜单内容</li>
 * @author guyadong
 *
 */
public class ItemEngine implements ItemAdapter{
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
	public ItemEngine(JedisPoolLazy pool) {
		ackPublisher = RedisFactory.getPublisher(pool);
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
					// 取消频道订阅,中断连接
					isQuit = true;
				}else{
					// 执行命令
					CmdItem cmd = (CmdItem)found;
					CmdItem reqCmd = (CmdItem)req;
					for(BaseOption<Object> param:cmd.getParameters()){
						param.updateFrom(reqCmd.getParameter(param.getName()));
					}
					ack.setValue(cmd.runCmd());
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

		}catch(SmqUnsubscribeException e){
			ack.setErrorMessage("DISCONNECT");
			throw e;
		}catch(Exception e){
			e.printStackTrace();
			ack.setStatus(Ack.Status.ERROR).setErrorMessage(e.getMessage());
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
}

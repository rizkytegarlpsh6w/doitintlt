package gu.dtalk.engine;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import gu.dtalk.Ack;
import gu.dtalk.CmdItem;
import gu.dtalk.ICmd;
import gu.dtalk.IItem;
import gu.dtalk.IMenu;
import gu.dtalk.IOption;
import gu.dtalk.ItemType;
import gu.dtalk.Items;
import gu.dtalk.MenuItem;
import gu.dtalk.RootMenu;
import gu.simplemq.Channel;
import gu.simplemq.IPublisher;
import gu.simplemq.exceptions.SmqUnsubscribeException;
import gu.simplemq.redis.JedisPoolLazy;
import gu.simplemq.redis.RedisFactory;

import static com.google.common.base.Preconditions.*;

/**
 * 消息驱动的菜单引擎，根据收到的请求执行对应的动作<br>
 * <li>OPTION：修改指定的参数</li>
 * <li>CMD:执行指定的命令</li>
 * <li>MENU:输出菜单内容</li>
 * @author guyadong
 *
 */
public class ItemEngine implements ItemAdapter{
	private IMenu root = new RootMenu(); 
	private IPublisher ackPublisher;
	private final Channel<Ack<Object>> ackChannel;
	private HeartbeatProber prober;
	private long lasthit;
	public ItemEngine(String ackChannelName,JedisPoolLazy pool) {
		ackChannel = new Channel<Ack<Object>>(
				ackChannelName,
				new TypeReference<Ack<Object>>() {}.getType());
		ackPublisher = RedisFactory.getPublisher(pool);

	}

	
	/** 
	 * 响应菜单命令
	 */
	@Override
	public void onSubscribe(JSONObject jsonObject) throws SmqUnsubscribeException {
		lasthit = System.currentTimeMillis();
		if(prober != null){
			prober.hit(System.currentTimeMillis());
		}
		Ack<Object> ack = new Ack<Object>().setStatus(Ack.Status.OK);
		try{
			IItem item = ItemType.parseItem(jsonObject);
			IItem found = root.recursiveFind(item.getName());
			if(null == found){
				throw new IllegalStateException("UNSUPPORTED ITEM");
			}if(found.isDisable()){
				throw new IllegalStateException("DISABLE ITEM");
			}else{
				switch(found.getCatalog()){
				case OPTION:{
					IOption option = (IOption)found;
					// 设置参数
					if(option.isReadOnly()){
						throw new IllegalStateException("READONLY VALUE");
					}else{
						Object v = ((IOption)item).getValue();
						if(!option.setValue(v)){
							throw new IllegalStateException("INVALID VALUE");
						}
					}
					break;
				}
				case CMD:{
					// 执行命令
					((ICmd)found).runCmd();
					break;
				}
				case MENU:{
					//  输出当前菜单
					ack.setValue(found);
					break;
				}
				default:
					throw new IllegalStateException("UNSUPPORTED CATALOG");
				}
			}
		}catch(SmqUnsubscribeException e){
			ack.setErrorMessage("DISCONNECT");
			throw e;
		}catch(Exception e){
			ack.setStatus(Ack.Status.ERROR).setErrorMessage(e.getMessage());
		}
		// 向ack频道发送返回值消息
		ackPublisher.publish(ackChannel, ack);
	}

	public IMenu getRoot() {
		return root;
	}

	public void setRoot(IMenu root) {
		this.root = checkNotNull(root);
		// 自动添加退出命令
		if(this.root.getChild(Items.QUIT_NAME)!=null){
			CmdItem quit = Items.makeQuit();
			MenuItem rootMenu = (MenuItem)root;
			rootMenu.addChilds(quit);
		}
	}

	@Override
	public long getLastHit() {
		return lasthit;
	}


}

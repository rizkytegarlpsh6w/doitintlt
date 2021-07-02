package gu.dtalk.engine;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import gu.dtalk.BaseOption;
import gu.dtalk.ICmd;
import gu.dtalk.IItem;
import gu.dtalk.IMenu;
import gu.dtalk.ItemType;
import gu.dtalk.RootMenu;
import gu.simplemq.Channel;
import gu.simplemq.IMessageAdapter;
import gu.simplemq.IPublisher;
import gu.simplemq.exceptions.SmqUnsubscribeException;
import gu.simplemq.redis.JedisPoolLazy;
import gu.simplemq.redis.RedisFactory;

import static com.google.common.base.Preconditions.*;

public class DevTalkEngine implements IMessageAdapter<JSONObject>{
	private IMenu root = new RootMenu(); 
	private IPublisher ackPublisher;
	private final Channel<Ack<Object>> ackChannel;
	public DevTalkEngine(String ackChannelName,JedisPoolLazy pool) {
		ackChannel = new Channel<Ack<Object>>(
				ackChannelName,
				new TypeReference<Ack<Object>>() {}.getType());
		ackPublisher = RedisFactory.getPublisher(pool);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onSubscribe(JSONObject jsonObject) throws SmqUnsubscribeException {
		Ack<Object> ack = new Ack<Object>().setStatus(Ack.Status.OK);
		try{
			IItem item = ItemType.parseItem(jsonObject);
			IItem found = root.recursiveFind(item.getName());
			if(null == found){
				ack.setStatus(Ack.Status.ERROR).setErrorMessage("UNSUPPORTED ITEM");
			}if(found.isDisable()){
				ack.setStatus(Ack.Status.ERROR).setErrorMessage("DISABLE ITEM");
			}else{				
				switch(found.getCatalog()){
				case OPTION:{
					Object v = ((BaseOption<Object>)item).getObjectValue();
					((BaseOption<Object>)found).setObjectValue(v);
					break;
				}
				case CMD:{
					((ICmd)found).runCmd();
					break;
				}
				case MENU:{
					ack.setValue(found);
					break;
				}
				default:
					ack.setStatus(Ack.Status.ERROR).setErrorMessage("UNSUPPORTED CATALOG");
					break;
				}
			}
		}catch(Exception e){
			ack.setStatus(Ack.Status.ERROR).setErrorMessage(e.getMessage());
		}
		ackPublisher.publish(ackChannel, ack);
	}

	public IItem getRoot() {
		return root;
	}

	public void setRoot(IMenu root) {
		this.root = checkNotNull(root);
	}

	public DevTalkEngine setAckPublisher(IPublisher ackPublisher) {
		this.ackPublisher = checkNotNull(ackPublisher);
		return this;
	}

}

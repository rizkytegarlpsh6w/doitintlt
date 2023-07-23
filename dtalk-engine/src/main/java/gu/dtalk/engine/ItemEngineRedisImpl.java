package gu.dtalk.engine;

import gu.dtalk.Ack;
import gu.dtalk.MenuItem;
import gu.simplemq.Channel;
import gu.simplemq.IPublisher;
import gu.simplemq.json.JSONObjectDecorator;
import static gu.dtalk.CommonConstant.*;
import static com.google.common.base.Preconditions.*;

/**
 * 消息驱动的菜单引擎实现<br>
 * @author guyadong
 *
 */
public class ItemEngineRedisImpl extends BaseItemEngine implements ItemAdapter{
	private final IPublisher publisher;
	private Channel<Ack<Object>> ackChannel;
	private Channel<MenuItem> menuChannel;
	
	private DtalkListener listener;
	private static final ThreadLocal<String> ackChannelName = new  ThreadLocal<String>();

	public ItemEngineRedisImpl(IPublisher publisher) {
		this.publisher = checkNotNull(publisher,"publisher is null");
	}
	@Override
	public void setAckChannel(String name){
		ackChannel = new Channel<Ack<Object>>(name,Ack.class);
		menuChannel = new Channel<MenuItem>(name,MenuItem.class);	
	}
	@Override
	public String getAckChannel(){
		return ackChannel == null ? null : ackChannel.name;
	}

	@Override
	protected void responseMenu(MenuItem object) {
		publisher.publish(menuChannel, (MenuItem)object);
	}

	/**
	 * 向ack频道发送返回值消息
	 * @see gu.dtalk.engine.BaseItemEngine#responseAck(gu.dtalk.Ack)
	 */
	@Override
	protected void responseAck(Ack<Object> ack) {
		switch (reqType.get()) {
		case MULTI:
		case TASKQUEUE:
			if(ackChannelName.get() != null){
				Channel<Ack<Object>> ackChannel = new Channel<Ack<Object>>(ackChannelName.get(),Ack.class);
				publisher.publish(ackChannel, ack);
			}
			break;
		default:
			if(this.ackChannel != null){
				publisher.publish(this.ackChannel, ack);
			}
			break;
		}
	}

	@Override
	protected synchronized DtalkListener getDtalkListener() {
		if(null == listener){
			listener = new DtalkListener(){

				@Override
				protected void responseAck(Ack<Object> ack) {
					publisher.publish(ackChannel, ack);					
				}};
		}
		return listener;
	}
	
	@Override
	protected void beforeSubscribe(JSONObjectDecorator jsonObject){		
		super.beforeSubscribe(jsonObject);
		ackChannelName.set(jsonObject.getStringOrNull(REQ_FIELD_ACKCHANNEL));
	}
	@Override
	protected void afterSubscribe(){
		super.afterSubscribe();
		ackChannelName.remove();
	}
}

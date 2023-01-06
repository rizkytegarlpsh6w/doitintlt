package gu.dtalk.engine;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import gu.dtalk.Ack;
import gu.dtalk.MenuItem;
import gu.simplemq.json.BaseJsonEncoder;
import static gu.dtalk.engine.ItemAdapterHttpServer.APPICATION_JSON;


/**
 * 消息驱动的菜单引擎http实现<br>
 * @author guyadong
 *
 */
public class ItemEngineHttpImpl extends BaseItemEngine{
	private DtalkListener listener;
	private static final ThreadLocal<Response> response = new  ThreadLocal<Response>();
	
	public ItemEngineHttpImpl() {
	}

	@Override
	protected void responseMenu(MenuItem object) {
		String json=BaseJsonEncoder.getEncoder().toJsonString(object);
		response.set(NanoHTTPD.newFixedLengthResponse(
    			Status.OK, 
    			APPICATION_JSON, 
    			json));
	}

	/**
	 * 向ack频道发送返回值消息
	 * @see gu.dtalk.engine.BaseItemEngine#responseAck(gu.dtalk.Ack)
	 */
	@Override
	protected void responseAck(Ack<Object> ack) {
		String json=BaseJsonEncoder.getEncoder().toJsonString(ack);
		response.set(NanoHTTPD.newFixedLengthResponse(
    			Ack.Status.OK.equals(ack.getStatus()) ? Status.OK: Status.INTERNAL_ERROR, 
    			APPICATION_JSON, 
    			json));
	}

	@Override
	protected synchronized DtalkListener getDtalkListener() {
		if(null == listener){
			listener = new DtalkListener(){

				@Override
				protected void responseAck(Ack<Object> ack) {
					//publisher.publish(ackChannel, ack);
					
				}};
		}
		return listener;
	}
	
	Response getResponse(){
		Response resp =  response.get();
		response.remove();
		return resp;
	}
}

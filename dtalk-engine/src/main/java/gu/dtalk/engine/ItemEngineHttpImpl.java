package gu.dtalk.engine;

import gu.dtalk.Ack;
import gu.dtalk.MenuItem;
import gu.simplemq.json.BaseJsonEncoder;
import static gu.dtalk.engine.DtalkHttpServer.APPICATION_JSON;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;
import org.nanohttpd.protocols.websockets.WebSocket;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;


/**
 * 消息驱动的菜单引擎http实现<br>
 * @author guyadong
 *
 */
public class ItemEngineHttpImpl extends BaseItemEngine{
	private DtalkListener listener;
	private Supplier<AtomicReference<WebSocket>> supplier= Suppliers.ofInstance(null);
	private static final ThreadLocal<Response> response = new  ThreadLocal<Response>();
	
	public ItemEngineHttpImpl() {
	}

	@Override
	protected void responseMenu(MenuItem object) {
		String json=BaseJsonEncoder.getEncoder().toJsonString(object);
		response.set(Response.newFixedLengthResponse(
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
		response.set(Response.newFixedLengthResponse(
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
					AtomicReference<WebSocket> ws = supplier.get();
					if(ws != null){
						synchronized (ws) {
							try {
								if(null != ws.get()){
									ws.get().send(BaseJsonEncoder.getEncoder().toJsonString(ack));
								}
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}					
				}};
		}
		return listener;
	}
	
	Response getResponse(){
		Response resp =  response.get();
		response.remove();
		return resp;
	}

	/**
	 * @param supplier 要设置的 supplier
	 * @return 
	 */
	ItemEngineHttpImpl setSupplier(Supplier<AtomicReference<WebSocket>> supplier) {
		if(null != supplier){
			this.supplier = supplier;
		}
		return this;
	}
}

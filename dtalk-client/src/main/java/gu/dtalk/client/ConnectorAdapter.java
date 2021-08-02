package gu.dtalk.client;

import java.io.PrintStream;

import gu.dtalk.Ack;
import gu.dtalk.Ack.Status;
import gu.simplemq.exceptions.SmqUnsubscribeException;

/**
 * 管理端连接控制器简单实现
 * @author guyadong
 *
 */
public class ConnectorAdapter extends TextMessageAdapter<Ack<String>> {
	private String reqChannel;
	@Override
	public void onSubscribe(Ack<String> ack) throws SmqUnsubscribeException {
		super.onSubscribe(ack);
		render.rendeAck(ack);
		if(ack.getStatus() == Status.OK){
			reqChannel = ack.getValue();
			// 连接成功取消订阅
			throw new SmqUnsubscribeException(true);
		}
	}
	public ConnectorAdapter setStream(PrintStream stream) {
		render.setStream(stream);
		return this;
	}

	public String getReqChannel() {
		return reqChannel;
	}
}

package gu.dtalk.client;

import gu.dtalk.Ack;
import gu.dtalk.MenuItem;

public interface ClientRender {

	/**
	 * @param ack
	 * @param renderValueIfOk 当Ack状态为OK时是否输出不为null的value字段内容
	 */
	void rendeAck(Ack<?> ack, boolean renderValueIfOk);

	void rendeItem(MenuItem menu);

}
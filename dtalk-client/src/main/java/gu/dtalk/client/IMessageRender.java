package gu.dtalk.client;

import gu.dtalk.Ack;
import gu.dtalk.MenuItem;

/**
 * 消息渲染器接口
 * @author guyadong
 *
 */
public interface IMessageRender {

	/**
	 * 渲染{@link Ack}
	 * @param ack
	 * @param renderValueIfOk 当Ack状态为OK时是否输出不为null的value字段内容
	 */
	void rendeAck(Ack<?> ack, boolean renderValueIfOk);

	/**
	 * 渲染菜单项
	 * @param menu
	 */
	void rendeItem(MenuItem menu);

}
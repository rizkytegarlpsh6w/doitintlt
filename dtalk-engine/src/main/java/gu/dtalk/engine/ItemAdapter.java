package gu.dtalk.engine;

import com.alibaba.fastjson.JSONObject;

import gu.dtalk.MenuItem;
import gu.simplemq.IMessageAdapter;

/**
 * 消息驱动的菜单引擎接口<br>
 * 根据收到的请求执行对应的动作
 * @author guyadong
 *
 */
public interface ItemAdapter extends IMessageAdapter<JSONObject> {
	/**
	 * @return 返回最近一次收到消息的时间戳
	 */
	public long lastHitTime();

	/**
	 * @return 返回请求响应频道
	 */
	String getAckChannel();

	/**
	 * 指定请求响应频道
	 * @param ackChannelName
	 */
	void setAckChannel(String ackChannelName);

	/**
	 * @return 返回根菜单实例
	 */
	MenuItem getRoot();
}

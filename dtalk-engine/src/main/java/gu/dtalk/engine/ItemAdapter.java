package gu.dtalk.engine;

/**
 * 消息驱动的菜单引擎接口<br>
 * 根据收到的请求执行对应的动作
 * @author guyadong
 *
 */
public interface ItemAdapter extends BaseItemDriver {


	/**
	 * @return 返回请求响应频道
	 */
	String getAckChannel();

	/**
	 * 指定请求响应频道
	 * @param ackChannelName 频道名
	 */
	void setAckChannel(String ackChannelName);

}

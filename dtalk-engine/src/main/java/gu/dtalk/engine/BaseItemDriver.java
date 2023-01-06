package gu.dtalk.engine;

import com.alibaba.fastjson.JSONObject;

import gu.dtalk.MenuItem;
import gu.simplemq.IMessageAdapter;

public interface BaseItemDriver extends IMessageAdapter<JSONObject> {
	/**
	 * @return 返回根菜单实例
	 */
	MenuItem getRoot();
	/**
	 * 设置当前设备的MAC地址(HEX字符串)
	 * @param selfMac 要设置的 selfMac
	 * @return 当前接口对象
	 */
	BaseItemDriver setSelfMac(String selfMac);
	/**
	 * @return 返回最近一次收到消息的时间戳
	 */
	public long lastHitTime();
}

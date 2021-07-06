package gu.dtalk.engine;

import com.alibaba.fastjson.JSONObject;

import gu.simplemq.IMessageAdapter;

public interface ItemAdapter extends IMessageAdapter<JSONObject> {
	/**
	 * 返回最近一次收到消息的时间戳
	 * @return
	 */
	public long getLastHit();
}

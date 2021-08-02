package gu.dtalk.client;

import gu.simplemq.IMessageAdapter;
import gu.simplemq.exceptions.SmqUnsubscribeException;

public class TextMessageAdapter <T> implements IMessageAdapter<T>{

	private long lastRespTimestamp;
	private Object lastRespObj;
	protected TextRender render = new TextRender();
	
	public TextMessageAdapter() {
	}

	public TextRender getRender() {
		return render;
	}

	public void setRender(TextRender render) {
		if(null != render){
			this.render = render;
		}
	}

	public long getLastResp() {
		return lastRespTimestamp;
	}

	public Object getLastRespObj() {
		return lastRespObj;
	}

	@Override
	public void onSubscribe(T resp) throws SmqUnsubscribeException {
		lastRespTimestamp = System.currentTimeMillis();
		lastRespObj = resp;
		// DO NOTHING
	}

}

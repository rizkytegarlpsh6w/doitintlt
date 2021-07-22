package gu.dtalk.client;

import java.io.PrintStream;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;
import gu.dtalk.Ack;
import gu.dtalk.CommonUtils;
import gu.dtalk.IItem;
import gu.dtalk.IMenu;
import gu.dtalk.ItemType;
import gu.simplemq.IMessageAdapter;
import gu.simplemq.exceptions.SmqUnsubscribeException;

import static gu.dtalk.CommonUtils.*;
/**
 * 文本渲染引擎<br>
 * @author guyadong
 *
 */
public class TextRenderEngine implements IMessageAdapter<JSONObject>{
	private IItem currentLevel;
	private IMenu root;
	private long lastRespTimestamp;
	private Object lastRespObj;
	private TextRender render = new TextRender();
	public TextRenderEngine() {
	}

	
	@Override
	public void onSubscribe(JSONObject resp) throws SmqUnsubscribeException {
		lastRespTimestamp = System.currentTimeMillis();
		lastRespObj = resp;
		if(isAck(resp)){
			Ack<?> ack = TypeUtils.castToJavaBean(resp, Ack.class);
			render.rendeAck(ack);
		}else if(isItem(resp)){
			IItem item = ItemType.parseItem(resp);
			if(item instanceof IMenu){
				IMenu menu = (IMenu)item;
				render.rendeItem(menu);				
				if(CommonUtils.isRoot(item)){
					root = menu;
				}
				if(root !=null){
					currentLevel = root.getChildByPath(item.getPath());
				}
			}else{
				render.getStream().printf("UNSUPPORTED ITEM RENDE TYPE:%s\n", item.getCatalog());
			}
		}else{
			render.getStream().printf("UNKNOW TYPE:%s\n", resp.toString());
		}
	}

	public TextRenderEngine setStream(PrintStream stream) {
		render.setStream(stream);
		return this;
	}
	public IItem getCurrentLevel() {
		return currentLevel;
	}
	public IMenu getRoot() {
		return root;
	}
	public TextRender getRender() {
		return render;
	}
	public TextRenderEngine setRender(TextRender render) {
		if(null != render){
			this.render = render;
		}
		return this;
	}
	public TextRenderEngine reset(){
		currentLevel = null;
		root = null;
		return this;
	}
	public long getLastResp() {
		return lastRespTimestamp;
	}
	public Object getLastRespObj() {
		return lastRespObj;
	}
}

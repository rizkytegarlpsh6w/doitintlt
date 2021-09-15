package gu.dtalk.client;

import java.io.PrintStream;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;
import gu.dtalk.Ack;
import gu.dtalk.BaseItem;
import gu.dtalk.MenuItem;
import gu.dtalk.ItemType;
import gu.simplemq.exceptions.SmqUnsubscribeException;
import static gu.dtalk.CommonUtils.*;
import static com.google.common.base.Preconditions.*;

/**
 * 文本渲染引擎<br>
 * @author guyadong
 *
 */
public class TextRenderEngine extends TextMessageAdapter<JSONObject>{
	private MenuItem currentLevel;
	private MenuItem root;
	private Ack<?> lastAck;

	public Ack<?> getLastAck() {
		return lastAck;
	}


	public TextRenderEngine() {
	}

	
	@Override
	public void onSubscribe(JSONObject resp) throws SmqUnsubscribeException {
		super.onSubscribe(resp);
		lastAck = null;
		if(isAck(resp)){
			Ack<?> ack = TypeUtils.castToJavaBean(resp, Ack.class);
			lastAck = TypeUtils.castToJavaBean(resp, Ack.class);
			render.rendeAck(ack);
		}else if(isItem(resp)){
			BaseItem item = ItemType.parseItem(resp);
			if(item instanceof MenuItem){
				MenuItem menu = (MenuItem)item;
				render.rendeItem(menu);				
				if(isRoot(item)){
					root = menu;
					currentLevel = menu;
				}else{
					checkState(root!=null," root menu is uninitialized");
					currentLevel = (MenuItem) root.getChildByPath(menu.getPath());
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
	public MenuItem getCurrentLevel() {
		return currentLevel;
	}
	public MenuItem getRoot() {
		return root;
	}
	public TextRenderEngine reset(){
		currentLevel = null;
		root = null;
		return this;
	}
	public void paint(){
		render.rendeItem(currentLevel);				
	}
}

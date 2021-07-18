package gu.dtalk.client;

import java.io.PrintStream;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;
import gu.dtalk.Ack;
import gu.dtalk.IItem;
import gu.dtalk.IMenu;
import gu.dtalk.ItemType;
import gu.dtalk.Items;
import gu.simplemq.IMessageAdapter;
import gu.simplemq.exceptions.SmqUnsubscribeException;

/**
 * 文本渲染引擎<br>
 * @author guyadong
 *
 */
public class TextRenderEngine implements IMessageAdapter<JSONObject>{
	public static final String ACK_FIELD_STATUS="status";
	public static final String ITEM_FIELD_NAME="name";
	public static final String ITEM_FIELD_PATH="path";
	public static final String ITEM_FIELD_CATALOG="catalog";
	public static final String OPTION_FIELD_TYPE="type";
	public static final String OPTION_FIELD_VALUE="value";
	private IItem currentLevel;
	private IMenu root;
	private long lastResp;
	private TextRender render = new TextRender();
	public TextRenderEngine() {
	}
	private boolean hasField(JSONObject resp,String name){
		return resp.containsKey(name);
	}
	private boolean isAck(JSONObject resp){
		return hasField(resp,ACK_FIELD_STATUS);
	}
	private boolean isItem(JSONObject resp){
		return hasField(resp,ITEM_FIELD_NAME) 
				&&  hasField(resp,ITEM_FIELD_PATH) 
				&& hasField(resp,ITEM_FIELD_CATALOG);
	}	
	
	@Override
	public void onSubscribe(JSONObject resp) throws SmqUnsubscribeException {
		lastResp = System.currentTimeMillis();
		if(isAck(resp)){
			Ack<?> ack = TypeUtils.castToJavaBean(resp, Ack.class);
			render.rendeAck(ack);
		}else if(isItem(resp)){
			IItem item = ItemType.parseItem(resp);
			if(item instanceof IMenu){
				IMenu menu = (IMenu)item;
				render.rendeItem(menu);				
				if(Items.isRoot(item)){
					root = menu;
				}
				if(root !=null){
					currentLevel = root.getChildByPath(item.getPath());
				}
			}
		}else{
			render.getStream().printf("UNKNOW TYPE:%s", resp.toString());
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
		return lastResp;
	}
}

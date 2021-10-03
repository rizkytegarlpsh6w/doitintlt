package gu.dtalk.client;

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
 * 渲染引擎<br>
 * 用于显示从设备端收到的消息
 * @author guyadong
 *
 */
public class RenderEngine extends TextMessageAdapter<JSONObject>{
	private String currentPath;
	private MenuItem root;
	private Ack<?> lastAck;

	public Ack<?> getLastAck() {
		return lastAck;
	}


	public RenderEngine() {
	}

	
	@Override
	public void onSubscribe(JSONObject resp) throws SmqUnsubscribeException {
		super.onSubscribe(resp);
		lastAck = null;
		if(isAck(resp)){
			Ack<?> ack = TypeUtils.castToJavaBean(resp, Ack.class);
			lastAck = TypeUtils.castToJavaBean(resp, Ack.class);
			render.rendeAck(ack, true);
		}else if(isItem(resp)){
			BaseItem item = ItemType.parseItem(resp);
			if(item instanceof MenuItem){
				MenuItem menu = (MenuItem)item;
				currentPath = menu.getPath();
				if(isRoot(resp)){
					root = menu;
				}else{
					checkState(root!=null," root menu is uninitialized");
					// 更新root中当前菜单项内容
					root.getChildByPath(currentPath).getParent().updateChild(menu);
				}
				render.rendeItem(menu);
			}else{
				System.out.printf("UNSUPPORTED ITEM RENDE TYPE:%s\n", item.getCatalog());
			}
		}else{
			System.out.printf("UNKNOW TYPE:%s\n", resp.toString());
		}
	}

	public MenuItem getCurrentLevel() {
		return checkNotNull(root).findMenu(currentPath);
	}
	public MenuItem getRoot() {
		return root;
	}
	public RenderEngine reset(){
		currentPath = null;
		root = null;
		return this;
	}
	public void paint(){
		render.rendeItem(getCurrentLevel());				
	}
}

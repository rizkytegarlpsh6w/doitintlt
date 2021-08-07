package gu.dtalk;

import static gu.dtalk.CommonConstant.*;

import com.alibaba.fastjson.JSONObject;

import net.gdface.utils.FaceUtilits;

public class CommonUtils {

	/**
	 * 返回响应通道名
	 * @param mac
	 * @return
	 */
	public static String getAckChannel(String mac){
		return mac + ACK_SUFFIX;
	}
	public static String getAckChannel(byte[] mac){
		return getAckChannel(FaceUtilits.toHex(mac));
	}
	public static String getConnChannel(String mac){
		return mac + CONNECT_SUFFIX;
	}
	public static String getConnChannel(byte[] mac){
		return getConnChannel(FaceUtilits.toHex(mac));
	}
	private static boolean hasField(Object resp,String name){
		return resp instanceof JSONObject &&  ((JSONObject) resp).containsKey(name);
	}
	public static boolean isAck(Object resp){
		return resp instanceof JSONObject &&  hasField((JSONObject) resp,ACK_FIELD_STATUS);
	}
	public static boolean isItem(Object resp){
		return hasField(resp,ITEM_FIELD_NAME) 
				&&  hasField(resp,ITEM_FIELD_PATH) 
				&& hasField(resp,ITEM_FIELD_CATALOG);
	}	

	public static CmdItem makeQuit(){
		CmdItem item = new CmdItem();
		item.setName(QUIT_NAME);
		return item;
	}
	public static CmdItem makeBack(){
		CmdItem item = new CmdItem();
		item.setName(BACK_NAME);
		return item;
	}
	public static boolean isBack(BaseItem item){
		return (item instanceof BaseItem) && BACK_NAME.equals(item.getName());
	}
	public static boolean isRoot(BaseItem item){
		return (item instanceof BaseItem) && null == item.getParent();
	}
	public static boolean isQuit(BaseItem item){
		return (item instanceof BaseItem) && QUIT_NAME.equals(item.getName());
	}
	public static boolean isQuit(JSONObject item){
		return (item instanceof JSONObject) && QUIT_NAME.equals(((JSONObject)item).getString(ITEM_FIELD_NAME));
	}
	public static boolean isQuit(Object item){
		return isQuit((BaseItem) item)  || isQuit((JSONObject)item);
	}
}

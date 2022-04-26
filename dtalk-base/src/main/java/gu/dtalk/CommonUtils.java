package gu.dtalk;

import static com.google.common.base.Preconditions.checkArgument;
import static gu.dtalk.CommonConstant.*;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.MoreObjects;

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
	/**
	 * 生成连接频道名
	 * @param mac
	 * @return
	 */
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
	public static boolean isRoot(JSONObject item){
		return (item instanceof JSONObject) && "/".equals(item.getString(ITEM_FIELD_PATH));
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
	public static boolean isQuit(JSONObject item, BaseItem start){
		return isQuit(normalize(item,start));
	}
	public static boolean isQuit(Object item){
		return isQuit((BaseItem) item)  || isQuit((JSONObject)item);
	}
	public static boolean isImage(JSONObject item){
		return item == null ? false : OptionType.IMAGE.name().equals(item.getString(OPTION_FIELD_TYPE));
	}
	public static boolean isImage(JSONObject item, BaseItem start){
		return isImage(normalize(item,start));
	}
	/**
	 * 归一化输入的{@link JSONObject}对象<br>
	 * 根据{@value CommonConstant#ITEM_FIELD_NAME}或{@value CommonConstant#ITEM_FIELD_PATH}字段的值
	 * 查找是否存在指定的item,如果不存在抛出异常,
	 * 如果没有定义{@value CommonConstant#ITEM_FIELD_NAME}或{@value CommonConstant#ITEM_FIELD_PATH}则抛出异常<br>
	 * 如果 {@link JSONObject}没有指定{@value CommonConstant#ITEM_FIELD_CATALOG}字段，
	 * 则设置为找到的item的对应字段.<br>
	 * 找到的item为{@link BaseOption}对象,且 {@link JSONObject}没有指定{@value CommonConstant#OPTION_FIELD_TYPE}字段，
	 * 则设置为找到的item的对应字段.
	 * @param jsonObject
	 * @param start 搜索起始对象
	 * @return 返回归一化的{@code jsonObject}，
	 * 保证有定义{@value CommonConstant#ITEM_FIELD_NAME},{@value CommonConstant#ITEM_FIELD_PATH}字段
	 * @throws IllegalArgumentException
	 */
	public static JSONObject normalize(JSONObject jsonObject, BaseItem start){
		if(null != jsonObject && null != start){
			String name = jsonObject.getString(ITEM_FIELD_NAME);
			String path = jsonObject.getString(ITEM_FIELD_PATH);
			checkArgument(name != null || path != null,"NOT DEFINED path or name");
			BaseItem node;
			if(path != null){
				node = start.findChecked(path);
			}else if(name != null){
				node = start.findChecked(name);
			}else{
				throw new IllegalArgumentException("NOT DEFINED " + ITEM_FIELD_PATH +" or " + ITEM_FIELD_NAME);
			}
			if(jsonObject.containsKey(ITEM_FIELD_CATALOG)){
				checkArgument(node.getCatalog().equals(jsonObject.getObject(ITEM_FIELD_CATALOG, ItemType.class)),
						"MISMATCH CATALOG %s",
						node.getPath());
			}else{
				jsonObject.fluentPut(ITEM_FIELD_CATALOG, node.getCatalog());
			}
			if((node instanceof BaseOption)){
				BaseOption<?> opt = (BaseOption<?>)node;
				if(jsonObject.containsKey(OPTION_FIELD_TYPE)){
					checkArgument(opt.getType().equals(jsonObject.getObject(OPTION_FIELD_TYPE,OptionType.class)),
							"MISMATCH TYPE %s",
							node.getPath());
				}else{
					jsonObject.fluentPut(OPTION_FIELD_TYPE, opt.getType());
				}
			}
			if(name == null){
				jsonObject.fluentPut(ITEM_FIELD_NAME, node.getName());
			}
			for(Object child:MoreObjects.firstNonNull(jsonObject.getJSONArray(ITEM_FIELD_CHILDS),new JSONArray())){
				checkArgument(child instanceof JSONObject,"INVALID JSON FORMAT FOR CHILD OF %s",
						node.getPath());
				normalize((JSONObject)child, node);
			}
		}
		return jsonObject;
	}
	
}

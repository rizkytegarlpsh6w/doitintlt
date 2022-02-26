package gu.dtalk;

import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.TypeUtils;
import static com.google.common.base.Preconditions.*;
import static gu.dtalk.CommonConstant.*;
import java.util.Map;

/**
 * 菜单选项类型
 * @author guyadong
 *
 */
public enum ItemType {
	/** 参数类型 */OPTION,
	/** 命令 */CMD,
	/** 菜单*/MENU;
	/**
	 * @param jsonObject
	 * @return {@code jsonObject}反序列化为{@link BaseItem}实例
	 * @throws IllegalArgumentException 反序列化失败
	 */
	public static final BaseItem parseItem(Map<String,Object> jsonObject)  {
		ItemType catalog =TypeUtils.castToEnum(
				checkNotNull(jsonObject.get(ITEM_FIELD_CATALOG),"NOT FOUND %s field",ITEM_FIELD_CATALOG), 
				ItemType.class, 
				ParserConfig.getGlobalInstance());
		switch(catalog){
		case OPTION:
			return OptionType.parseOption(jsonObject);
		case CMD:
			return TypeUtils.castToJavaBean(jsonObject, CmdItem.class);
		case MENU:
			return TypeUtils.castToJavaBean(jsonObject, MenuItem.class);
		default:
			throw new IllegalArgumentException("UNSUPPORTED CATALOG: " + catalog); 
		}
	}
}

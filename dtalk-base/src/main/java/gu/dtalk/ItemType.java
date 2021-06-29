package gu.dtalk;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;

public enum ItemType {
	/** 参数类型 */OPTION,
	/** 命令 */CMD,
	/** 菜单*/MENU;
	public static final IItem parseItem(JSONObject t)  {
		ItemType catalog = ItemType.valueOf((String) t.get("catalog"));
		switch(catalog){
		case OPTION:
			return OptionType.parseOption(t);
		case CMD:
			return TypeUtils.castToJavaBean(t, CmdItem.class);
		case MENU:
			return TypeUtils.castToJavaBean(t, MenuItem.class);
		default:
			throw new IllegalArgumentException("UNSUPPORTED CATALOG"); 
		}

	}
}

package gu.dtalk;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;
import static com.google.common.base.Preconditions.*;

public enum ItemType {
	/** 参数类型 */OPTION,
	/** 命令 */CMD,
	/** 菜单*/MENU;
	public static final IItem parseItem(JSONObject t)  {
		String c = checkNotNull((String) t.get("catalog"),"NOT FOUND catalog field");
		ItemType catalog = ItemType.valueOf(c);
		switch(catalog){
		case OPTION:
			return OptionType.parseOption(t);
		case CMD:
			return TypeUtils.castToJavaBean(t, CmdItem.class);
		case MENU:
			return TypeUtils.castToJavaBean(t, MenuItem.class);
		default:
			throw new IllegalArgumentException("UNSUPPORTED CATALOG: " + c); 
		}

	}
}

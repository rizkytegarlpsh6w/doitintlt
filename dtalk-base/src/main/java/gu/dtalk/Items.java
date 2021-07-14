package gu.dtalk;

import static gu.dtalk.CommonConstant.*;

public class Items {
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
	public static boolean isBack(IItem item){
		return (item instanceof IItem) && BACK_NAME.equals(item.getName());
	}
	public static boolean isRoot(IItem item){
		return (item instanceof IItem) && null == item.getParent();
	}
	public static boolean isQuit(IItem item){
		return (item instanceof IItem) && QUIT_NAME.equals(item.getName());
	}
}

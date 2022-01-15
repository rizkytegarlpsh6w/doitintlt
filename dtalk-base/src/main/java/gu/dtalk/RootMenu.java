package gu.dtalk;

import static gu.dtalk.CommonConstant.*;

/**
 * 根菜单 
 * @author guyadong
 *
 */
public class RootMenu extends MenuItem {

	public RootMenu() {
		super();
		setName("");
		// root菜单没有back
		items.remove(BACK_NAME);
		addChilds(CommonUtils.makeQuit());	
	}

}

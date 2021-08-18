package gu.dtalk;

import static gu.dtalk.CommonConstant.*;

public class RootMenu extends MenuItem {

	public RootMenu() {
		super();
		setName("");
		// root菜单没有back
		items.remove(BACK_NAME);
		addChilds(CommonUtils.makeQuit());	
	}

}

package gu.dtalk;

public class RootMenu extends MenuItem {

	public RootMenu() {
		setName("");
		addChilds(CommonUtils.makeQuit());
	}

}

package gu.dtalk;

public class RootMenu extends MenuItem {

	public RootMenu() {
		setNameUncheck("root");
		addChilds(CommonUtils.makeQuit());
	}

}

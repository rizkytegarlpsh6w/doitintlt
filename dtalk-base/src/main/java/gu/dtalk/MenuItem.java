package gu.dtalk;

public class MenuItem extends BaseItem {
	public MenuItem() {
		initChilds();
	}

	private void initChilds(){
		items.clear();
		CmdItem back = CommonUtils.makeBack();		
		addChilds(back);
	}
	@Override
	public final boolean isContainer() {
		return true;
	}

	@Override
	public final ItemType getCatalog() {
		return ItemType.MENU;
	}
}

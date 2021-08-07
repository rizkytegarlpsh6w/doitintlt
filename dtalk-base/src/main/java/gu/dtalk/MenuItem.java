package gu.dtalk;

public class MenuItem extends BaseItem {
	public MenuItem() {
		initChilds();
	}

	void initChilds(){
		items.clear();
		CmdItem back = CommonUtils.makeBack();		
		items.put(back.getName(), back);
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

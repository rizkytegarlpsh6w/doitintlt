package gu.dtalk;
import static gu.dtalk.CommonUtils.*;

public class MenuItem extends BaseItem {
	public MenuItem() {
		items.clear();
		CmdItem back = makeBack();
		// 为菜单项添加返回项
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

package gu.dtalk;

import java.util.List;

public interface IMenu  extends IItem{

	boolean isEmpty();

	int childCount();

	IItem getChild(String name);

	List<IItem> getChilds();
}

package gu.dtalk;

import java.util.List;

public interface IMenu  extends IItem{

	IItem getChild(int index);

	boolean isEmpty();

	int childCount();

	IItem recursiveFind(final String name);

	IItem getChild(final String name);

	List<IItem> getChilds();
}

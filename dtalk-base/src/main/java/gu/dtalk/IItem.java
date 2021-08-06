package gu.dtalk;

import java.util.List;

public interface IItem {
	
	ItemType getCatalog();
	
	String getName();

	boolean isContainer();
	
	boolean isDisable();
	
	String getDescription();
	
	IItem getParent();
	
	List<IItem> getChilds();
	
	IItem getChild(String name);

	String getUiName();

	String json();

	void setPath(String path);

	String getPath();
	
	IItem getChildByPath(String name);

}

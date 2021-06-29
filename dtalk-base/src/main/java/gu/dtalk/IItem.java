package gu.dtalk;

public interface IItem {
	
	ItemType getCatalog();
	
	String getName();

	boolean isContainer();
	
	boolean isDisable();
	
	String getDescription();
	
	IItem getParent();

	String getUiName();

	String json();
}

package gu.dtalk;

public interface IItem {
	String getName();

	boolean isContainer();
	
	boolean isDisable();
	
	String getDescription();
	
	IItem getParent();

	String getUiName();

	String json();
}

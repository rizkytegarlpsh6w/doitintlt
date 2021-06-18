package gu.dtalk;

public interface IItem {
	String getName();

	boolean isContainer();
	
	String getDescription();
	
	IMenu getParent();
}

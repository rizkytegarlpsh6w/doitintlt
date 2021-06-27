package gu.dtalk;

public interface IMenu  extends IItem{

	IItem getItem(int index);

	boolean isEmpty();

	int size();
}

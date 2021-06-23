package gu.dtalk;

import com.alibaba.fastjson.JSON;

public interface IItem {
	String getName();

	boolean isContainer();
	
	boolean isDisable();
	
	String getDescription();
	
	IMenu getParent();

	String getUiName();

	String json();
}

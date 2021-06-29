package gu.dtalk;

import java.util.List;

public interface ICmd extends IItem {

	List<IOption> getParameters();

	void runCmd();

	IOption getParameter(String name);

	IOption getParameter(int index);

}

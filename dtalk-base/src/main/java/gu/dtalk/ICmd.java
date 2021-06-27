package gu.dtalk;

import java.util.List;

public interface ICmd extends IItem {

	List<IOption> getParameters();

	void runCmd();

}

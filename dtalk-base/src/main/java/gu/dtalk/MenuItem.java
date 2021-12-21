package gu.dtalk;
import static com.google.common.base.Preconditions.checkArgument;
import static gu.dtalk.CommonUtils.*;

import java.util.Collections;
import java.util.Map;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;

import gu.dtalk.exception.CmdExecutionException;
import gu.dtalk.exception.UnsupportCmdException;

/**
 * 菜单对象
 * @author guyadong
 *
 */
public class MenuItem extends BaseItem {
	public MenuItem() {
		items.clear();
		CmdItem back = makeBack();
		// 为菜单项添加返回项
		items.put(back.getName(), back);
	}

	@Override
	public final boolean isContainer() {
		return true;
	}

	@Override
	public final ItemType getCatalog() {
		return ItemType.MENU;
	}
	/**
	 * 执行cmdpath指定的命令
	 * @param cmdpath
	 * @param parameters 命令参数,参数名-参数值(json格式),没有参数，可以输入{@code null}或空
	 * @return 返回值，没有返回值则返回null
	 * @throws UnsupportCmdException 
	 * @throws CmdExecutionException 
	 */
	public Object runCmd(String cmdpath,Map<String, ?> parameters) throws UnsupportCmdException, CmdExecutionException{
		checkArgument(!Strings.isNullOrEmpty(cmdpath),"cmd's path is null or empty");
		parameters = MoreObjects.firstNonNull(parameters,Collections.<String, Object>emptyMap());
		CmdItem cmd = findCmd(cmdpath);
		if(cmd == null){
			throw new UnsupportCmdException(cmdpath + "is not a cmd item" );
		}
		return cmd.runCmd(parameters);
	}
	
	public MenuItem readonlyOption(String optpath,boolean readonly){
		BaseOption<Object> option = findOption(optpath);
		if(option != null){
			option.setReadOnly(readonly);
		}
		return this;
	}
	public MenuItem disableItem(String optpath,boolean disable){
		BaseItem option = find(optpath);
		if(option != null){
			option.setDisable(disable);
		}
		return this;
	}

	/**
	 * 返回选项的值，如果{@code optpath}指定的{@link BaseOption}不存在则返回{@code null}
	 * @param optpath
	 * @return
	 * @see BaseOption#fetch()
	 */
	public <T> T fetchOption(String optpath){
		BaseOption<T> option = findOption(optpath);
		if(option != null){
			return option.fetch();
		}
		return null;
	}
	/**
	 * 返回选项的值，如果{@code optpath}指定的{@link BaseOption}不存在则返回{@code null}
	 * @param optpath
	 * @return
	 * @see BaseOption#getValue()
	 */
	public <T> T optionValueOf(String optpath){
		BaseOption<T> option = findOption(optpath);
		if(option != null){
			return option.getValue();
		}
		return null;
	}
	/**
	 * 更新选项的值，如果{@code optpath}指定的{@link BaseOption}不存在则跳过
	 * @param optpath
	 * @param value
	 * @return
	 * @see BaseOption#updateFrom(Object)
	 */
	public <T>MenuItem  updateValueOf(String optpath,T value){
		BaseOption<T> option = findOption(optpath);
		if(option != null){
			option.updateFrom(value);
		}
		return this;
	}
}

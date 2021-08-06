package gu.dtalk;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gu.simplemq.IMessageAdapter;

public class CmdItem extends BaseItem implements ICmd {

	private static final Function<IItem, IOption> TO_OPTION = new Function<IItem,IOption>(){

		@Override
		public IOption apply(IItem input) {
			return (IOption) input;
		}};
	private static final Function<IOption,IItem> TO_ITEM = new Function<IOption,IItem>(){

		@Override
		public IItem apply(IOption input) {
			return input;
		}};
	private static final Function<IItem, Object> TO_VALUE = new Function<IItem, Object>() {
		@Override
		public Object apply(IItem input) {
			return ((IOption) input).getValue();
		}
	};
	@JSONField(serialize = false,deserialize = false)
	private IMessageAdapter<Map<String, Object>> cmdAdapter;
	public CmdItem() {
	}

	@Override
	public final boolean isContainer() {
		return true;
	}
	@Override
	public final ItemType getCatalog() {
		return ItemType.CMD;
	}

	@Override
	public List<IOption> getParameters(){
		return Lists.transform(getChilds(),TO_OPTION);
	}
	public void setParameters(List<IOption> parameters){
		items.clear();
		addParameters(parameters);
	}
	public CmdItem addParameters(IOption ... parameter){
		return addParameters(Arrays.asList(parameter));
	}
	public CmdItem addParameters(Collection<IOption> parameters){
		addChilds(Collections2.transform(parameters, TO_ITEM));
		return this;
	}
	public IMessageAdapter<Map<String, Object>> getCmdAdapter() {
		return cmdAdapter;
	}
	public CmdItem setCmdAdapter(IMessageAdapter<Map<String, Object>> cmdAdapter) {
		this.cmdAdapter = cmdAdapter;
		return this;
	}
	@Override
	public final void runCmd(){
		if(cmdAdapter !=null){
			// 将 parameter 转为 Map<String, Object>
			Map<String, Object> objParams = Maps.transformValues(items, TO_VALUE);
			cmdAdapter.onSubscribe(objParams);
		}
	}
	@Override
	public IOption getParameter(final String name){
		return (IOption) getChild(name);
	}

}

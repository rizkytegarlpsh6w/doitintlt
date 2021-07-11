package gu.dtalk;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gu.simplemq.IMessageAdapter;

public class CmdItem extends BaseItem implements ICmd {

	private final LinkedHashMap<String,IOption> parameters = new LinkedHashMap<>();
	@JSONField(serialize = false,deserialize = false)
	private IMessageAdapter<Map<String, Object>> cmdAdapter;
	public CmdItem() {
	}

	@Override
	public final boolean isContainer() {
		return true;
	}
	@Override
	public List<IOption> getParameters(){
		return Lists.newArrayList(parameters.values());
	}
	public CmdItem setParameters(Collection<IOption> parameters){
		this.parameters.clear();
		return addParameters(parameters);
	}
	public CmdItem addParameters(IOption ... parameter){
		return addParameters(Arrays.asList(parameter));
	}
	public CmdItem addParameters(Collection<IOption> parameters){
		parameters = MoreObjects.firstNonNull(parameters, Collections.<IOption>emptyList());
		for(IItem param:parameters){
			((BaseItem)param).setParent(this);
		}
		ImmutableMap<String, IOption> m = Maps.uniqueIndex(parameters, new Function<IOption,String>(){
			@Override
			public String apply(IOption input) {
				return input.getName();
			}});
		this.parameters.putAll(m);	
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
			Map<String, Object> objParams = Maps.transformValues(parameters, new Function<IOption, Object>() {
				@Override
				public Object apply(IOption input) {
					return input.getValue();
				}
			});
			cmdAdapter.onSubscribe(objParams);
		}
	}
	@Override
	public IOption getParameter(final String name){
		return parameters.get(name);
	}

	@Override
	public final ItemType getCatalog() {
		return ItemType.CMD;
	}

	@Override
	public List<IItem> getChilds() {
		return ImmutableList.<IItem>copyOf(parameters.values());
	}
}

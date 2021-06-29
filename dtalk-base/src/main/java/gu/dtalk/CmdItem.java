package gu.dtalk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import gu.simplemq.IMessageAdapter;

public class CmdItem extends BaseItem implements ICmd {

	private final List<IOption> parameters = new ArrayList<>();
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
		return parameters;
	}
	public CmdItem setParameters(Collection<IOption> parameters){
		if(null != parameters){
			this.parameters.clear();
			this.parameters.addAll(parameters);
			for(IOption param:parameters){
				((BaseItem)param).setParent(this);
			}
		}
		return this;
	}
	public CmdItem addParameters(IOption ... parameter){
		return addParameters(Arrays.asList(parameter));
	}
	public CmdItem addParameters(Collection<IOption> parameters){
		if(null != parameters){
			parameters.addAll(parameters);
			for(IOption param:parameters){
				((BaseItem)param).setParent(this);
			}
		}
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
			ImmutableMap<String, IOption> m = Maps.uniqueIndex(parameters, new Function<IOption,String>(){
				@Override
				public String apply(IOption input) {
					return input.getName();
				}});
			Map<String, Object> objParams = Maps.transformValues(m, new Function<IOption, Object>() {
				@Override
				public Object apply(IOption input) {
					return input.getObjectValue();
				}
			});
			cmdAdapter.onSubscribe(objParams);
		}
	}
	@Override
	public IOption getParameter(final String name){
		Optional<IOption> found = Iterables.tryFind(parameters, new Predicate<IOption>() {
			@Override
			public boolean apply(IOption input) {
				return input.getName().equals(name);
			}
		});
		return found.isPresent()? found.get():null;
	}

	@Override
	public IOption getParameter(int index) {
		try{
			return parameters.get(index);
		}catch (IndexOutOfBoundsException e) {
			return null;
		}
	}
}

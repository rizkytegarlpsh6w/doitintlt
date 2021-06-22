package gu.dtalk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import gu.simplemq.IMessageAdapter;

public class CmdItem extends BaseItem implements ICmd {

	private final List<IOption> parameters = new ArrayList<>();
	private IMessageAdapter<Map<String, Object>> cmdAdapter;
	public CmdItem() {
	}

	@Override
	public final boolean isContainer() {
		return false;
	}
	@Override
	public List<IOption> getParameters(){
		return parameters;
	}
	public CmdItem setParameters(Collection<IOption> parameters){
		if(null != parameters){
			this.parameters.clear();
			this.parameters.addAll(parameters);
		}
		return this;
	}
	public CmdItem addParameters(IOption ... parameter){
		if(null != parameter){
			parameters.addAll(Arrays.asList(parameter));
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
	public final void runCmd(){
		if(cmdAdapter !=null){
			ImmutableMap<String, IOption> map = Maps.uniqueIndex(parameters, new Function<IOption,String>(){

				@Override
				public String apply(IOption input) {
					return input.getName();
				}});
			Map<String, Object> objparams = Maps.transformValues(map, new Function<IOption, Object>() {

				@Override
				public Object apply(IOption input) {
					return input.getObjectValue();
				}
			});
			cmdAdapter.onSubscribe(objparams);
		}
	}
}

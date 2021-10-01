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

public class CmdItem extends BaseItem {

	private static final Function<BaseItem, BaseOption<?>> TO_OPTION = new Function<BaseItem,BaseOption<?>>(){

		@Override
		public BaseOption<?> apply(BaseItem input) {
			return (BaseOption<?>) input;
		}};
	private static final Function<BaseOption<?>,BaseItem> TO_ITEM = new Function<BaseOption<?>,BaseItem>(){

		@Override
		public BaseItem apply(BaseOption<?> input) {
			return input;
		}};
	private static final Function<BaseItem, Object> TO_VALUE = new Function<BaseItem, Object>() {
		@Override
		public Object apply(BaseItem input) {
			return ((BaseOption<?>) input).fetch();
		}
	};
	@JSONField(serialize = false,deserialize = false)
	private ICmdAdapter cmdAdapter;
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
	@JSONField(serialize = false,deserialize = false)
	public List<BaseOption<?>> getParameters(){
		return Lists.transform(getChilds(),TO_OPTION);
	}
	@JSONField(serialize = false,deserialize = false)
	public void setParameters(List<BaseOption<?>> parameters){
		items.clear();
		addParameters(parameters);
	}
	public CmdItem addParameters(BaseOption<?> ... parameter){
		return addParameters(Arrays.asList(parameter));
	}
	public CmdItem addParameters(Collection<BaseOption<?>> parameters){
		addChilds(Collections2.transform(parameters, TO_ITEM));
		return this;
	}
	public ICmdAdapter getCmdAdapter() {
		return cmdAdapter;
	}
	public CmdItem setCmdAdapter(ICmdAdapter cmdAdapter) {
		this.cmdAdapter = cmdAdapter;
		return this;
	}

	public final Object runCmd(){
		if(cmdAdapter !=null){
			// 将 parameter 转为 Map<String, Object>
			Map<String, Object> objParams = Maps.transformValues(items, TO_VALUE);
			return cmdAdapter.apply(objParams);
		}
		return null;
	}
	public BaseOption<?> getParameter(final String name){
		return (BaseOption<?>) getChild(name);
	}
	
	/**
	 * 设置所有参数为{@code null}
	 */
	public void reset(){
		for (BaseOption<?> item : getParameters()) {
			item.setValue(null);
		}
	}
	public static interface ICmdAdapter extends Function<Map<String, Object>, Object>{
		
	}
}

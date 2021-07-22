package gu.dtalk;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class MenuItem extends BaseItem implements IMenu {
	private final LinkedHashMap<String,IItem> items = new LinkedHashMap<>();
	public MenuItem() {
		initChilds();
	}

	private void initChilds(){
		items.clear();
		CmdItem back = CommonUtils.makeBack();		
		items.put(back.getName(), back);
	}
	@Override
	public final boolean isContainer() {
		return true;
	}

	@Override
	public List<IItem> getChilds(){
		return Lists.newArrayList(items.values());
	}
	public MenuItem setChilds(List<IItem> childs){
		initChilds();
		return addChilds(childs);
	}
	public MenuItem addChilds(IItem ... childs){
		return addChilds(Arrays.asList(childs));
	}
	public MenuItem addChilds(Collection<IItem> childs){
		childs = MoreObjects.firstNonNull(childs, Collections.<IItem>emptyList());
		for(IItem param:childs){
			((BaseItem)param).setParent(this);
		}
		ImmutableMap<String, IItem> m = Maps.uniqueIndex(childs, new Function<IItem,String>(){
			@Override
			public String apply(IItem input) {
				return input.getName();
			}});
		items.putAll(m);	
		return this;
	}
	@Override
	public int childCount() {
		return items.size();
	}
	@Override
	public boolean isEmpty() {
		return items.isEmpty();
	}
	@Override
	public IItem getChild(final String name){
		IItem item = items.get(name);
		if (null == item ){
			try{
				// 如果name为数字则返回数字
				return getChilds().get(Integer.valueOf(name));
			}catch (Exception  e) {}
		}
		return item;
	}
	@Override
	public final ItemType getCatalog() {
		return ItemType.MENU;
	}
}

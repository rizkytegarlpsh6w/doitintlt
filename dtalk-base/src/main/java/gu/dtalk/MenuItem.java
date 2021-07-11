package gu.dtalk;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class MenuItem extends BaseItem implements IMenu {
	private final LinkedHashMap<String,IItem> items = new LinkedHashMap<>();
	public MenuItem() {
	}

	@Override
	public final boolean isContainer() {
		return true;
	}

	@Override
	public List<IItem> getChilds(){
		return Lists.newArrayList(items.values());
	}
	public MenuItem setChilds(Collection<IItem> childs){
		this.items.clear();
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
		return items.get(name);
	}
	@Override
	public IItem getChildByPath(String input){
		input = normalizePath(input);
		if(input.startsWith(getPath())){
			String[] nodes = input.substring(getPath().length()).split("/");
			IItem next = this;
			for(final String node:nodes){
				Optional<IItem> find = Iterables.tryFind(next.getChilds(),new Predicate<IItem>() {

					@Override
					public boolean apply(IItem input) {
						return input.getName().equals(node);
					}
				});
				if(!find.isPresent()){
					return null;
				}
				next = find.get();
			}
			return next;
		}
		return null;
	}
	@Override
	public final ItemType getCatalog() {
		return ItemType.MENU;
	}
}

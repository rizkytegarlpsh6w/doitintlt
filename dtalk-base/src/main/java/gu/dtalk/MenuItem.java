package gu.dtalk;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class MenuItem extends BaseItem implements IMenu {
	final List<IItem> items = new LinkedList<>();
	public MenuItem() {
	}

	@Override
	public final boolean isContainer() {
		return true;
	}

	@Override
	public List<IItem> getChilds(){
		return items;
	}
	public MenuItem setChilds(Collection<IItem> childs){
		if(null != childs){
			this.items.clear();
			this.items.addAll(childs);
			for(IItem param:childs){
				((BaseItem)param).setParent(this);
			}
		}
		return this;
	}
	public MenuItem addChilds(IItem ... childs){
		if(null != childs){
			items.addAll(Arrays.asList(childs));
		}
		return this;
	}
	public MenuItem addParameters(Collection<IItem> childs){
		if(null != childs){
			childs.addAll(childs);
			for(IItem param:childs){
				((BaseItem)param).setParent(this);
			}
		}
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
	public IItem getChild(int index) {
		return items.get(index);
	}
	@Override
	public IItem getChild(final String name){
		Optional<IItem> found = Iterables.tryFind(items, new Predicate<IItem>() {
			@Override
			public boolean apply(IItem input) {
				return input.getName().equals(name);
			}
		});
		return found.isPresent()? found.get():null;
	}
	@Override
	public IItem recursiveFind(final String name){

		IItem item = getChild(name);
		if(items!=null){
			return item;
		}
		Optional<IItem> found = Iterables.tryFind(items, new Predicate<IItem>() {
			@Override
			public boolean apply(IItem input) {
				if(input instanceof MenuItem){
					IItem tmp = ((MenuItem)input).recursiveFind(name);
					return tmp != null;
				}else if(input instanceof ICmd){
					IOption tmp = ((ICmd)input).getParameter(name);
					return tmp != null;
				}
				return input.getName().equals(name);
			}
		});
		return found.isPresent()? found.get():null;
	}
}

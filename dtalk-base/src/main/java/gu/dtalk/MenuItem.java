package gu.dtalk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class MenuItem extends BaseItem implements IMenu {
	final List<IItem> items = new ArrayList<>();
	public MenuItem() {
	}

	@Override
	public final boolean isContainer() {
		return true;
	}

	public List<IItem> getItems(){
		return items;
	}
	public MenuItem setItems(Collection<IItem> parameters){
		if(null != parameters){
			this.items.clear();
			this.items.addAll(parameters);
		}
		return this;
	}
	public MenuItem addItems(IItem ... parameter){
		if(null != parameter){
			items.addAll(Arrays.asList(parameter));
		}
		return this;
	}
	@Override
	public int size() {
		return items.size();
	}
	@Override
	public boolean isEmpty() {
		return items.isEmpty();
	}
	@Override
	public IItem getItem(int index) {
		return items.get(index);
	}
}

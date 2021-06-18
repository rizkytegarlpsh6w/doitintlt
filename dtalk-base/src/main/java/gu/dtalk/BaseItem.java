package gu.dtalk;


public abstract class BaseItem implements IItem{
	private String name;
	private IMenu parent;
	public BaseItem() {
	}
	@Override
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@Override
	public IMenu getParent() {
		return parent;
	}
	public void setParent(IMenu parent) {
		this.parent = parent;
	}
}

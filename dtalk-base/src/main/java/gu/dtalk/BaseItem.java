package gu.dtalk;


public abstract class BaseItem implements IItem{
	private String name;
	private IMenu parent;
	private boolean disable=false;
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
	void setParent(IMenu parent) {
		this.parent = parent;
	}
	@Override
	public boolean isDisable() {
		return disable;
	}
	@Override
	public String getDescription() {
		return "";
	}
	public void setDisable(boolean disable) {
		this.disable = disable;
	}
}

package gu.dtalk;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.base.Strings;

import static com.google.common.base.Preconditions.*;

public abstract class BaseItem implements IItem{
	private String name;
	private String uiName;
	@JSONField(serialize = false,deserialize = false)
	private IMenu parent;
	private boolean disable=false;
	public BaseItem() {
	}
	@Override
	public String getName() {
		return name;
	}
	/**
	 * @param name 允许的字符[a-zA-Z0-9_],不允许有空格
	 */
	public void setName(String name) {
		checkArgument(!Strings.isNullOrEmpty(name) && name.matches("^\\w+$"),
				"invalid option name '%s',allow character:[a-zA-Z0-9_],not space char allowed",name);
		this.name = checkNotNull(name);
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
	@Override
	public String getUiName() {
		return Strings.isNullOrEmpty(uiName) ? name : uiName;
	}
	public void setUiName(String uiName) {
		this.uiName = uiName;
	}
	@Override
	public String json(){
		return JSON.toJSONString(this);
	}
	@Override
	public String toString() {
		return json();
	}
}

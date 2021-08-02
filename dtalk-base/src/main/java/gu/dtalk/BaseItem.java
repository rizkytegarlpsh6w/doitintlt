package gu.dtalk;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import static com.google.common.base.Preconditions.*;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseItem implements IItem{
	private String name;
	private String uiName;
	@JSONField(serialize = false,deserialize = false)
	private IItem parent;
	private String path = null;
	private boolean disable=false;
	@JSONField(deserialize = false)
	private String description = "";
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
		name = MoreObjects.firstNonNull(name, "").trim();
		checkArgument(!Strings.isNullOrEmpty(name) && name.matches("^[a-zA-Z]\\w+$"),
				"invalid option name '%s',allow character:[a-zA-Z0-9_],not space char allowed,start with alphabet",name);
		// 不允许使用保留字做名字
		checkArgument(!CommonConstant.RESERV_ENAMES.contains(name),"the name %s is reserved word",name);
		this.name = checkNotNull(name);
	}
	void setNameUncheck(String name){
		this.name = checkNotNull(name);
	}
	@Override
	public IItem getParent() {
		return parent;
	}
	void setParent(IItem parent) {
		checkArgument(parent ==null || parent.isContainer());
		this.parent = parent;
		this.path = createPath(false);
	}
	/**
	 * 生成能对象在菜单中全路径名
	 * @param indexInstead 是否用索引值代替名字
	 * @return
	 */
	private String createPath(boolean indexInstead){
		List<String> list = new ArrayList<>();
		for(BaseItem item = this;item.parent !=null;item = (BaseItem) item.parent){
			if(indexInstead){
				list.add(Integer.toString(parent.getChilds().indexOf(item)));
			}else{
				list.add(parent.getName());
			}
		}
		return "/"+Joiner.on('/').join(Lists.reverse(list)) + "/" + name;
	}
	/**
	 * 路径名归一化,以'/'开始，不以'/'结尾
	 * @param path
	 * @return
	 */
	private String normalizePath(String path){
		path = MoreObjects.firstNonNull(path, "").trim();
		if(path.length()>1 ){
			if(!path.startsWith("/")){
				path = "/" + path;
			}
			if(path.endsWith("/")){
				path = path.substring(0, path.length()-1);					
			}
		}
		return path;
	}
	@Override
	public String getPath() {
		if(path == null){
			path = createPath(false);
		}
		return path;
	}
	@Override
	public void setPath(String path) {
		this.path = normalizePath(path);
	}
	@Override
	public boolean isDisable() {
		return disable;
	}
	@Override
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
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
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof BaseItem))
			return false;
		BaseItem other = (BaseItem) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	@Override
	public IItem getChildByPath(String input){
		input = MoreObjects.firstNonNull(input, "").trim();
		String relpath = input;
		if(input.startsWith("/")){
			if(input.startsWith(getPath())){
				relpath = input.substring(getPath().length());
			}else{
				String inxpath = createPath(true); 
				if(input.startsWith(inxpath)){
					relpath = input.substring(inxpath.length());
				}else{
					return null;
				}
			}
		}
		if(relpath.isEmpty()){
			return null;
		}
		String[] nodes = relpath.split("/");
		IItem child = this;
		for(String node:nodes){
			child = child.getChild(node);
			if(child == null){
				return null;
			}
		}
		return child;
	
	}
}

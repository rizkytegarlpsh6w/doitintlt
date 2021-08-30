package gu.dtalk;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import static com.google.common.base.Preconditions.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

public abstract class BaseItem{
	
	private String name;
	private String uiName;
	@JSONField(serialize = false,deserialize = false)
	private BaseItem parent;
	private String path = null;
	private boolean disable=false;
	@JSONField(deserialize = false)
	private String description = "";
	protected final LinkedHashMap<String,BaseItem> items = new LinkedHashMap<>();
	public BaseItem() {
	}
	public String getName() {		
		return name;
	}
	/**
	 * @param name 允许的字符[a-zA-Z0-9_],不允许有空格
	 * @return 
	 */
	public BaseItem setName(String name) {
		name = checkNotNull(name,"name is null").trim();
		checkArgument(name.isEmpty() || name.matches("^[a-zA-Z]\\w+$"),
				"invalid option name '%s',allow character:[a-zA-Z0-9_],not space char allowed,start with alphabet",name);
		this.name = name;
		return this;
	}

	public BaseItem getParent() {
		return parent;
	}
	BaseItem setParent(BaseItem parent) {
		checkArgument(parent ==null || parent.isContainer(),"INVALID parent");
		checkArgument(parent == null || !parent.getChilds().contains(this),"DUPLICATE element in parent %s",this.getName());
		this.parent = parent;
		refreshPath();
		return this;
	}
	public abstract boolean isContainer();
	public abstract ItemType getCatalog();
	/**
	 * 生成能对象在菜单中全路径名
	 * @param indexInstead 是否用索引值代替名字
	 * @return
	 */
	private String createPath(boolean indexInstead){
		List<String> list = new ArrayList<>();
		for(BaseItem item = this; item.parent !=null ; item = (BaseItem) item.parent){
			if(indexInstead){
				list.add(Integer.toString(parent.getChilds().indexOf(item)));
			}else{
				list.add(item.getName());
			}
		}
		return "/" + Joiner.on('/').join(Lists.reverse(list));
	}
	private void refreshPath(){		
		this.path = createPath(false);
		for(BaseItem child:items.values()){
			child.refreshPath();
		}
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
	public String getPath() {
		if(path == null){
			refreshPath();
		}
		return path;
	}
	public BaseItem setPath(String path) {
		this.path = normalizePath(path);
		return this;
	}
	public boolean isDisable() {
		return disable;
	}
	public BaseItem setDisable(boolean disable) {
		this.disable = disable;
		return this;
	}
	public String getDescription() {
		return description;
	}
	public BaseItem setDescription(String description) {
		this.description = description;
		return this;
	}
	public String getUiName() {
		return Strings.isNullOrEmpty(uiName) ? name : uiName;
	}
	public BaseItem setUiName(String uiName) {
		this.uiName = uiName;
		return this;
	}
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
	public BaseItem getChildByPath(String input){
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
		BaseItem child = this;
		for(String node:nodes){
			child = child.getChild(node);
			if(child == null){
				return null;
			}
		}
		return child;
	
	}
	public List<BaseItem> getChilds() {
		return Lists.newArrayList(items.values());
	}
	public BaseItem setChilds(List<BaseItem> childs) {
		items.clear();
		return addChilds(childs);
	}
	public BaseItem addChilds(BaseItem ... childs) {
		return addChilds(Arrays.asList(childs));
	}
	public BaseItem addChilds(Collection<BaseItem> childs) {
		childs = MoreObjects.firstNonNull(childs, Collections.<BaseItem>emptyList());
		for(BaseItem child:childs){
			if(!items.containsKey(child.getName())){
				child.setParent(this);
				items.put(child.getName(), child);
			}
		}
		return this;
	}
	public int childCount() {
		return items.size();
	}
	public boolean isEmpty() {
		return items.isEmpty();
	}
	public BaseItem getChild(final String name) {
		BaseItem item = items.get(name);
		if (null == item ){
			try{
				// 如果name为数字则返回数字
				return getChilds().get(Integer.valueOf(name));
			}catch (Exception  e) {}
		}
		return item;
	}

}

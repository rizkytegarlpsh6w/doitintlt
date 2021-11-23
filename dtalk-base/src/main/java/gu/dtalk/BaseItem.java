package gu.dtalk;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import static com.google.common.base.Preconditions.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
	private static final Function<BaseItem,String> PATH_FUN = new Function<BaseItem,String>(){
		@Override
		public String apply(BaseItem input) {
			return input.getPath();
		}};
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
		if(relpath.startsWith("/")){
			relpath = relpath.substring(1);
		}
		if(relpath.endsWith("/")){
			relpath = relpath.substring(0,relpath.length()-1);
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
	/**
	 * 根据{@code path}指定的路径查找对象,
	 * 先在当前对象中查找，如果找不到，从根结点查找
	 * @param path
	 * @return 返回找到的{@link BaseItem},找不到返回{@code null}
	 */
	public BaseItem find(String path){
		// 当前对象查找
		BaseItem child = getChildByPath(path);
		if (child !=null) {
			return child;
		}
		BaseItem root = this;
		for(;root.getParent() != null;root = root.getParent()){}
		// 从根菜单查找
		return root.getPath().equals(path) ? this : root.getChildByPath(path);
	}
	/**
	 * @param path
	 * @return
	 * @see #find(String)
	 */
	public MenuItem findMenu(String path){
		BaseItem item = find(path);
		return (item instanceof MenuItem) ? (MenuItem)item : null;
	}
	/**
	 * @param path
	 * @return
	 * @see #find(String)
	 */
	public CmdItem findCmd(String path){
		BaseItem item = find(path);
		return (item instanceof CmdItem) ? (CmdItem)item : null;
	}
	/**
	 * @param path
	 * @return
	 * @see #find(String)
	 */
	@SuppressWarnings("unchecked")
	public <T>BaseOption<T> findOption(String path){
		BaseItem item = find(path);
		return (item instanceof BaseOption) ? (BaseOption<T>)item : null;
	}
	/**
	 * @param path
	 * @return
	 * @see #find(String)
	 */
	public Base64Option findBase64Option(String path){
		BaseItem item = find(path);
		return (item instanceof Base64Option) ? (Base64Option)item : null;
	}
	/**
	 * @param path
	 * @return
	 * @see #find(String)
	 */
	public BoolOption findBoolOption(String path){
		BaseItem item = find(path);
		return (item instanceof BoolOption) ? (BoolOption)item : null;
	}
	/**
	 * @param path
	 * @return
	 * @see #find(String)
	 */
	public IPv4Option findIPv4Option(String path){
		BaseItem item = find(path);
		return (item instanceof IPv4Option) ? (IPv4Option)item : null;
	}
	/**
	 * @param path
	 * @return
	 * @see #find(String)
	 */
	public MACOption findMACOption(String path){
		BaseItem item = find(path);
		return (item instanceof MACOption) ? (MACOption)item : null;
	}
	/**
	 * @param path
	 * @return
	 * @see #find(String)
	 */
	public IntOption findIntOption(String path){
		BaseItem item = find(path);
		return (item instanceof IntOption) ? (IntOption)item : null;
	}
	/**
	 * @param path
	 * @return
	 * @see #find(String)
	 */
	public FloatOption findFloatOption(String path){
		BaseItem item = find(path);
		return (item instanceof FloatOption) ? (FloatOption)item : null;
	}
	/**
	 * @param path
	 * @return
	 * @see #find(String)
	 */
	public DateOption findDateOption(String path){
		BaseItem item = find(path);
		return (item instanceof DateOption) ? (DateOption)item : null;
	}
	/**
	 * @param path
	 * @return
	 * @see #find(String)
	 */
	public StringOption findStringOption(String path){
		BaseItem item = find(path);
		return (item instanceof StringOption) ? (StringOption)item : null;
	}
	/**
	 * @param path
	 * @return
	 * @see #find(String)
	 */
	public PasswordOption findPasswordOption(String path){
		BaseItem item = find(path);
		return (item instanceof PasswordOption) ? (PasswordOption)item : null;
	}
	/**
	 * @param path
	 * @return
	 * @see #find(String)
	 */
	public UrlOption findUrlOption(String path){
		BaseItem item = find(path);
		return (item instanceof UrlOption) ? (UrlOption)item : null;
	}
	/**
	 * @param path
	 * @return
	 * @see #find(String)
	 */
	public ImageOption findImageOption(String path){
		BaseItem item = find(path);
		return (item instanceof ImageOption) ? (ImageOption)item : null;
	}
	/**
	 * @param path
	 * @return
	 * @see #find(String)
	 */
	@SuppressWarnings("unchecked")
	public <T>CheckOption<T> findCheckOption(String path){
		BaseItem item = find(path);
		return (item instanceof CheckOption) ? (CheckOption<T>)item : null;
	}
	/**
	 * @param path
	 * @return
	 * @see #find(String)
	 */
	@SuppressWarnings("unchecked")
	public <T>SwitchOption<T> findSwitchOption(String path){
		BaseItem item = find(path);
		return (item instanceof SwitchOption) ? (SwitchOption<T>)item : null;
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
	public Map<String, String> childNames(){
		return Maps.newLinkedHashMap(Maps.transformValues(items, PATH_FUN));
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
	/**
	 * 用{@code item}更新同名的子对象，如果对象不存在则跳过
	 * @param item
	 */
	public void updateChild(BaseItem item){
		if(items.containsKey(item.getName())){
			items.remove(item.getName());
			addChilds(item);
		}
	}
}

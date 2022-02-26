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

/**
 * 菜单选项抽象类<br>
 * 所有选项的基类
 * @author guyadong
 *
 */
public abstract class BaseItem{
	
	/**
	 * 条目名称([a-zA-Z0-9_],不允许有空格)
	 */
	private String name;
	/**
	 * 条目的界面显示名称,如果不指定则使用{@link #name}
	 */
	private String uiName;
	/**
	 * 当前对象父节点
	 */
	@JSONField(serialize = false,deserialize = false)
	private BaseItem parent;
	/**
	 * 当前对象在整个菜单树形结构中的全路径
	 */
	private String path = null;
	/**
	 * 当前条目是否禁用
	 */
	private boolean disable=false;
	/**
	 * 对当前条目的说明文字
	 */
	@JSONField(deserialize = false)
	private String description = "";
	/**
	 * 当前条目下的子条目
	 */
	protected final LinkedHashMap<String,BaseItem> items = new LinkedHashMap<>();
	/**
	 * 从{@link BaseItem}对象中返回条目路径的转换器
	 */
	private static final Function<BaseItem,String> PATH_FUN = new Function<BaseItem,String>(){
		@Override
		public String apply(BaseItem input) {
			return input.getPath();
		}};
	public BaseItem() {
	}
	/**
	 * @return 条目名称
	 */
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

	/**
	 * 返回父结点
	 * @return
	 */
	public BaseItem getParent() {
		return parent;
	}
	/**
	 * 检查循环引用, 如果为循环引用则抛出异常
	 */
	private void checkCycleRef(){
		BaseItem node = this;
		while(node.parent != null){
			checkState(node.parent != this, "CYCLE REFERENCE");
			node = node.parent;
		}
	}
	/**
	 * 设置父结点
	 * @param parent
	 * @return
	 */
	BaseItem setParent(BaseItem parent) {
		checkArgument(parent ==null || parent.isContainer(),"INVALID parent");
		checkArgument(parent == null || !parent.getChilds().contains(this),"DUPLICATE element in parent %s",this.getName());
		this.parent = parent;
		checkCycleRef();
		refreshPath();
		return this;
	}
	/**
	 * @return  是否为容器(可包含item)
	 */
	public abstract boolean isContainer();
	/**
	 * @return 返回item分类类型
	 */
	public abstract ItemType getCatalog();
	/**
	 * 生成能对象在菜单中全路径名
	 * @param indexInstead 是否用索引值代替名字
	 * @return 全路径名
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
	/**
	 * 重新计算当前条目及子条目的路径
	 */
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
	/**
	 * @return 当前对象在整个菜单树形结构中的全路径
	 */
	public String getPath() {
		if(path == null){
			refreshPath();
		}
		return path;
	}
	/**
	 * 设置当前对象在整个菜单树形结构中的全路径
	 * @param path
	 * @return 当前对象
	 */
	public BaseItem setPath(String path) {
		this.path = normalizePath(path);
		return this;
	}
	/**
	 * @return 当前条目是否禁用
	 */
	public boolean isDisable() {
		return disable;
	}
	/**
	 * 设置当前条目是否禁用
	 * @param disable
	 * @return 当前对象
	 */
	public BaseItem setDisable(boolean disable) {
		this.disable = disable;
		return this;
	}
	/**
	 * @return 对当前条目的说明文字

	 */
	public String getDescription() {
		return description;
	}
	/**
	 * 设置对当前条目的说明文字
	 * @param description
	 * @return 当前对象
	 */
	public BaseItem setDescription(String description) {
		this.description = description;
		return this;
	}
	/**
	 * @return 条目的界面显示名称
	 */
	public String getUiName() {
		return Strings.isNullOrEmpty(uiName) ? name : uiName;
	}
	/**
	 * 设置条目的界面显示名称
	 * @param uiName
	 * @return 当前对象
	 */
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
	/**
	 * 返回{@code path}指定的路径查找当前对象下的子条目<br>
	 * @param path
	 * @return 返回子条目，没有找到返回{@code null}
	 */
	public BaseItem getChildByPath(String path){
		path = MoreObjects.firstNonNull(path, "").trim();
		String relpath = path;
		if(path.startsWith("/")){
			if(path.startsWith(getPath())){
				relpath = path.substring(getPath().length());
			}else{
				String inxpath = createPath(true); 
				if(path.startsWith(inxpath)){
					relpath = path.substring(inxpath.length());
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
	 * 根据{@code path}指定的路径查找对象,
	 * 与{@link #find(String)}基本相同,只是当找不到指定的对象时抛出异常
	 * @param path
	 * @return 返回找到的{@link BaseItem}对象
	 * @throws IllegalArgumentException 没找到指定的对象
	 */
	public BaseItem findChecked(String path){
		return checkNotNull(find(path),"NOT FOUND ITEM %s",path);		
	}
	/**
	 * 根据path指定的路径查找menu对象, 先在当前对象中查找，如果找不到，从根结点查找
	 * @param path
	 * @return 返回找到的{@link CmdItem}对象,找不到返回null
	 */
	public MenuItem findMenu(String path){
		BaseItem item = find(path);
		return (item instanceof MenuItem) ? (MenuItem)item : null;
	}
	/**
	 * 根据path指定的路径查找menu对象, 与{@link #findCmd(String)}基本相同,只是当找不到指定的对象时抛出异常
	 * @param path
	 * @return 返回找到的{@link MenuItem}对象
	 * @throws IllegalArgumentException 没找到指定的对象
	 */
	public MenuItem findMenuChecked(String path){
		return checkNotNull(findMenu(path),"NOT FOUND MENU %s",path);
	}
	/**
	 * 根据path指定的路径查找cmd对象, 先在当前对象中查找，如果找不到，从根结点查找
	 * @param path
	 * @return 返回找到的{@link CmdItem}对象,找不到返回null
	 */
	public CmdItem findCmd(String path){
		BaseItem item = find(path);
		return (item instanceof CmdItem) ? (CmdItem)item : null;
	}
	/**
	 * 根据path指定的路径查找cmd对象, 与{@link #findCmd(String)}基本相同,只是当找不到指定的对象时抛出异常
	 * @param path
	 * @return 返回找到的{@link CmdItem}对象
	 * @throws IllegalArgumentException 没找到指定的对象
	 */
	public CmdItem findCmdChecked(String path){
		return checkNotNull(findCmd(path),"NOT FOUND CMD %s",path);
	}
	/**
	 * 根据path指定的路径查找对象, 先在当前对象中查找，如果找不到，从根结点查找
	 * @param path
	 * @return 返回找到的{@link BaseItem},找不到返回{@code null}
	 */
	@SuppressWarnings("unchecked")
	public <T>BaseOption<T> findOption(String path){
		BaseItem item = find(path);
		return (item instanceof BaseOption) ? (BaseOption<T>)item : null;
	}
	
	/**
	 * 根据{@code path}指定的路径查找option对象,
	 * 与{@link #findOption(String)}基本相同,只是当找不到指定的对象时抛出异常
	 * @return 返回找到的{@link BaseItem}
	 * @throws IllegalArgumentException 没找到指定的对象
	 * @see #findOption(String)
	 */
	public <T>BaseOption<T> findOptionChecked(String path){
		BaseOption<T> opt = findOption(path);
		return checkNotNull(opt,"NOT FOUND OPTION %s",path);
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
	/**
	 * 返回所有子条目
	 * @return
	 */
	public List<BaseItem> getChilds() {
		return Lists.newArrayList(items.values());
	}
	/**
	 * 设置子条目(会清除原有的子条目)
	 * @param childs
	 * @return 当前对象
	 */
	public BaseItem setChilds(List<BaseItem> childs) {
		items.clear();
		return addChilds(childs);
	}
	/**
	 * 添加子条目
	 * @param childs
	 * @return 当前对象
	 */
	public BaseItem addChilds(BaseItem ... childs) {
		return addChilds(Arrays.asList(childs));
	}
	/**
	 * 添加子条目
	 * @param childs
	 * @return 当前对象
	 */
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
	/**
	 * @return 返回子条目的数量
	 */
	public int childCount() {
		return items.size();
	}
	/**
	 * @return 所有子条目的名称-路径映射
	 */
	public Map<String, String> childNames(){
		return Maps.newLinkedHashMap(Maps.transformValues(items, PATH_FUN));
	}
	/**
	 * @return 是否有子条目
	 */
	public boolean isEmpty() {
		return items.isEmpty();
	}
	/**
	 * 根据{@code name}指定的条目名称查找当前对象下的子条目<br>
	 * 如果{@code name}为数字则为子条目索引
	 * @param name
	 * @return 子条目，没找到返回{@code null}
	 */
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

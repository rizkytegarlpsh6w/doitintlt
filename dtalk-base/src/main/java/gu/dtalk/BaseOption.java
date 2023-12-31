package gu.dtalk;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Observable;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import gu.dtalk.event.ValueChangeEvent;
import gu.dtalk.event.ValueListener;

import static com.google.common.base.Preconditions.*;

/**
 * 用于参数配置和命令参数的选项对象
 * @author guyadong
 *
 * @param <T> 实例封装的数据类型
 */
public abstract class BaseOption<T> extends BaseItem {
	/**
	 * 选项值
	 */
	private T optionValue;
	/**
	 * 选项默认值
	 */
	private T defaultValue;
	/**
	 * 该选项是否为必须的
	 */
	private boolean required;
	/**
	 * 该选项是否为只读的
	 */
	private boolean readOnly;
	/**
	 * 修改此选项值时会不会导致应用重启
	 */
	private boolean needReset;
	/**
	 * 修改此选项值后前端要不要重新获取数据
	 */
	private boolean needRefresh;
	/**
	 * 当前选项的java类型
	 */
	protected final Type type;
	/**
	 * 侦听器管理对象
	 */
	protected final Observable observable = new Observable(){
		@Override
		public void notifyObservers(Object arg) {
			setChanged();
			super.notifyObservers(arg);
		}
	};
	/**
	 * 数据值验证器，默认不验证直接返回true
	 */
	@JSONField(serialize = false,deserialize = false)
	private Predicate<T> valueValidator = Predicates.alwaysTrue();
	/**
	 * 保存已知可用的的值
	 */
	private LinkedHashSet<T> available = Sets.newLinkedHashSet();
	public BaseOption(Type type) {
		super();
		this.type = checkNotNull(type);
	}
	/**
	 * @return 当前选项的java类型
	 */
	public Type javaType(){
		return type;
	}
	/**
	 * @return 当前选项的类型
	 */
	public abstract OptionType getType();
	/**
	 * 设置当前选项的类型，
	 * 默认实现不会修改当前选项的类型
	 * @param type
	 * @return 当前对象
	 */
	BaseOption<T> setType(OptionType type){
		return this;
	}
	/**
	 * @return 该选项是否为只读的
	 */
	public boolean isReadOnly() {
		return readOnly;
	}
	/**
	 * 设置该选项是否为只读的
	 * @param readOnly -
	 * @return 当前对象
	 */
	public BaseOption<T> setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
		return this;
	}
	/**
	 * @return needReset
	 */
	public boolean isNeedReset() {
		return needReset;
	}
	/**
	 * 设置修改此选项值时会不会导致应用重启
	 * @param needReset 要设置的 needReset
	 * @return 当前对象
	 */
	public BaseOption<T> setNeedReset(boolean needReset) {
		this.needReset = needReset;
		return this;
	}
	
	/**
	 * @return needRefresh
	 */
	public boolean isNeedRefresh() {
		return needRefresh;
	}
	/**
	 * 设置修改此选项值后前端要不要重新获取数据
	 * @param needRefresh 要设置的 needRefresh
	 */
	public void setNeedRefresh(boolean needRefresh) {
		this.needRefresh = needRefresh;
	}
	/**
	 * @return regex
	 */
	public String getRegex() {
		return getType().regex;
	}
	/**
	 * @param regex 要设置的 regex
	 * @return 当前对象
	 */
	public BaseOption<T> setRegex(String regex) {
		return this;
	}
	@Override
	public final boolean isContainer() {
		return false;
	}

	@Override
	public final ItemType getCatalog() {
		return ItemType.OPTION;
	}
	@Override
	public final BaseItem addChilds(Collection<BaseItem> childs) {
		// DO NOTHING
		return this;
	}
	/**
	 * 验证value是否有效,该方法不会抛出异常
	 * @param value -
	 * @return 成功返回true，否则返回false
	 */
	@SuppressWarnings("unchecked")
	public boolean validate(Object value){
		try{
			return valueValidator.apply((T) value);
		}catch (Exception e) {
			return false;
		}
	}
	/**
	 * @return 选项值
	 */
	public final T getValue() {
		return	optionValue;
	}
	/**
	 * 设置指定的值<br>
	 * 如果值有改变则向observer发送{@link ValueChangeEvent}消息
	 * @param value -
	 * @return 当前对象
	 * @throws IllegalArgumentException 数值验证失败
	 * @see #validate(Object)
	 */
	public BaseOption<T> setValue(T value)  {
		if(!Objects.equal(value, optionValue)){
			optionValue = value;
			observable.notifyObservers(new ValueChangeEvent<BaseOption<T>>(this));
		}
		return this;
	}
	/**
	 * 更新选项的值，如果选项为只读(readonly)或value不满足条件({@link #validate(Object)})则抛出异常
	 * @param value 要更新的值
	 * @see #setValue(Object)
	 */
	public void updateFrom(T value){
		checkState(!isReadOnly(),"READONLY VALUE");
		checkArgument(valueValidator.apply(value),"INVALID VALUE");		
		setValue(value);
	}
	/**
	 * 用选项req的值更新当前选项的值
	 * @param req 更新源对象
	 * @see #updateFrom(Object)
	 */
	public void updateFrom(BaseOption<T> req){
		if(req != null){
			// 设置参数
			updateFrom(req.getValue());
		}
	}
	/**
	 * @return 返回缺省值
	 */
	public final T getDefaultValue() {
		return defaultValue;
	}
	/**
	 * 设置默认值，同时验证数据有效性，失败抛出异常
	 * @param defaultValue 默认值
	 * @return 当前对象
	 * @throws IllegalArgumentException 数值验证失败
	 * @see #validate(Object)
	 */
	public BaseOption<T> setDefaultValue(T defaultValue) {
		checkArgument(null == defaultValue || validate(defaultValue),"INVALID DEFAULT VALUE");
		this.defaultValue = defaultValue;
		return this;
	}
	/**
	 * @return 返回选项的值，如果为null则返回默认值
	 */
	public T fetch(){
		if(getValue() == null){
			return getDefaultValue();
		}
		return getValue();
	}
	/**
	 * 设置数据验证器
	 * @param validator 为null忽略
	 * @return 当前对象
	 */
	public synchronized BaseOption<T> setValidator(Predicate<T> validator) {
		if(validator != null){
			this.valueValidator = validator;
		}
		return this;
	}

	/**
	 * @return 该选项是否为必须的
	 */
	public boolean isRequired() {
		return required;
	}

	/**
	 * 设置该选项是否为必须的
	 * @param required -
	 * @return 当前对象
	 */
	public BaseOption<T> setRequired(boolean required) {
		this.required = required;
		return this;
	}	
	/**
	 * @return 将选项的值转为用于显示的字符串
	 */
	public String contentOfValue(){
		return optionValue == null ? "": optionValue.toString();
	}
	
	/**
	 * 以字符串形式设置值
	 * @param input 如果不符合数据类型的格式则抛出异常
	 * @return 当前对象
	 * @see OptionType#trans()
	 */
	public BaseOption<T> asValue(String input){
		return setValue(getType().<T>trans().apply(input));
	}
	/**
	 * 以字符串形式设置默认值
	 * @param input 如果不符合数据类型的格式则抛出异常
	 * @return 当前对象
	 * @see OptionType#trans()
	 */
	public BaseOption<T> asDefaultValue(String input){
		return setDefaultValue(getType().<T>trans().apply(input));
	}
	/**
	 * 检查value,defaultValue的有效性，无效则抛出异常
	 * @throws IllegalArgumentException value,defaultValue的值无效
	 * @return 当前对象
	 */
	public BaseOption<T> compile(){
		// 检测value,defaultValue的值是否有效,无效则抛出异常
		checkArgument(null == getValue() || validate(getValue())
				,"CHECK:invalid value of %s",getType().name());
		checkArgument(null == getDefaultValue() || validate(getDefaultValue())
				,"CHECK:invalid defaultValue of %s",getType().name());
		return this;
	}
	/**
	 * 添加事件侦听器
	 * @param listeners 侦听器列表
	 * @return 当前对象
	 */
	@SuppressWarnings("unchecked")
	@SafeVarargs
	public final BaseOption<T> addListener(ValueListener<T> ...listeners) {
		for (ValueListener<T> listener : MoreObjects.firstNonNull(listeners, new ValueListener[0])) {
			if(listener != null){
				this.observable.addObserver(listener);
			}
		}
		return this;
	}

	@SuppressWarnings("unchecked")
	@SafeVarargs
	public final BaseOption<T> deleteListener(ValueListener<T> ...listeners) {
		for (ValueListener<T> listener : MoreObjects.firstNonNull(listeners, new ValueListener[0])) {
			if(listener != null){
				this.observable.deleteObserver(listener);
			}
		}
		return this;
	}
	@Override
	BaseOption<T> setParent(BaseItem parent) {
		super.setParent(parent);
		// 父类为CmdItem时，disable,readOnly属性无效
		if(parent instanceof CmdItem){
			setDisable(false);
			setReadOnly(false);
		}
		return this;
	}
	/**
	 * @return 返回所有可用的值
	 */
	public List<T> getAvailable() {
		return Lists.newArrayList(available);
	}

	/**
	 * 设置可用的值
	 * @param available 要设置的 availableValues
	 * @return 当前对象
	 */
	public BaseOption<T> setAvailable(List<T> available) {
		this.available.clear();
		this.available.addAll(MoreObjects.firstNonNull(available, Collections.<T>emptyList()));
		return this;
	}

	/**
	 * 添加可用的值
	 * @param values 可用的值列表
	 * @return 当前对象
	 */
	@SuppressWarnings("unchecked")
	public BaseOption<T> addAvailable(T... values){
		if(values != null){
			this.available.addAll(Collections2.filter(Arrays.asList(values), Predicates.notNull()));
		}
		return this;
	}
	/**
	 * 删除可用的值
	 * @param values 删除值的列表
	 * @return 当前对象
	 */
	@SuppressWarnings("unchecked")
	public BaseOption<T> removeAvailable(T... values){
		if(values != null){
			this.available.removeAll(Collections2.filter(Arrays.asList(values), Predicates.notNull()));
		}
		return this;
	}

	public void clearAvailable() {
		available.clear();
	}
}

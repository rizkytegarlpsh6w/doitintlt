package gu.dtalk;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Observable;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import gu.dtalk.event.ValueChangeEvent;
import gu.dtalk.event.ValueListener;

import static com.google.common.base.Preconditions.*;

/**
 * @author guyadong
 *
 * @param <T> 实例封装的数据类型
 */
public abstract class BaseOption<T> extends BaseItem {
	private T optionValue;
	private T defaultValue;
	private boolean required;
	private boolean readOnly;
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
	public BaseOption(Type type) {
		super();
		this.type = checkNotNull(type);
	}
	public Type javaType(){
		return type;
	}
	public abstract OptionType getType();
	BaseOption<T> setType(OptionType type){
		return this;
	}
	public boolean isReadOnly() {
		return readOnly;
	}
	public BaseOption<T> setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
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
	 * @param value
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
	public final T getValue() {
		return	optionValue;
	}
	/**
	 * 设置指定的值，同时验证数据有效性，失败抛出异常
	 * @param value
	 * @return
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
	public void updateFrom(T value){
		// 设置参数
		checkArgument(validate(value),"INVALID VALUE");		
		setValue(value);
	}
	public void updateFrom(BaseOption<T> req){
		if(req != null){
			// 设置参数
			checkState(!isReadOnly(),"READONLY VALUE");
			updateFrom(req.getValue());
		}
	}
	public final T getDefaultValue() {
		return defaultValue;
	}
	/**
	 * 设置默认值，同时验证数据有效性，失败抛出异常
	 * @param defaultValue
	 * @return
	 * @throws IllegalArgumentException 数值验证失败
	 * @see #validate(Object)
	 */
	public BaseOption<T> setDefaultValue(T defaultValue) {
		checkArgument(null == defaultValue || validate(defaultValue),"INVALID DEFAULT VALUE");
		this.defaultValue = defaultValue;
		return this;
	}
	public T fetch(){
		if(getValue() == null){
			return getDefaultValue();
		}
		return getValue();
	}
	/**
	 * 设置数据验证器
	 * @param validator 为null忽略
	 * @return
	 */
	public synchronized BaseOption<T> setValidator(Predicate<T> validator) {
		if(validator != null){
			this.valueValidator = validator;
		}
		return this;
	}

	public boolean isRequired() {
		return required;
	}

	public BaseOption<T> setRequired(boolean required) {
		this.required = required;
		return this;
	}	
	public String contentOfValue(){
		return optionValue == null ? "": optionValue.toString();
	}
	
	/**
	 * 以字符串形式设置值
	 * @param input 如果不符合数据类型的格式则抛出异常
	 * @return
	 * @see OptionType#trans()
	 */
	public BaseOption<T> asValue(String input){
		return setValue(getType().<T>trans().apply(input));
	}
	/**
	 * 以字符串形式设置默认值
	 * @param input 如果不符合数据类型的格式则抛出异常
	 * @return
	 * @see OptionType#trans()
	 */
	public BaseOption<T> asDefaultValue(String input){
		return setDefaultValue(getType().<T>trans().apply(input));
	}
	/**
	 * 检查value,defaultValue的有效性，无效则抛出异常
	 * @throws IllegalArgumentException value,defaultValue的值无效
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
	 * @param listeners
	 * @return
	 */
	@SafeVarargs
	public final BaseOption<T> addListener(ValueListener<T> ...listeners) {
		if(listeners != null){
			for (ValueListener<T> listener : listeners) {
				if(listener != null){
					this.observable.addObserver(listener);
				}
			}
		}
		return this;
	}

	@SafeVarargs
	public final BaseOption<T> deleteListener(ValueListener<T> ...listeners) {
		if(listeners != null){
			for (ValueListener<T> listener : listeners) {
				if(listener != null){
					this.observable.deleteObserver(listener);
				}
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
}

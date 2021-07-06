package gu.dtalk;

import java.lang.reflect.Type;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import static com.google.common.base.Preconditions.*;

public abstract class BaseOption<T> extends BaseItem implements IOption {
	private T optionValue;
	private T defaultValue;
	private boolean reqiured;
	@JSONField(serialize = false,deserialize = false)
	protected final Type type;
	@JSONField(serialize = false,deserialize = false)
	private Predicate<T> valueValidator = Predicates.alwaysTrue();
	public BaseOption(Type type) {
		super();
		this.type = checkNotNull(type);
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}
	@Override
	public final boolean isContainer() {
		return false;
	}
	public static final boolean isValidString(String value,Type type) {
		try{
			return null == JSON.parseObject(value, type);
		}catch (Exception e) {
			return false;
		}
	}

	@Override
	public final Object getValue() {
		return	optionValue;
	}
	@SuppressWarnings("unchecked")
	@Override
	public boolean setValue(Object value) {
		if(!isDisable() && !isReadOnly()){
			if(valueValidator.apply((T) value)){
				optionValue = (T) value;
			}
		}
		return false;
	}
	@Override
	public final Object getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(T defaultValue) {
		this.defaultValue = defaultValue;		
	}

	public synchronized void setValidator(Predicate<T> validator) {
		if(validator!=null){
			this.valueValidator = validator;
		}
	}

	@Override
	public boolean isReqiured() {
		return reqiured;
	}

	public void setReqiured(boolean reqiured) {
		this.reqiured = reqiured;
	}

	@Override
	public final ItemType getCatalog() {
		return ItemType.OPTION;
	}

}

package gu.dtalk;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import static com.google.common.base.Preconditions.*;

public abstract class BaseOption<T> extends BaseItem {
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
	public abstract OptionType getType();
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

	public final Object getValue() {
		return	optionValue;
	}
	@SuppressWarnings("unchecked")
	public boolean setValue(Object value) {
		if(!isDisable() && !isReadOnly()){
			if(valueValidator.apply((T) value)){
				optionValue = (T) value;
			}
		}
		return false;
	}
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

	public boolean isReqiured() {
		return reqiured;
	}

	public void setReqiured(boolean reqiured) {
		this.reqiured = reqiured;
	}

	public final ItemType getCatalog() {
		return ItemType.OPTION;
	}

	public final List<BaseItem> getChilds() {
		return Collections.emptyList();
	}

	public final BaseItem getChild(String name) {
		return null;
	}

	@Override
	public final BaseItem getChildByPath(String input) {
		return null;
	}

	@Override
	public final void setChilds(List<BaseItem> childs) {
	}

	@Override
	public final void addChilds(BaseItem... childs) {
	}

	@Override
	public final BaseItem addChilds(Collection<BaseItem> childs) {
		return this;
	}

}

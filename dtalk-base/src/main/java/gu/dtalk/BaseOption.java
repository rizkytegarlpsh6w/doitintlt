package gu.dtalk;

import java.lang.reflect.Type;

import com.alibaba.fastjson.JSON;
import static com.google.common.base.Preconditions.*;

public abstract class BaseOption<T> extends BaseItem implements IOption {
	protected T optionValue;
	protected T defaultValue;
	protected final Type type;

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
	public static final boolean isValid(String value,Type type) {
		try{
			return null == JSON.parseObject(value, type);
		}catch (Exception e) {
			return false;
		}
	}
	@Override
	public final boolean isValid(String value) {
		return isValid(value, type);
	}

	@Override
	public final String getValue() {
		try{
			return	JSON.toJSONString(optionValue);
		}catch (Exception e) {
			return "ERROR VALUE";
		}
	}
	@Override
	public boolean setValue(String value) {
		try{
			optionValue =	JSON.parseObject(value, type);
			return true;
		}catch (Exception e) {
			return false;
		}
	}
	@Override
	public final String getDefaultValue() {
		try{
			return JSON.toJSONString(defaultValue);
		}catch (Exception e) {
			return "ERROR DEFAUTL VALUE";
		}
	}
	@Override
	public Object getObjectValue(){
		return optionValue;
	}

}

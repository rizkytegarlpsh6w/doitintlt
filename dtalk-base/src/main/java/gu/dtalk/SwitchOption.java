package gu.dtalk;

import java.lang.reflect.Type;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;

public class SwitchOption<E> extends CheckOption<E> {
	private final Type elementType;
	public SwitchOption() {
		super();
		elementType =new TypeReference<E>() {}.getType();
	}

	@Override
	public final OptionType getType() {
		return OptionType.SWITCH;
	}

	@Override
	public boolean setValue(String value) {
		super.setValue(value);
		if(optionValue!=null){
			if(optionValue.size() == 1){
				return true;
			}else if(isValid(value, elementType)){
				@SuppressWarnings("unchecked")
				List<E> one = Lists.newArrayList((E)JSON.parseObject(value, elementType));
				return super.setValue(JSON.toJSONString(one));
			}
		}
		optionValue = null;
		return false;
	}
}

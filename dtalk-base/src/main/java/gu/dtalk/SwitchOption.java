package gu.dtalk;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;

public class SwitchOption<E> extends CheckOption<E> {
	private final Type elementType;
	@JSONField(serialize = false,deserialize = false)
	private final Predicate<Set<Integer>> switchValidator = new Predicate<Set<Integer>>() {

		@Override
		public boolean apply(Set<Integer> input) {			
			return input.size() == 1;
		}
	};
	public SwitchOption() {
		super();
		elementType =new TypeReference<E>() {}.getType();
		setValidator(switchValidator);
	}

	@Override
	public final OptionType getType() {
		return OptionType.SWITCH;
	}

	@Override
	public boolean setValue(String value) {
		super.setValue(value);
		if(optionValue==null){
			if(isValidString(value, elementType)){
				@SuppressWarnings("unchecked")
				List<E> one = Lists.newArrayList((E)JSON.parseObject(value, elementType));
				return super.setValue(JSON.toJSONString(one));
			}
		}
		optionValue = null;
		return false;
	}
	@Override
	public synchronized void setValidator(Predicate<Set<Integer>> validator) {

		super.setValidator(Predicates.and(switchValidator, validator));
	
	}
}

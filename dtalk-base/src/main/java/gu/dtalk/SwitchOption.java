package gu.dtalk;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.base.MoreObjects;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import static com.google.common.base.Preconditions.*;

public class SwitchOption<E> extends CheckOption<E> {
	@JSONField(serialize = false,deserialize = false)
	private final Predicate<Set<Integer>> switchValidator = new Predicate<Set<Integer>>() {

		@Override
		public boolean apply(Set<Integer> input) {			
			return input.size() == 1;
		}
	};
	public SwitchOption() {
		super();
		super.setValidator(switchValidator);
	}

	@Override
	public final OptionType getType() {
		return OptionType.SWITCH;
	}

	@Override
	public BaseOption<Set<Integer>> setValidator(Predicate<Set<Integer>> validator) {
		return super.setValidator(Predicates.and(switchValidator, validator));
	}

	@JSONField(serialize = false,deserialize = false)
	public E getSwitch(){
		List<E> list = getSelected();
		return list.size()>0 ? list.get(0) : null;
	}
	@JSONField(serialize = false,deserialize = false)
	public CheckOption<E> setSwitch(E value){
		return setSelected(Arrays.asList(value));
	}

	@JSONField(serialize = false,deserialize = false)
	@Override
	public CheckOption<E> setSelected(List<E> sel) {
		sel = MoreObjects.firstNonNull(sel, Collections.<E>emptyList());
		if(!sel.isEmpty()){
			checkArgument(sel.size()==1,"TOO MANY SELECTED OPTIONS");
		}
		return super.setSelected(sel);
	}

}

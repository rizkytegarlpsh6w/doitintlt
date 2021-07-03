package gu.dtalk;

import java.util.Set;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

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
		setValidator(switchValidator);
	}

	@Override
	public final OptionType getType() {
		return OptionType.SWITCH;
	}

	@Override
	public synchronized void setValidator(Predicate<Set<Integer>> validator) {

		super.setValidator(Predicates.and(switchValidator, validator));
	
	}
}

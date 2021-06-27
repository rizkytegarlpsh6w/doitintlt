package gu.dtalk;

import java.util.LinkedHashMap;
import java.util.Set;

import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.base.MoreObjects;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import static com.google.common.base.Preconditions.*;
public class CheckOption<E> extends BaseOption<Set<Integer>> {
	@JSONField(serialize = false,deserialize = false)
	private final Predicate<Set<Integer>> checkValidator = new Predicate<Set<Integer>>() {

		@Override
		public boolean apply(Set<Integer> set) {
			boolean findInvalid = Iterables.tryFind(set, new Predicate<Integer>() {
				@Override
				public boolean apply(Integer index) {
					return index < 0 || index >= options.size();
				}
			}).isPresent();
			return !findInvalid;
		}
	};
	private final LinkedHashMap<E, String> options = new LinkedHashMap<>();
	public CheckOption() {
		super(new TypeReference<Integer>() {}.getType());
		setValidator(checkValidator);
	}

	public void addOption(E opt,String desc){
		options.put(checkNotNull(opt), MoreObjects.firstNonNull(desc, ""));
	}

	@Override
	public OptionType getType() {
		return OptionType.MULTICHECK;
	}

	@Override
	public synchronized void setValidator(Predicate<Set<Integer>> validator) {
		super.setValidator(Predicates.and(checkValidator, validator));
	}


}

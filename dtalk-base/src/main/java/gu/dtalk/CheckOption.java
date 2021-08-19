package gu.dtalk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

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
	protected final LinkedHashMap<E, String> options = new LinkedHashMap<>();
	public CheckOption() {
		super(new TypeReference<Set<Integer>>() {}.getType());
		super.setValidator(checkValidator);
	}

	public CheckOption<E> addOption(E opt,String desc){
		options.put(checkNotNull(opt), MoreObjects.firstNonNull(desc, ""));
		return this;
	}

	@Override
	public OptionType getType() {
		return OptionType.MULTICHECK;
	}

	@Override
	public BaseOption<Set<Integer>> setValidator(Predicate<Set<Integer>> validator) {
		return super.setValidator(Predicates.and(checkValidator, validator));
	}

	public List<E> getSelected(){
		@SuppressWarnings("unchecked")
		ArrayList<Integer> indexs = Lists.newArrayList((Set<Integer>) getValue());
		final ArrayList<E> opts = Lists.newArrayList(options.keySet());
		return Lists.transform(indexs, new Function<Integer, E>() {

			@Override
			public E apply(Integer input) {
				return opts.get(input);
			}
		});
	}
	public CheckOption<E> setSelected(List<E> sel){
		sel = MoreObjects.firstNonNull(sel, Collections.<E>emptyList());
		Iterable<E> itor = Iterables.filter(sel, new Predicate<E>() {

			@Override
			public boolean apply(E input) {
				return options.containsKey(input);
			}
		});
		final ArrayList<E> opts = Lists.newArrayList(options.keySet());

		HashSet<Integer> indexSet = Sets.newHashSet(Iterables.transform(itor, new Function<E,Integer>(){

			@Override
			public Integer apply(E input) {
				return opts.indexOf(input);
			}}));
		setValue(indexSet);
		return this;
	}
}

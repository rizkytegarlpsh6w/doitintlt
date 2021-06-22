package gu.dtalk;

import java.util.LinkedHashMap;
import java.util.Set;

import com.alibaba.fastjson.TypeReference;
import com.google.common.base.MoreObjects;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import static com.google.common.base.Preconditions.*;
public class CheckOption<E> extends BaseOption<Set<Integer>> {

	private final LinkedHashMap<E, String> options = new LinkedHashMap<>();
	public CheckOption() {
		super(new TypeReference<Integer>() {}.getType());
	}

	public void addOption(E opt,String desc){
		options.put(checkNotNull(opt), MoreObjects.firstNonNull(desc, ""));
	}

	@Override
	public OptionType getType() {
		return OptionType.MULTICHECK;
	}

	protected boolean isValueElemnt(Integer index){
		return index > 0 && index < options.size();
	}
	@Override
	public boolean setValue(String value) {
		super.setValue(value);
		if(optionValue!=null){
			boolean findInvalid = Iterables.tryFind(optionValue, new Predicate<Integer>() {
				@Override
				public boolean apply(Integer input) {
					return !isValueElemnt(input);
				}
			}).isPresent();
			if(findInvalid){
				optionValue = null;
			}
			return !findInvalid;
		}
		return false;
	}

}

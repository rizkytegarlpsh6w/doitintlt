package gu.dtalk;

import java.util.Arrays;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import gu.dtalk.CmdItem;
import gu.dtalk.OptionType;


public class OptionViewCmd implements CmdItem.ICmdAdapter{
	public static final String QUERY = "query";
	public OptionViewCmd() {
		super();
	}

	private Map<String, Object>toMap(OptionType optionType){
		ImmutableMap<String, Object> m = ImmutableMap
				.<String, Object>of(
						"name",optionType.name(),
						"regex",optionType.regex,
						"optClass",optionType.optClass.getSimpleName());
		
		return m;
	}
	@Override
	public Object apply(Map<String, Object> input) {
		String name = MoreObjects.firstNonNull((String)input.get(QUERY),"").toUpperCase();
		if(name.isEmpty()){
			return Lists.transform(Arrays.asList(OptionType.values()), new Function<OptionType,Map<String,Object>>(){

				@Override
				public Map<String, Object> apply(OptionType input) {
					return toMap(input);
				}});
		}else{
			OptionType query = OptionType.valueOf(name);
			return toMap(query);
		}
	}


}

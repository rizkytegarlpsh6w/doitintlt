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
						"optClass",optionType.optClass.getName());
		
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
			try{
				OptionType query = OptionType.valueOf(name);
				return toMap(query);
			}catch (IllegalArgumentException e) {
				try{
					// 尝试name转为数字
					OptionType query = OptionType.values()[Integer.valueOf(name.trim())];
					return toMap(query);
				}catch (IndexOutOfBoundsException e2) {
					throw e2;
				}catch (NumberFormatException e2) {
					throw e;
				}catch (Exception e2) {
					throw e;
				}
			}
		}
	}


}

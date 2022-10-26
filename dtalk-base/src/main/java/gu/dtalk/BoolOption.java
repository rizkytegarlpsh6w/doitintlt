package gu.dtalk;

import java.util.Collections;
import java.util.List;

import com.alibaba.fastjson.TypeReference;

/**
 * 布尔类型选项
 * @author guyadong
 *
 */
public class BoolOption extends BaseOption<Boolean> {

	public BoolOption() {
		super(new TypeReference<Boolean>() {}.getType());
	}
	@Override
	public final OptionType getType() {
		return OptionType.BOOL;
	}
	@Override
	public List<Boolean> getAvaiable() {
		return Collections.emptyList();
	}
}

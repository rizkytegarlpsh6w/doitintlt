package gu.dtalk;

import com.alibaba.fastjson.TypeReference;

/**
 * 数字类型选项基类
 * @author guyadong
 *
 * @param <T> OPTION数据类型
 */
public abstract class BaseNumOption<T extends Number> extends BaseOption<T> {

	public BaseNumOption(T defaultValue) {
		super(new TypeReference<T>() {}.getType());
		setDefaultValue(defaultValue);
	}
}

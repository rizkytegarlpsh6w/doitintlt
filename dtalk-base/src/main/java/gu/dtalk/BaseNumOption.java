package gu.dtalk;

import com.alibaba.fastjson.TypeReference;

public abstract class BaseNumOption<T extends Number> extends BaseOption<T> {

	public BaseNumOption() {
		super(new TypeReference<T>() {}.getType());
	}


}

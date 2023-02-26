package gu.dtalk.event;

import static com.google.common.base.Preconditions.*;

import gu.dtalk.BaseItem;
import gu.dtalk.BaseOption;


/**
 * 选项数据值改变侦听器
 * @author guyadong
 *
 * @param <T> OPTION的数据类型
 */
public abstract class ValueListener<T> extends BaseObserver<ValueChangeEvent<BaseOption<T>>> {

	public ValueListener<T> registerTo(BaseOption<T> option){
		if(option != null){
			option.addListener(this);
		}
		return this;
	}
	@SuppressWarnings("unchecked")
	public void registerTo(BaseItem root){
		checkArgument(root != null);
		if(root instanceof BaseOption){
			registerTo((BaseOption<T>)root);
		}else{
			for (BaseItem child : root.getChilds()) {
				registerTo(child);
			}
		}
	}
}

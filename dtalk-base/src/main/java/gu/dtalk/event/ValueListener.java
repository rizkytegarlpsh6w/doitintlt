package gu.dtalk.event;

import static com.google.common.base.Preconditions.checkNotNull;

import gu.dtalk.BaseItem;
import gu.dtalk.BaseOption;


public abstract class ValueListener<T> extends BaseObserver<ValueChangeEvent<BaseOption<T>>> {

	public ValueListener<T> register(BaseOption<T> option){
		if(option != null){
			option.addListener(this);
		}
		return this;
	}
	@SuppressWarnings("unchecked")
	public void register(BaseItem root){
		checkNotNull(root);
		if(root instanceof BaseOption){
			register((BaseOption<T>)root);
		}else{
			for (BaseItem child : root.getChilds()) {
				register(child);
			}
		}
	}
}

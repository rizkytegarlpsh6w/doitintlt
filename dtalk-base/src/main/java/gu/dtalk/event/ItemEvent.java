package gu.dtalk.event;

import java.util.EventObject;

import gu.dtalk.BaseItem;
import static com.google.common.base.Preconditions.*;

/**
 * 事件父类
 * @author guyadong
 *
 */
public class ItemEvent<T extends BaseItem> extends EventObject {

	private static final long serialVersionUID = 5726171993308275693L;

	public ItemEvent(T source) {
		super(checkNotNull(source,"source is null "));
	}
	@SuppressWarnings("unchecked")
	public T item(){
		return (T) getSource();
	}
}

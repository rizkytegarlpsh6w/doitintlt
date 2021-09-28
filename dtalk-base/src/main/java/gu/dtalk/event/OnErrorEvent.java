package gu.dtalk.event;

import gu.dtalk.BaseItem;

/**
 * 出错(异常)发生事件
 * @author guyadong
 *
 */
public class OnErrorEvent<T extends BaseItem> extends ItemEvent<T> {
	private static final long serialVersionUID = 2695954894269335016L;
	public final Throwable e;
	public final String message;
	public OnErrorEvent(T source, Throwable e) {
		this(source,null==e.getMessage()?e.getClass().getName():e.getMessage(),e);
	}
	public OnErrorEvent(T source, String message) {
		this(source,message,null);
	}
	public OnErrorEvent(T source, String message,Throwable e) {
		super(source);
		this.e = e;
		this.message = null==message?"null":message;
	}

}

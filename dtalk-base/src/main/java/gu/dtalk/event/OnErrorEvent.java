package gu.dtalk.event;

/**
 * 出错(异常)发生事件
 * @author guyadong
 *
 */
public class OnErrorEvent extends ItemEvent {
	private static final long serialVersionUID = 2695954894269335016L;
	public Throwable e;
	public final String message;
	public OnErrorEvent(Object source, Throwable e) {
		this(source,null==e.getMessage()?e.getClass().getName():e.getMessage(),e);
	}
	public OnErrorEvent(Object source, String message) {
		this(source,message,null);
	}
	public OnErrorEvent(Object source, String message,Throwable e) {
		super(source);
		this.e = e;
		this.message = null==message?"null":message;
	}

}

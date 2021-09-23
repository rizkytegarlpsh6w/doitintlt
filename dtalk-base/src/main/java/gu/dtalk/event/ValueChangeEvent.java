package gu.dtalk.event;

/**
 * value改变事件
 * @author guyadong
 *
 * @param <T>
 */
public class ValueChangeEvent<T> extends ItemEvent {

	private static final long serialVersionUID = 8039626618382197982L;
	
	final T value;
	public ValueChangeEvent(Object source,T value) {
		super(source);
		this.value = value;
	}

}

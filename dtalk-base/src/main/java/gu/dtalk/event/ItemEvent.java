package gu.dtalk.event;

import java.util.EventObject;

/**
 * 事件父类
 * @author guyadong
 *
 */
public class ItemEvent extends EventObject {

	private static final long serialVersionUID = 5726171993308275693L;

	public ItemEvent(Object source) {
		super(source);
	}

}

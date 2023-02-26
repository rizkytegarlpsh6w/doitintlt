package gu.dtalk.event;

import gu.dtalk.BaseOption;

/**
 * value改变事件
 * @author guyadong
 *
 * @param <O> OPTION类型
 */
public class ValueChangeEvent<O extends BaseOption<?>> extends ItemEvent<O> {

	private static final long serialVersionUID = 8039626618382197982L;
	
	public ValueChangeEvent(O  source) {
		super(source);
	}

	@SuppressWarnings("unchecked")
	public O option(){
		return (O) getSource();
	}
}

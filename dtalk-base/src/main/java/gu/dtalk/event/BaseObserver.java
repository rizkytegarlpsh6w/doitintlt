package gu.dtalk.event;

import java.util.Observable;
import java.util.Observer;

/**
 * 事件侦听器抽象类
 * @author guyadong
 *
 * @param <E> 事件的参数类型
 */
public abstract class BaseObserver<E extends ItemEvent<?>> implements Observer {

	public BaseObserver() {
	}
	/**
	 * 执行更新事件
	 * @param event 事件
	 */
	protected abstract void doUpdate(E event);
	@SuppressWarnings("unchecked")
	@Override
	public final void update(Observable o, Object arg) {
		E event;
		try {
			// 通过强制类型转时是否抛出异常自动过滤不属于当前侦听器关注的对象
			event = (E)arg;
		} catch (Throwable e) {		
			return;
		}
		doUpdate(event);
	}

}

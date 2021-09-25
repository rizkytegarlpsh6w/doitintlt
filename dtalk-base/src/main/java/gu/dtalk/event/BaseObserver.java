package gu.dtalk.event;

import java.util.Observable;
import java.util.Observer;

/**
 * 事件侦听器抽象类
 * @author guyadong
 *
 * @param <ARG> 事件的参数类型
 */
public abstract class BaseObserver<SRC,ARG> implements Observer {

	public BaseObserver() {
	}
	protected abstract void doUpdte(SRC o,ARG arg);
	@SuppressWarnings("unchecked")
	@Override
	public final void update(Observable o, Object arg) {
		SRC src;
		ARG argument;
		try {
			// 通过强制类型转时是否抛出异常自动过滤不属于当前侦听器关注的对象
			src = (SRC) o;
			argument = (ARG)arg;
		} catch (Exception e) {		
			return;
		}
		doUpdte(src, argument);
	}

}

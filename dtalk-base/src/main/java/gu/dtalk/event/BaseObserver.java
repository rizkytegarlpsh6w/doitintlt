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
		try {
			doUpdte((SRC) o, (ARG) arg);
		} catch (Exception e) {			
		}
	}

}

package gu.dtalk;

import java.util.Map;

import gu.dtalk.exception.InteractiveCmdStartException;

/**
 * 交互设备命令接口
 * @author guyadong
 *
 */
public interface ICmdInteractiveAdapter extends ICmdUnionAdapter {
	/**
	 * 执行设备命令
	 * @param input 以值对(key-value)形式提供的输入参数
	 * @param listener 状态侦听器，用于向管理端发送命令状态
	 * @return 命令返回值，没有返回值则返回{@code null}
	 * @throws InteractiveCmdStartException 当设备命令被拒绝或不支持或其他出错时抛出此异常,通过{@link InteractiveCmdStartException#getStatus() }获取状态类型
	 */
	void apply(Map<String, Object> input,ICmdInteractiveStatusListener listener) throws InteractiveCmdStartException;
	/**
	 * 取消当前执行的设备命令
	 * @throws UnsupportedOperationException 设备命令不支持取消
	 */
	void cancel() throws UnsupportedOperationException;
}
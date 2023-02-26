package gu.dtalk;

/**
 * 命令状态侦听器，用于交互命令向管理端发送命令执行状态
 * @author guyadong
 */
public interface ICmdInteractiveStatusListener{
	/**
	 * 返回设备命令完成进度<br>
	 * 设备命令在执行过程中应该定时调用此方法，以作为心跳发送给管理端，直到任务结束
	 * @param progress 完成进度(0-100),可为{@code null}
	 * @param statusMessage 附加状态消息,可为{@code null}
	 */
	void onProgress(Integer progress,String statusMessage);
	/**
	 * 任务结束,设备命令成功执行完成
	 * @param value 命令执行返回值,没有返回值则为{@code null}
	 */
	void onFinished(Object value);
	/**
	 * 任务结束,执行中的设备命令被取消
	 */
	void onCaneled();
	/**
	 * 任务结束,调用抛出异常
	 * @param errorMessage 错误信息，可为{@code null}
	 * @param throwable 异常对象，可为{@code null}
	 */
	void onError(String errorMessage, Throwable throwable);
	/**
	 * 此方法用于设备命令发送方控制设备端定时报告进度的间隔(秒)，
	 * 设备端调用此方法获取数值后，用于控制调用{@link #onProgress(Integer, String)}方法的调用间隔
	 * @return 返回要求的{@link #onProgress(Integer, String)}方法调用间隔(秒)，{@code <=0}时，使用设备自定义的默认值
	 */
	int getProgressInternal();
}
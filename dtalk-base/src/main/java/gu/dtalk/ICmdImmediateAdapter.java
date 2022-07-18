package gu.dtalk;

import java.util.Map;

import gu.dtalk.exception.CmdExecutionException;

/**
 * 立即执行设备命令执行接口
 * @author guyadong
 *
 */
public interface ICmdImmediateAdapter  extends ICmdUnionAdapter{
	/**
	 * 执行设备命令
	 * @param input 以值对(key-value)形式提供的输入参数
	 * @return 命令返回值，没有返回值则返回{@code null}
	 * @throws CmdExecutionException 命令执行失败
	 */
	Object apply(Map<String, Object> input) throws CmdExecutionException;
}

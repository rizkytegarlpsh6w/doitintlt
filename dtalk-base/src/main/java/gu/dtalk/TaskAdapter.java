package gu.dtalk;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import gu.dtalk.Ack.Status;
import gu.dtalk.CmdItem.ICmdAdapter;
import gu.dtalk.exception.CmdExecutionException;
import gu.dtalk.exception.UnsupportCmdException;
import gu.simplemq.Channel;
import gu.simplemq.IMessageAdapter;
import gu.simplemq.exceptions.SmqUnsubscribeException;
import gu.simplemq.redis.RedisFactory;

/**
 * 任务执行对象<br>
 * 将{@link ICmdAdapter}实例封装为执行队列任务的{@link IMessageAdapter}<br>
 * 调用{@link #register()}将当前对象注册到队列<br>
 * 调用{@link #unregister()}从队列注册将当前对象<br>
 * @author guyadong
 *
 */
public class TaskAdapter implements IMessageAdapter<Map<String, Object>>{
	/** 任务响应频道名 */
	public static final String P_TASK_ACK = "taskAck";
	/** 任务序列号 */
	public static final String P_TASK_ID = "taskId";
	private final Channel<Map<String, Object>> channel;
	private ICmdAdapter cmdAdapter;
	/** 执行publish的线程池对象 */
	protected static final ExecutorService publishExecutor = MoreExecutors.getExitingExecutorService(
			new ThreadPoolExecutor(1, 1,
	                0L, TimeUnit.MILLISECONDS,
	                new LinkedBlockingQueue<Runnable>(),
	                new ThreadFactoryBuilder().setNameFormat("task-ack-publish-%d").build()));
	/**
	 * @param queue 队列名称
	 */
	public TaskAdapter(String queue) {
		channel = new Channel<Map<String, Object>>(queue,this ){};
	}
	/**
	 * 处理收到的任务包
	 * @see gu.simplemq.IMessageAdapter#onSubscribe(java.lang.Object)
	 */
	@Override
	public final void onSubscribe(Map<String, Object> parameter) throws SmqUnsubscribeException {
		parameter = MoreObjects.firstNonNull(parameter, Collections.<String, Object>emptyMap());
		// 返回值
		Object res = null;
		// 抛出异常
		Exception err = null;
		try {
			if(cmdAdapter == null){
				throw new UnsupportCmdException("UNSUPPORTED TASK");
			}
			res = cmdAdapter.apply(parameter);
		} catch (CmdExecutionException e) {
			err = e;
		} catch (UnsupportCmdException e) {
			err = e;
		}
		
		Object tack = parameter.get(P_TASK_ACK);
		Object tid = parameter.get(P_TASK_ID);
		// 如果命令参数有提供响应频道名则将命令执行情况通过Ack对象发送到指定的频道
		if(tack instanceof String){
			final String ackChannel = (String) tack;
			if(!Strings.isNullOrEmpty(ackChannel)){
				Object ack = makeAck(res,err,ackChannel, (tid instanceof Number)? (Number)tid:-1);
				publish(ackChannel,ack);
			}
		}
	}
	/**
	 * 创建响应消息对象
	 * @param res 设备命令执行结果,对于没有返回值的命令为{@code null}
	 * @param err 设备命令执行异常
	 * @param ackChannel 设备命令响应频道
	 * @param taskid 任务序列号
	 * @return 返回响应消息对象
	 */
	@SuppressWarnings("unchecked")
	protected <T, ACK>ACK makeAck(T res,Exception err,String ackChannel, Number taskid) {
		final Ack<T> ack = new Ack<T>()
				.setStatus(Status.OK)
				.setValue(res)
				.setCmdSn(taskid.longValue());

		if(err != null){
			ack.setStatus(Status.ERROR).setStatusMessage(err.getMessage());
		}
		return (ACK) ack;
	}
	/**
	 * 返回队列名称
	 * @return
	 */
	public String getQueue(){
		return channel.name;
	}
	/**
	 * 注册当前对象到{@link #channel}指定的队列
	 * @return 当前对象
	 */
	public final TaskAdapter register(){
		RedisFactory.getConsumer().register(channel);
		return this;
	}
	/**
	 * 将当前对象从{@link #channel}指定的队列注销
	 * @return 当前对象
	 */
	public final TaskAdapter unregister(){
		RedisFactory.getConsumer().unregister(channel);
		return this;
	}
	/**
	 * @return cmdAdapter
	 */
	public ICmdAdapter getCmdAdapter() {
		return cmdAdapter;
	}
	/**
	 * @param cmdAdapter 要设置的 cmdAdapter
	 * @return 当前对象
	 */
	public TaskAdapter setCmdAdapter(ICmdAdapter cmdAdapter) {
		this.cmdAdapter = cmdAdapter;
		return this;
	}
	/**
	 * 向{@code ackChannel}指定的频道发送消息响应对象{@code ack}
	 * @param ackChannel
	 * @param ack
	 */
	private <T>void publish(final String ackChannel,final T ack){
		if(ack != null){
			publishExecutor.execute(new Runnable() {
				@Override
				public void run() {
					Channel<T> ch = new Channel<T>(ackChannel,ack.getClass());
					RedisFactory.getPublisher().publish(ch, ack);						
				}
			});
		}
	}
}
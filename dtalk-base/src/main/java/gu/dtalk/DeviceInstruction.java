package gu.dtalk;

import java.util.List;
import java.util.Map;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * 设备命令数据<br>
 * 此类对象将做为设备命令经redis服务器发送到设备端,
 * 数据发到redis服务器以及设备端从redis服务器收到数据的过程要经过JSON序列化和反序列化<br>
 * @author guyadong
 *
 */
public class DeviceInstruction{
	private String cmdpath;
	private long cmdSn;
	private List<Integer> target;
	private boolean group;
	private String ackChannel;
	private Map<String, Object> parameters;
	public DeviceInstruction() {
	}

	public String getCmdpath() {
		return cmdpath;
	}
	public DeviceInstruction setCmdpath(String cmdpath) {
		this.cmdpath = cmdpath;
		return this;
	}
	/** 设置要执行的设备命令类型 */
	public long getCmdSn() {
		return cmdSn;
	}
	/**
	 * 设置唯一的命令序列号
	 * @param cmdSn
	 * @return 当前对象
	 */
	public DeviceInstruction setCmdSn(long cmdSn) {
		this.cmdSn = cmdSn;
		return this;
	}
	
	public List<Integer> getTarget() {
		return target;
	}

	/**
	 * 设置执行命令的设备组或设备ID,类型由{@link #setGroup(boolean)}确定
	 * @param target
	 * @return 当前对象
	 */
	public DeviceInstruction setTarget(List<Integer> target) {
		// 过滤掉所有为null元素
		this.target = target == null ? null : Lists.newLinkedList(Iterables.filter(target, Predicates.notNull()));
		return this;
	}
	/**
	 * 指定设备命令的目标ID及目标类型(设备/设备组)
	 * @param target
	 * @param group
	 * @return
	 * @see #setTarget(List)
	 * @see #setGroup(boolean)
	 */
	public DeviceInstruction setTarget(List<Integer> target,boolean group) {
		setTarget(target);
		setGroup(group);
		return this;
	}
	public boolean isGroup() {
		return group;
	}
	/**
	 * 指定目标类型为设备组或设备{@link #setTarget(List)}
	 * @param group 目标类型:{@code true}:设备组,{@code false}:设备
	 * @return 当前对象
	 */
	public DeviceInstruction setGroup(boolean group) {
		this.group = group;
		return this;
	}
	public String getAckChannel() {
		return ackChannel;
	}
	/**
	 * 设置用于接收设备命令响应的通道,
	 * 如果不指定命令响应通道,则命令发送方法无法知道命令执行状态,
	 * 每一次设备命令发送都应该有一个唯一的命令响应接受通道,以便于命令发送方区命令响应来源
	 * @param ackChannel
	 * @return 当前对象
	 */
	public DeviceInstruction setAckChannel(String ackChannel) {
		this.ackChannel = ackChannel;
		return this;
	}
	/**
	 * @return 返回设备命令参数
	 */
	public Map<String, Object> getParameters() {
		return parameters;
	}
	/**
	 * 
	 * 设置设备命令参数
	 * @param parameters
	 * @return 当前对象
	 */
	public DeviceInstruction setParameters(Map<String,Object> parameters) {
		this.parameters = parameters;
		return this;
	}
	/**
	 * 设置设备命令参数
	 * @param key
	 * @param value
	 * @return 当前对象
	 */
	public DeviceInstruction withParameters(String key,Object value) {
		if(this.parameters == null){
			this.parameters = Maps.newHashMap();
		}
		this.parameters.put(key, value);
		return this;
	}
}

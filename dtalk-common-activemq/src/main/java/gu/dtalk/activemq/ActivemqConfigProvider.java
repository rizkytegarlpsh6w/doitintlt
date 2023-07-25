package gu.dtalk.activemq;

import java.util.Properties;

import gu.simplemq.activemq.ActivemqProperties;

/**
 * ActiveMQ 服务器参数SPI(Service Provider Interface)读写接口
 * 必须定义服务地址,否则视为无效实例被忽略
 * @author guyadong
 *
 */
public interface ActivemqConfigProvider {
	/**
	 * @return 返回ActiveMQ连接配置,可返回空,不可返回{@code null}
	 */
	ActivemqProperties getProperties();
	/**
	 * 设置ActiveMQ连接配置
	 * @param properties 
	 */
	void setProperties(Properties properties);
	/**
	 * 设置activemq连接参数
	 * @param name 参数名
	 * @param value 参数值
	 */
	void setProperty(String name,String value);
	/**
	 * 读取activemq连接参数
	 * @param name 参数名
	 * @return 参数值,没有找到返回{@code null}
	 */
	String getProperty(String name);
	/**
	 * 读取activemq连接参数
	 * @param name 参数名
	 * @param defaultValue 默认值
	 * @return 参数值,没有找到返回默认值
	 */
	String getProperty(String name,String defaultValue);
	/**
	 * 返回当前配置的连接类型,不可为{@code null}
	 * @return 连接类型
	 */
	ActivemqConfigType type();
}

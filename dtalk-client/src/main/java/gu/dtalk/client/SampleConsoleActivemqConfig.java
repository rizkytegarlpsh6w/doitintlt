package gu.dtalk.client;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.google.common.base.Strings;
import com.google.common.net.HostAndPort;

import gu.dtalk.OptionType;
import gu.dtalk.activemq.DefaultCustomActivemqConfigProvider;
import gu.simplemq.Constant;
import gu.simplemq.MQProperties;
import gu.simplemq.MessageQueueType;
import gu.simplemq.utils.URISupport;
import net.gdface.cli.BaseAppConfig;
import static redis.clients.jedis.Protocol.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static gu.dtalk.client.SampleConsole.run;
import static net.gdface.utils.ConditionChecks.checkTrue;
import static gu.dtalk.activemq.ActivemqContext.HELPER;
import static gu.dtalk.activemq.ActivemqContext.CONST_PROVIDER;

/**
 * 终端命令行配置参数
 * @author guyadong
 *
 */
public class SampleConsoleActivemqConfig extends BaseAppConfig implements SampleConsoleConstants,Constant {
	@SuppressWarnings("serial")
	private static final HashMap<String, Object> CONSTANTS = 
		new HashMap<String, Object>(){{put(IMPL_TYPE, MessageQueueType.ACTIVEMQ);}};
	private final MQProperties activemqProperties = HELPER.initParameters(null);

	private String mac;
	private SampleConsoleActivemqConfig() {
		super(true);
		options.addOption(Option.builder().longOpt(AMQ_HOST_OPTION_LONG)
				.desc(AMQ_HOST_OPTION_DESC + CONST_PROVIDER.getDefaultHost()).numberOfArgs(1).build());

		options.addOption(Option.builder().longOpt(AMQ_PORT_OPTION_LONG)
				.desc(AMQ_PORT_OPTION_DESC + CONST_PROVIDER.getDefaultPort()).numberOfArgs(1).type(Number.class).build());

		options.addOption(Option.builder().longOpt(AMQ_PWD_OPTION_LONG)
				.desc(AMQ_PWD_OPTION_DESC).numberOfArgs(1).build());

		options.addOption(Option.builder().longOpt(AMQ_URI_OPTION_LONG)
				.desc(AMQ_URI_OPTION_DESC).numberOfArgs(1).build());

		options.addOption(Option.builder().longOpt(AMQ_TIMEOUT_OPTION_LONG)
				.desc(AMQ_TIMEOUT_OPTION_DESC + DEFAULT_TIMEOUT + " ms.").numberOfArgs(1).type(Number.class).build());

		options.addOption(Option.builder().longOpt(DEVICE_MAC_OPTION_LONG)
				.desc(DEVICE_MAC_OPTION_DESC ).numberOfArgs(1).build());

		options.addOption(Option.builder().longOpt(MQTT_OPTION_LONG).desc(MQTT_OPTION_DESC).optionalArg(true).numberOfArgs(1).build());
		
		defaultValue.setProperty(AMQ_HOST_OPTION_LONG, activemqProperties.get(MQ_HOST));
		defaultValue.setProperty(AMQ_PORT_OPTION_LONG, activemqProperties.get(MQ_PORT));
		defaultValue.setProperty(AMQ_PWD_OPTION_LONG, activemqProperties.get(MQ_PASSWORD));
		defaultValue.setProperty(AMQ_URI_OPTION_LONG, activemqProperties.get(MQ_URI));
		defaultValue.setProperty(AMQ_TIMEOUT_OPTION_LONG, activemqProperties.get(MQ_TIMEOUT));
		defaultValue.setProperty(DEVICE_MAC_OPTION_LONG, "");

	}
	@Override
	public void loadConfig(Options options, CommandLine cmd) throws ParseException {
		super.loadConfig(options, cmd);
		
		if(hasProperty(MQ_HOST)){
			activemqProperties.setProperty(MQ_HOST, (String) getProperty(AMQ_HOST_OPTION_LONG));
		}
		if(hasProperty(AMQ_PORT_OPTION_LONG)){
			activemqProperties.setProperty(MQ_PORT, (String) getProperty(AMQ_PORT_OPTION_LONG));
		}
		if(hasProperty(AMQ_PWD_OPTION_LONG)){
			activemqProperties.setProperty(MQ_PASSWORD, (String) getProperty(AMQ_PWD_OPTION_LONG));
		}
		if(hasProperty(AMQ_URI_OPTION_LONG)){
			activemqProperties.setProperty(MQ_URI, (String) getProperty(AMQ_URI_OPTION_LONG));
		}
		if(hasProperty(AMQ_TIMEOUT_OPTION_LONG)){
			activemqProperties.setProperty(MQ_TIMEOUT, (String) getProperty(AMQ_TIMEOUT_OPTION_LONG));
		}
		this.mac = (String) getProperty(DEVICE_MAC_OPTION_LONG);
		if(!Strings.isNullOrEmpty(this.mac)){
			// 检查输入的mac地址字符串是否符合格式要求
			checkTrue(OptionType.MAC.strValidator.apply(this.mac),
					ParseException.class,"INVALID MAC address %s",this.mac);
			this.mac = this.mac.replaceAll("[:-]", "");
		}
		
		parseMQTTOption();

	}
	
	private void parseMQTTOption() throws ParseException{
		if(hasProperty(MQTT_OPTION_LONG)){
			Object v = getProperty(MQTT_OPTION_LONG);
			activemqProperties.setProperty(MQ_PUBSUB_MQTT, "true");
			if(v instanceof String){
				String value =  (String) v;
				try {
					// 尝试解析为端口号(整数)
					int port = Integer.parseInt(value);
					checkArgument(port > 0,"INVALID option %s:%s,as port ,>0 required", MQTT_OPTION_LONG, port);
					activemqProperties.setProperty(MQ_PUBSUB_URI, String.format("mqtt://%s:%d",CONST_PROVIDER.getDefaultHost(), port));
				} catch (NumberFormatException e) {
					HostAndPort hostAndPort;
					try {
						// 尝试解析为 HOST:PORT OR HOST 格式
						// 如果为 HOST 格式,则端口号使用默认值
						hostAndPort = HostAndPort.fromString(value).withDefaultPort(DEFAULT_MQTT_PORT);	
					} catch (IllegalArgumentException e2) {
						try {
							// 尝试解析为URI
							URI u = new URI(value);
							// 强制修改schema为mqtt,
							URI r = URISupport.changeScheme(u, "mqtt");
							activemqProperties.setProperty(MQ_PUBSUB_URI, r.toString());
							return ;
						} catch (URISyntaxException e1) {
							throw new ParseException(String.format("INVALID value %s for option %s", value, MQTT_OPTION_LONG));
						}
					}
					activemqProperties.setProperty(MQ_PUBSUB_URI, String.format("mqtt://%s:%d",hostAndPort.getHost(), hostAndPort.getPort()));
					return;
				}
			}else {
				// v 为 true
				checkState(Boolean.TRUE.equals(v),"INVALID VALUE TYPE FOR %s=%s",MQTT_OPTION_LONG,v);
				// 参数个数为0时,默认使用 DEFAULT_MQTT_CONNECTOR
				activemqProperties.setProperty(MQ_PUBSUB_URI, DEFAULT_MQTT_CONNECTOR);
			}
		}
	}
	/**
	 * @return activemqProperties
	 */
	public MQProperties getActivemqParameters() {
		return activemqProperties;
	}
	/**
	 * @return 目标设备MAC地址
	 */
	public String getMac() {
		return mac;
	}

	@Override
	protected String getAppName() {
		return SampleConsole.class.getSimpleName();
	}
	@Override
	protected String getHeader() {
		return "Text terminal for Dtalk Device(Dtalk设备交互字符终端)";
	}
	@Override
	protected void doAfterParse() {
		DefaultCustomActivemqConfigProvider.INIT_PROPERTIES.init(activemqProperties);
	}
	@Override
	protected Map<String, Object> doGetConstants() {
		return CONSTANTS;
	}
	public static void main(String []args){
		run(new SampleConsoleActivemqConfig(), args);
	}
}

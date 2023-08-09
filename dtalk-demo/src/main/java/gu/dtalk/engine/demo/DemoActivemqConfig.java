package gu.dtalk.engine.demo;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import gu.dtalk.activemq.DefaultCustomActivemqConfigProvider;
import gu.simplemq.MessageQueueType;
import gu.simplemq.activemq.ActivemqConstants;
import gu.simplemq.activemq.ActivemqProperties;
import gu.simplemq.activemq.ActivemqUtils;
import net.gdface.cli.BaseAppConfig;

import static gu.dtalk.engine.demo.Demo.run;
import static redis.clients.jedis.Protocol.*;

import java.util.HashMap;
import java.util.Map;
/**
 * 终端(ACTIVEMQ)命令行配置参数
 * @author guyadong
 *
 */
public class DemoActivemqConfig extends BaseAppConfig implements DemoConstants,ActivemqConstants {
	@SuppressWarnings("serial")
	private static final HashMap<String, Object> CONSTANTS = 
		new HashMap<String, Object>(){{put(IMPL_TYPE, MessageQueueType.ACTIVEMQ);}};
	private final ActivemqProperties activemqProperties = ActivemqUtils.initParameters(null);

	DemoActivemqConfig() {
		super(true);
		options.addOption(Option.builder().longOpt(AMQ_HOST_OPTION_LONG)
				.desc(AMQ_HOST_OPTION_DESC + DEFAULT_HOST).numberOfArgs(1).build());

		options.addOption(Option.builder().longOpt(AMQ_PORT_OPTION_LONG)
				.desc(AMQ_PORT_OPTION_DESC + DEFAULT_PORT).numberOfArgs(1).type(Number.class).build());

		options.addOption(Option.builder().longOpt(AMQ_PWD_OPTION_LONG)
				.desc(AMQ_PWD_OPTION_DESC).numberOfArgs(1).build());

		options.addOption(Option.builder().longOpt(AMQ_URI_OPTION_LONG)
				.desc(AMQ_URI_OPTION_DESC).numberOfArgs(1).build());

		options.addOption(Option.builder().longOpt(AMQ_TIMEOUT_OPTION_LONG)
				.desc(AMQ_TIMEOUT_OPTION_DESC + DEFAULT_TIMEOUT + " ms.").numberOfArgs(1).type(Number.class).build());

		options.addOption(Option.builder(TRACE_OPTION).longOpt(TRACE_OPTION_LONG)
				.desc(TRACE_OPTION_DESC ).hasArg(false).build());
		
		defaultValue.setProperty(AMQ_HOST_OPTION_LONG, activemqProperties.get(ACON_HOST));
		defaultValue.setProperty(AMQ_PORT_OPTION_LONG, activemqProperties.get(ACON_PORT));
		defaultValue.setProperty(AMQ_PWD_OPTION_LONG, activemqProperties.get(ACON_PASSWORD));
		defaultValue.setProperty(AMQ_URI_OPTION_LONG, activemqProperties.get(ACON_URI));
		defaultValue.setProperty(AMQ_TIMEOUT_OPTION_LONG, activemqProperties.get(ACON_sendTimeout));

	}
	@Override
	public void loadConfig(Options options, CommandLine cmd) throws ParseException {
		super.loadConfig(options, cmd);
		if(hasProperty(ACON_HOST)){
			activemqProperties.setProperty(ACON_HOST, (String) getProperty(AMQ_HOST_OPTION_LONG));
		}
		if(hasProperty(AMQ_PORT_OPTION_LONG)){
			activemqProperties.setProperty(ACON_PORT, (String) getProperty(AMQ_PORT_OPTION_LONG));
		}
		if(hasProperty(ACON_PASSWORD)){
			activemqProperties.setProperty(ACON_PASSWORD, (String) getProperty(AMQ_PWD_OPTION_LONG));
		}
		if(hasProperty(AMQ_URI_OPTION_LONG)){
			activemqProperties.setProperty(ACON_URI, (String) getProperty(AMQ_URI_OPTION_LONG));
		}
		if(hasProperty(AMQ_TIMEOUT_OPTION_LONG)){
			activemqProperties.setProperty(ACON_sendTimeout, (String) getProperty(AMQ_TIMEOUT_OPTION_LONG));
		}
	}
	/**
	 * @return redisParameters
	 */
	public ActivemqProperties getActivemqParameters() {
		return activemqProperties;
	}
	@Override
	protected String getAppName() {
		return Demo.class.getSimpleName();
	}
	@Override
	protected String getHeader() {
		return "Device talk Demo starting(设备模拟器)";
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
		run(new DemoActivemqConfig(), args);
	}
}

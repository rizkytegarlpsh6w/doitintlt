package gu.dtalk.client;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.google.common.base.Strings;
import gu.dtalk.OptionType;
import gu.dtalk.activemq.DefaultCustomActivemqConfigProvider;
import gu.simplemq.MessageQueueType;
import gu.simplemq.activemq.ActivemqConstants;
import gu.simplemq.activemq.ActivemqProperties;
import gu.simplemq.activemq.ActivemqUtils;
import net.gdface.cli.BaseAppConfig;
import static redis.clients.jedis.Protocol.*;

import java.util.HashMap;
import java.util.Map;

import static gu.dtalk.client.SampleConsole.run;
import static net.gdface.utils.ConditionChecks.checkTrue;

/**
 * 终端命令行配置参数
 * @author guyadong
 *
 */
public class SampleConsoleActivemqConfig extends BaseAppConfig implements SampleConsoleConstants,ActivemqConstants {
	@SuppressWarnings("serial")
	private static final HashMap<String, Object> CONSTANTS = 
		new HashMap<String, Object>(){{put(IMPL_TYPE, MessageQueueType.ACTIVEMQ);}};
	private final ActivemqProperties activemqProperties = ActivemqUtils.initParameters(null);

	private String mac;
	private SampleConsoleActivemqConfig() {
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

		options.addOption(Option.builder().longOpt(DEVICE_MAC_OPTION_LONG)
				.desc(DEVICE_MAC_OPTION_DESC ).numberOfArgs(1).build());

		defaultValue.setProperty(AMQ_HOST_OPTION_LONG, activemqProperties.get(ACON_HOST));
		defaultValue.setProperty(AMQ_PORT_OPTION_LONG, activemqProperties.get(ACON_PORT));
		defaultValue.setProperty(AMQ_PWD_OPTION_LONG, activemqProperties.get(ACON_PASSWORD));
		defaultValue.setProperty(AMQ_URI_OPTION_LONG, activemqProperties.get(ACON_URI));
		defaultValue.setProperty(AMQ_TIMEOUT_OPTION_LONG, activemqProperties.get(ACON_sendTimeout));
		defaultValue.setProperty(DEVICE_MAC_OPTION_LONG, "");

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
		this.mac = (String) getProperty(DEVICE_MAC_OPTION_LONG);
		if(!Strings.isNullOrEmpty(this.mac)){
			// 检查输入的mac地址字符串是否符合格式要求
			checkTrue(OptionType.MAC.strValidator.apply(this.mac),
					ParseException.class,"INVALID MAC address %s",this.mac);
			this.mac = this.mac.replaceAll("[:-]", "");
		}
	}
	/**
	 * @return activemqProperties
	 */
	public ActivemqProperties getActivemqParameters() {
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

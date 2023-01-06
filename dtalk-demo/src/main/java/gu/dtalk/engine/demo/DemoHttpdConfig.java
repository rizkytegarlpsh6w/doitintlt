package gu.dtalk.engine.demo;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import gu.dtalk.CommonConstant;
import net.gdface.cli.BaseAppConfig;
import static fi.iki.elonen.NanoHTTPD.*;
/**
 * 终端命令行配置参数
 * @author guyadong
 *
 */
public class DemoHttpdConfig extends BaseAppConfig implements DemoConstants {
	static final DemoHttpdConfig DEMO_CONFIG = new DemoHttpdConfig();
	
	private int port = CommonConstant.DEFAULT_HTTP_PORT;
	private int timeout = SOCKET_READ_TIMEOUT; 
	private boolean trace;

	public DemoHttpdConfig() {

		options.addOption(Option.builder().longOpt(HTTP_PORT_OPTION_LONG)
				.desc(HTTP_PORT_OPTION_DESC + port).numberOfArgs(1).type(Number.class).build());


		options.addOption(Option.builder().longOpt(HTTP_TIMEOUT_OPTION_LONG)
				.desc(HTTP_TIMEOUT_OPTION_DESC + timeout + " ms.").numberOfArgs(1).type(Number.class).build());

		options.addOption(Option.builder(TRACE_OPTION).longOpt(TRACE_OPTION_LONG)
				.desc(TRACE_OPTION_DESC ).hasArg(false).build());

		
		defaultValue.setProperty(HTTP_PORT_OPTION_LONG, port);
		defaultValue.setProperty(HTTP_TIMEOUT_OPTION_LONG, timeout);

	}
	@Override
	public void loadConfig(Options options, CommandLine cmd) throws ParseException {
		super.loadConfig(options, cmd);
		if(hasProperty(HTTP_PORT_OPTION_LONG)){
			port=((Number)getProperty(HTTP_PORT_OPTION_LONG)).intValue();
		}
		if(hasProperty(HTTP_TIMEOUT_OPTION_LONG)){
			timeout =  ((Number)getProperty(HTTP_TIMEOUT_OPTION_LONG)).intValue();
		}
		this.trace = getProperty(TRACE_OPTION_LONG);
		
	}
	/**
	 * @return 发生异常时是否输出详细堆栈信息
	 */
	public boolean isTrace() {
		return trace;
	}
	@Override
	protected String getAppName() {
		return Demo.class.getSimpleName();
	}
	@Override
	protected String getHeader() {
		return "Device talk Demo starting(设备模拟器HTTP)";
	}
	/**
	 * @return port
	 */
	public int getPort() {
		return port;
	}
	/**
	 * @return timeout
	 */
	public int getTimeout() {
		return timeout;
	}
}

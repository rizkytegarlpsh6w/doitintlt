package gu.dtalk.engine.demo;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.google.common.base.Predicates;
import com.google.common.collect.Maps;

import gu.simplemq.redis.JedisPoolLazy;
import gu.simplemq.redis.JedisPoolLazy.PropName;
import net.gdface.cli.BaseAppConfig;
import static redis.clients.jedis.Protocol.*;

import java.net.URI;
import java.util.Map;
/**
 * 终端命令行配置参数
 * @author guyadong
 *
 */
public class DemoConfig extends BaseAppConfig implements DemoConstants {
	static final DemoConfig DEMO_CONFIG = new DemoConfig();
	private final Map<PropName, Object> redisParameters = JedisPoolLazy.initParameters(null);

	private boolean trace;
	public DemoConfig() {
		options.addOption(Option.builder().longOpt(REDIS_HOST_OPTION_LONG)
				.desc(REDIS_HOST_OPTION_DESC + DEFAULT_HOST).numberOfArgs(1).build());

		options.addOption(Option.builder().longOpt(REDIS_PORT_OPTION_LONG)
				.desc(REDIS_PORT_OPTION_DESC + DEFAULT_PORT).numberOfArgs(1).type(Number.class).build());

		options.addOption(Option.builder().longOpt(REDIS_PWD_OPTION_LONG)
				.desc(REDIS_PWD_OPTION_DESC).numberOfArgs(1).build());

		options.addOption(Option.builder().longOpt(REDIS_DB_OPTION_LONG)
				.desc(REDIS_DB_OPTION_DESC + DEFAULT_DATABASE).numberOfArgs(1).type(Number.class).build());

		options.addOption(Option.builder().longOpt(REDIS_URI_OPTION_LONG)
				.desc(REDIS_URI_OPTION_DESC).numberOfArgs(1).type(URI.class).build());

		options.addOption(Option.builder().longOpt(REDIS_TIMEOUT_OPTION_LONG)
				.desc(REDIS_TIMEOUT_OPTION_DESC + DEFAULT_TIMEOUT + " ms.").numberOfArgs(1).type(Number.class).build());

		options.addOption(Option.builder(TRACE_OPTION).longOpt(TRACE_OPTION_LONG)
				.desc(TRACE_OPTION_DESC ).hasArg(false).build());

		options.addOption(Option.builder().longOpt(CONNEC_PWD_OPTION_LONG)
				.desc(CONNEC_PWD_OPTION_DESC ).numberOfArgs(1).build());
		
		defaultValue.setProperty(REDIS_HOST_OPTION_LONG, null);
		defaultValue.setProperty(REDIS_PORT_OPTION_LONG, redisParameters.get(PropName.port));
		defaultValue.setProperty(REDIS_PWD_OPTION_LONG, null);
		defaultValue.setProperty(REDIS_DB_OPTION_LONG, redisParameters.get(PropName.database));
		defaultValue.setProperty(REDIS_URI_OPTION_LONG, null);
		defaultValue.setProperty(REDIS_TIMEOUT_OPTION_LONG, redisParameters.get(PropName.timeout));
		defaultValue.setProperty(CONNEC_PWD_OPTION_LONG, "");

	}
	@Override
	public void loadConfig(Options options, CommandLine cmd) throws ParseException {
		super.loadConfig(options, cmd);
		redisParameters.put(PropName.host, getProperty(REDIS_HOST_OPTION_LONG));
		redisParameters.put(PropName.port, getProperty(REDIS_PORT_OPTION_LONG));
		redisParameters.put(PropName.password, getProperty(REDIS_PWD_OPTION_LONG));
		redisParameters.put(PropName.database, getProperty(REDIS_DB_OPTION_LONG));
		redisParameters.put(PropName.uri, getProperty(REDIS_URI_OPTION_LONG));
		redisParameters.put(PropName.timeout, getProperty(REDIS_TIMEOUT_OPTION_LONG));		
		this.trace = getProperty(TRACE_OPTION_LONG);
		
	}
	/**
	 * @return redisParameters
	 */
	public Map<PropName, Object> getRedisParameters() {
		return Maps.filterValues(redisParameters, Predicates.notNull());
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
		return "Device talk Demo starting(设备模拟器)";
	}
}

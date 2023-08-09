package gu.dtalk.engine.demo;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.google.common.base.Predicates;
import com.google.common.collect.Maps;

import gu.simplemq.MessageQueueType;
import gu.simplemq.redis.JedisPoolLazy.PropName;
import gu.dtalk.redis.DefaultCustomRedisConfigProvider;
import gu.simplemq.redis.JedisUtils;
import net.gdface.cli.BaseAppConfig;

import static redis.clients.jedis.Protocol.*;
import static gu.dtalk.engine.demo.Demo.run;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
/**
 * 终端(REDIS)命令行配置参数
 * @author guyadong
 *
 */
public class DemoRedisConfig extends BaseAppConfig implements DemoConstants {
	@SuppressWarnings("serial")
	private static final HashMap<String, Object> CONSTANTS = 
		new HashMap<String, Object>(){{put(IMPL_TYPE, MessageQueueType.REDIS);}};
	private final Map<PropName, Object> redisParameters = JedisUtils.initParameters(null);

	DemoRedisConfig() {
		super(true);
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
		
		defaultValue.setProperty(REDIS_HOST_OPTION_LONG, null);
		defaultValue.setProperty(REDIS_PORT_OPTION_LONG, redisParameters.get(PropName.port));
		defaultValue.setProperty(REDIS_PWD_OPTION_LONG, null);
		defaultValue.setProperty(REDIS_DB_OPTION_LONG, redisParameters.get(PropName.database));
		defaultValue.setProperty(REDIS_URI_OPTION_LONG, null);
		defaultValue.setProperty(REDIS_TIMEOUT_OPTION_LONG, redisParameters.get(PropName.timeout));
		
	}
	@Override
	public void loadConfig(Options options, CommandLine cmd) throws ParseException {
		super.loadConfig(options, cmd);
		redisParameters.put(PropName.host, getProperty(REDIS_HOST_OPTION_LONG));
		if(hasProperty(REDIS_PORT_OPTION_LONG)){
			redisParameters.put(PropName.port, ((Number)getProperty(REDIS_PORT_OPTION_LONG)).intValue());
		}
		redisParameters.put(PropName.password, getProperty(REDIS_PWD_OPTION_LONG));
		if(hasProperty(REDIS_DB_OPTION_LONG)){
			redisParameters.put(PropName.database, ((Number)getProperty(REDIS_DB_OPTION_LONG)).intValue());
		}
		redisParameters.put(PropName.uri, getProperty(REDIS_URI_OPTION_LONG));
		if(hasProperty(REDIS_TIMEOUT_OPTION_LONG)){
			redisParameters.put(PropName.timeout, ((Number)getProperty(REDIS_TIMEOUT_OPTION_LONG)).intValue());
		}
	}
	/**
	 * @return redisParameters
	 */
	public Map<PropName, Object> getRedisParameters() {
		return Maps.filterValues(redisParameters, Predicates.notNull());
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
		DefaultCustomRedisConfigProvider.initredisParameters(getRedisParameters());
	}
	@Override
	protected Map<String, Object> doGetConstants() {
		return CONSTANTS;
	}
	public static void main(String []args){
		run(new DemoRedisConfig(), args);
	}
}

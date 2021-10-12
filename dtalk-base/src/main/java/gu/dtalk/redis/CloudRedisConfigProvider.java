package gu.dtalk.redis;

/**
 * 仅有云配置
 * @author guyadong
 *
 */
public class CloudRedisConfigProvider implements RedisConfigProvider {

	@Override
	public String getHost() {
		return "dtalk.gdface.net";
	}

	@Override
	public void setHost(String host) {

	}

	@Override
	public int getPort() {
		return 0;
	}

	@Override
	public void setPort(int port) {

	}

	@Override
	public String getPassword() {
		return null;
	}

	@Override
	public void setPassword(String password) {

	}

	@Override
	public int getDatabase() {
		return 0;
	}

	@Override
	public void setDatabase(int database) {

	}

	@Override
	public long getTimeout() {
		return 0;
	}

	@Override
	public void setTimeout(long timeout) {

	}
	@Override
	public final RedisConfigType type(){
		return RedisConfigType.CLOUD;
	}
}

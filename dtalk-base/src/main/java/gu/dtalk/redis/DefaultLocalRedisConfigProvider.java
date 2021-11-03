package gu.dtalk.redis;

import java.net.URI;

/**
 * 局域网配置
 * @author guyadong
 *
 */
public class DefaultLocalRedisConfigProvider implements RedisConfigProvider {

	@Override
	public String getHost() {
		return "landtalkhost";
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
	public int getTimeout() {
		return 0;
	}

	@Override
	public void setTimeout(int timeout) {

	}

	public URI getURI() {
		return null;
	}

	public void setURI(URI uri) {
	}

	@Override
	public final RedisConfigType type() {
		return RedisConfigType.LOCALHOST;
	}

}

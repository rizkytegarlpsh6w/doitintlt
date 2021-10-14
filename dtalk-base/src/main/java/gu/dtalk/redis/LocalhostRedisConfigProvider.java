package gu.dtalk.redis;
import static gu.dtalk.CommonConstant.*;
/**
 * {@link RedisConfigProvider}默认实现，只用于测试
 * @author guyadong
 *
 */

import java.net.URI;
public class LocalhostRedisConfigProvider implements RedisConfigProvider {

	@Override
	public String getHost() {
		return REDIS_HOST;
	}

	@Override
	public int getPort() {
		return REDIS_PORT;
	}

	@Override
	public String getPassword() {
		return null;
	}

	@Override
	public int getDatabase() {
		return 0;
	}

	@Override
	public int getTimeout() {
		return 0;
	}

	@Override
	public void setHost(String host) {
		
	}

	@Override
	public void setPort(int port) {
		
	}

	@Override
	public void setPassword(String password) {
		
	}

	@Override
	public void setDatabase(int database) {
		
	}

	@Override
	public void setTimeout(int timeout) {
	}
	
	@Override
	public URI getURI(){
		return null;
	}

	@Override
	public void setURI(URI uri){
	}

	@Override
	public final RedisConfigType type() {
		return RedisConfigType.LOCALHOST;
	}
}

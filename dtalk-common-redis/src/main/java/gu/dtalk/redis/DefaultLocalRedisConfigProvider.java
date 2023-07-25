package gu.dtalk.redis;

import java.net.URI;
import java.net.UnknownHostException;
import net.gdface.utils.JcifsUtil;

/**
 * 局域网配置
 * @author guyadong
 *
 */
public class DefaultLocalRedisConfigProvider implements RedisConfigProvider {
	private static String landtalkhost = "landtalkhost";
	
	private volatile String host;
	/**
	 * 返回局域网redis主机名
	 * @return landtalkhost
	 */
	public static String getLandtalkhost() {
		return landtalkhost;
	}

	/**
	 * 初始化局域网redis主机名，默认值为'landtalkhost'
	 * @param landtalkhost 要设置的 landtalkhost
	 */
	public static void initLandtalkhost(String landtalkhost) {
		DefaultLocalRedisConfigProvider.landtalkhost = landtalkhost;
	}

	@Override
	public String getHost() {
		if(host == null){
			synchronized (DefaultLocalRedisConfigProvider.class) {
				if(host == null){
						// 如果是主机名则解析为IP地址 
						try {
							host = JcifsUtil.hostAddressOf(landtalkhost);
						} catch (UnknownHostException e) {
							host = landtalkhost;
						}		
				}
			}
		}
		return host;
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
		return RedisConfigType.LAN;
	}

}

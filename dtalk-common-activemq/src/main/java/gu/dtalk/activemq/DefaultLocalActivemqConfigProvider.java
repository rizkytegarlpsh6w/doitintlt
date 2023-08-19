package gu.dtalk.activemq;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import org.apache.activemq.ActiveMQConnectionFactory;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import gu.simplemq.activemq.PropertiesHelper;
import gu.simplemq.utils.MQProperties;
import net.gdface.utils.JcifsUtil;

import static com.google.common.base.Preconditions.*;

/**
 * 局域网配置
 * @author guyadong
 *
 */
public class DefaultLocalActivemqConfigProvider extends BaseActivemqConfigProvider{
	private static final String REG_IPV4 = "^((?:(?:25[0-5]|2[0-4]\\d|[01]?\\d?\\d)\\.){3}(?:25[0-5]|2[0-4]\\d|[01]?\\d?\\d))$";
	public static final MQProperties INIT_PROPERTIES = PropertiesHelper.AHELPER.initParameters(null).initURI(makeLantalkURL("landtalkhost"));
	public DefaultLocalActivemqConfigProvider(){
	}
	
	@Override
	public final ActivemqConfigType type() {
		return ActivemqConfigType.LAN;
	}
	@Override
	protected MQProperties selfProp() {
		MQProperties props = INIT_PROPERTIES;
		URI uri = PropertiesHelper.AHELPER.getLocation(props);
		if(!uri.getHost().matches(REG_IPV4)){
			// 如果host不是IP地址格式，则替换主机名为对应的IP地址
			String host = IP_CACHE.getUnchecked(uri.getHost());
			URI u2 = changeHost(uri,host);
			props.initURI(u2);
		}
		return props;
	}

	private static final LoadingCache<String, String> IP_CACHE = CacheBuilder.newBuilder().build(new CacheLoader<String, String>(){

		@Override
		public String load(String landtalkhost) throws Exception {
			try {
				return JcifsUtil.hostAddressOf(landtalkhost);
			} catch (UnknownHostException e) {
				return landtalkhost;
			}	
		}});

	private static URI changeHost(URI uri,String host){
		// 替换主机名
		try {
			return new URI(uri.getScheme(),uri.getUserInfo(),host,uri.getPort(),uri.getPath(),uri.getQuery(),uri.getFragment());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	private static String makeLantalkURL(String landtalkhost){
		try {
			URI uri = new URI(ActiveMQConnectionFactory.DEFAULT_BROKER_BIND_URL);
			return changeHost(uri,landtalkhost).toString();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * 初始化局域网 ActiveMQ 主机名，默认值为'landtalkhost'
	 * @param landtalkhost 要设置的 landtalkhost
	 */
	public static void initLandtalkhost(String landtalkhost) {
		INIT_PROPERTIES.initURI(makeLantalkURL(checkNotNull(landtalkhost,"lantalkhost is null")));
	}

}

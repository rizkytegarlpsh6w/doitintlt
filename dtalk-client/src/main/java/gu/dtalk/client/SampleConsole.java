package gu.dtalk.client;

import com.google.common.base.Predicates;
import com.google.common.base.Strings;

import gu.dtalk.ConnectReq;
import gu.dtalk.exception.DtalkException;
import gu.dtalk.redis.RedisConfigType;
import gu.simplemq.Channel;
import gu.simplemq.redis.JedisPoolLazy;
import net.gdface.utils.FaceUtilits;

public class SampleConsole extends BaseConsole {

	public SampleConsole(String devmac, RedisConfigType config) {
		super(devmac, config);
	}
	@Override
	protected boolean authorize() {
			System.out.println("Input password  of Device,default password is last 4 character of device MAC address(lowercase):");
			ConnectReq req = new ConnectReq();
			req.mac = FaceUtilits.toHex(temminalMac);
			Channel<ConnectReq> conch = new Channel<>(connchname, ConnectReq.class);
			String pwd = null;
			while ((reqChannel == null) && !(pwd=scanLine(Predicates.<String>alwaysTrue())).isEmpty()) {
				req.pwd = FaceUtilits.getMD5String(pwd.getBytes());
				syncPublish(conch,req);
			}
			if(reqChannel != null){
				System.out.println("PASSWORD validate passed");
				return true;
			}
			return false;
		}

	public static void main(String []args){
		System.out.println("Text terminal for Device Talk is starting(设备交互字符终端启动)");
		String devmac = null;
		// 如果命令行提供了设备mac地址，则尝试解析该参数
		if(args.length > 1){
			devmac = parseMac(args[0]);
			if(devmac.isEmpty()){
				System.out.printf("ERROR:Invalid mac adress %s\n",devmac);
				return ;
			}
		}
		// 否则提示输入命令行参数
		if(Strings.isNullOrEmpty(devmac)){
			devmac = inputMac();
		}
		RedisConfigType config;
		try {
			config = RedisConfigType.lookupRedisConnect();
		} catch (DtalkException e) {
			System.out.println(e.getMessage());
			return;
		}
		logger.info("use config={}",config.toString());
		// 创建redis连接实例
		JedisPoolLazy.createDefaultInstance( config.readRedisParam() );

		SampleConsole client = new SampleConsole(devmac, config);
		client.start();

	}

}

package gu.dtalk.client;

import static gu.dtalk.client.SampleConsoleConfig.CONSOLE_CONFIG;

import com.google.common.base.Predicates;
import com.google.common.base.Strings;

import gu.dtalk.ConnectReq;
import gu.dtalk.redis.DefaultCustomRedisConfigProvider;
import gu.simplemq.Channel;
import gu.simplemq.IMessageQueueFactory;
import gu.simplemq.MessageQueueConfigManagers;
import gu.simplemq.MessageQueueType;
import gu.simplemq.exceptions.SmqNotFoundConnectionException;
import net.gdface.utils.FaceUtilits;

/**
 * 简单字符终端实现
 * @author guyadong
 *
 */
public class SampleConsole extends BaseConsole {

	public SampleConsole(String devmac, IMessageQueueFactory factory) throws SmqNotFoundConnectionException {
		super(devmac, factory);
	}
	/**
	 * 使用密码验证连接合法性<br>
	 * 向dtalk引擎发送包含连接密码和本机mac地址的json连接请求字符串({@link ConnectReq}),
	 * 收到回复的请求通道名，即连接成功
	 * @see gu.dtalk.client.BaseConsole#authorize()
	 */
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
		CONSOLE_CONFIG.parseCommandLine(args);
		MessageQueueType implType = CONSOLE_CONFIG.getImplType();
		switch (implType) {
		case REDIS:
			DefaultCustomRedisConfigProvider.initredisParameters(CONSOLE_CONFIG.getRedisParameters());
			break;
		default:
			throw new UnsupportedOperationException("UNSUPPORTED Message queue type:" + implType);
		}
		String devmac = CONSOLE_CONFIG.getMac();
		boolean trace = CONSOLE_CONFIG.isTrace();
		System.out.printf("Text terminal for Redis %s Talk is starting(设备(%s)交互字符终端启动)\n",implType.name(),implType.name());
		// 否则提示输入命令行参数
		if(Strings.isNullOrEmpty(devmac)){
			devmac = inputMac();
		}
		try {
			SampleConsole client = 
					BaseConsole.makeConsole(SampleConsole.class, devmac, MessageQueueConfigManagers.getManager(implType));
			client.setStackTrace(trace).start();
		} catch (SmqNotFoundConnectionException e) {
			if(trace){
				logger.error(e.getMessage(),e);	
			}else{
				System.out.println(e.getMessage());
			}
		}	
	}
}

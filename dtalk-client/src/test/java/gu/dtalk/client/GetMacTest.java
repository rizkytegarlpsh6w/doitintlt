package gu.dtalk.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Set;
import java.util.Stack;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

import net.gdface.utils.NetworkUtil;

public class GetMacTest {
	private static final Logger logger = LoggerFactory.getLogger(GetMacTest.class);
	@Test
	public void test1() throws UnknownHostException, IOException {
		Socket socket = new Socket("www.cnnic.net.cn", 80);
		InetAddress address = socket.getLocalAddress();
		Set<NetworkInterface> nics = NetworkUtil.getPhysicalNICs();
		for(NetworkInterface nic:nics){
			logger.info("{}",nic);
			
		}
		NetworkInterface curaddress = NetworkInterface.getByInetAddress(address);
		logger.info("current nic:{}",curaddress);
		logger.info("mac={}",NetworkUtil.formatMac(curaddress.getHardwareAddress(),":"));
		logger.info("ip={}",NetworkUtil.formatIp(address.getAddress()));
		
	}
	@Test
	public void test2(){
		String [] strings={"1","2","3"};
		logger.info(Joiner.on("/").join(strings));
		Stack<String> stack = new Stack<>();
		for(String str:strings){
			stack.push(str);
		}
		StringBuffer buffer = new StringBuffer("/");
		for(;!stack.isEmpty();){
			buffer.append(stack.pop()).append("/");
		}
		logger.info(buffer.toString());
	}
}

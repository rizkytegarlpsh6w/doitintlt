package gu.dtalk.engine.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import gu.dtalk.engine.DeviceUtils;
import gu.dtalk.engine.DtalkHttpServer;
import gu.dtalk.engine.ImageServe;
import net.gdface.utils.NetworkUtil;

import static gu.dtalk.engine.demo.DemoHttpdConfig.*;

/**
 * dtalk引擎演示(HTTP)
 * @author guyadong
 *
 */
public class DemoHttpd {
	private final DtalkHttpServer httpServer;
	public DemoHttpd() {
		DemoMenu root = new DemoMenu().init().register(DemoListener.INSTANCE);
		
		httpServer = new DtalkHttpServer(DEMO_CONFIG.getPort())
				.setRoot(root)
				.setDebug(true)
				.setNoAuth(DEMO_CONFIG.isNoauth())
				.setNoCORS(DEMO_CONFIG.isNoCORS());
	}
	/**
	 * 启动连接
	 */
	private void start(){
		byte[] devMac = DeviceUtils.DEVINFO_PROVIDER.getMac();
		System.out.printf("DEVICE MAC address(设备地址): %s\n",NetworkUtil.formatMac(devMac, ":"));
		try {
			httpServer.addExtServe(new ImageServe("/PERSON").setBodyGetter(new ImageServDemo()));
			httpServer.start(DEMO_CONFIG.getTimeout(), false);
			System.out.printf("HTTP Connect port(连接端口) : port:%d %s %s\n",
					DEMO_CONFIG.getPort(),
					DEMO_CONFIG.isNoauth() ? "NOAUTH" : "",
					DEMO_CONFIG.isNoCORS()? "NOCORS" : "");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private static void waitquit(){
		System.out.println("PRESS 'quit' OR 'CTRL-C' to exit");
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in)); 
		try{
			while(!"quit".equalsIgnoreCase(reader.readLine())){				
			}
			System.exit(0);
		} catch (IOException e) {

		}finally {

		}
	}
	public static void main(String []args){		
		try{
			DEMO_CONFIG.parseCommandLine(args);

			System.out.println("Device talk Demo(HTTP) starting(设备模拟器启动)");
			
			new DemoHttpd().start();
			waitquit();
		}catch (Throwable e) {
			if(DEMO_CONFIG.isTrace()){
				e.printStackTrace();	
			}else{
				System.out.println(e.getMessage());
			}
		}
	}

}

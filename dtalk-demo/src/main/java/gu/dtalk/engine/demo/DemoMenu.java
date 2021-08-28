package gu.dtalk.engine.demo;

import gu.dtalk.IntOption;
import gu.dtalk.ItemBuilder;
import gu.dtalk.MenuItem;
import gu.dtalk.RootMenu;
import gu.dtalk.StringOption;
import net.gdface.utils.FaceUtilits;

import static gu.dtalk.CommonConstant.*;
import static gu.dtalk.engine.SampleConnector.DEVINFO_PROVIDER;

public class DemoMenu extends RootMenu{

	public DemoMenu() {
	}
	public DemoMenu init(){
		String macStr = FaceUtilits.toHex(DEVINFO_PROVIDER.getMac());
		
		MenuItem device = 
				ItemBuilder.builder(MenuItem.class)
					.name("device")
					.description("设备")
					.addChilds(
							ItemBuilder.builder(StringOption.class).name("name").uiName("设备名称").instance(),
							ItemBuilder.builder(StringOption.class).name("sn").uiName("设备序列号").instance().setValue("001122334455"),
							ItemBuilder.builder(StringOption.class).name("mac").uiName("物理地址").instance().setReadonly(true).setValue(macStr),
							ItemBuilder.builder(StringOption.class).name("gps").uiName("位置(GPS)").instance().setReadonly(true),
							ItemBuilder.builder(StringOption.class).name("password").uiName("连接密码").instance().setDefaultValue(DEVINFO_PROVIDER.getPassword()),
							ItemBuilder.builder(StringOption.class).name("version").uiName("版本号").instance().setReadonly(true).setDefaultValue("unknow"))
					.instance();
		MenuItem network = 
				ItemBuilder.builder(MenuItem.class)
					.name("redis")
					.description("REDIS 服务器")
					.addChilds(
							ItemBuilder.builder(StringOption.class).name("host").uiName("主机名称").instance().setValue(REDIS_HOST),
							ItemBuilder.builder(IntOption.class).name("port").uiName("端口号").instance().setValue(REDIS_PORT),
							ItemBuilder.builder(StringOption.class).name("password").uiName("连接密码").instance().setValue(REDIS_PASSWORD))
					.instance();
		
		addChilds(device,network);
		return this;
	}
}

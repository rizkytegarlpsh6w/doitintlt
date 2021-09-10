package gu.dtalk.engine.demo;

import gu.dtalk.IntOption;
import gu.dtalk.ItemBuilder;
import gu.dtalk.MACOption;
import gu.dtalk.MenuItem;
import gu.dtalk.OptionType;
import gu.dtalk.PasswordOption;
import gu.dtalk.RootMenu;
import gu.dtalk.StringOption;
import gu.dtalk.SwitchOption;

import static gu.dtalk.CommonConstant.*;
import static gu.dtalk.engine.SampleConnector.DEVINFO_PROVIDER;

import java.util.Date;

import gu.dtalk.BoolOption;
import gu.dtalk.CheckOption;
import gu.dtalk.DateOption;
import gu.dtalk.IPv4Option;

public class DemoMenu extends RootMenu{

	public DemoMenu() {
	}
	public DemoMenu init(){
		byte[] mac = DEVINFO_PROVIDER.getMac();
		byte[] ip = DEVINFO_PROVIDER.getIp();
		MenuItem device = 
				ItemBuilder.builder(MenuItem.class)
					.name("device")
					.uiName("设备")
					.addChilds(
							ItemBuilder.builder(StringOption.class).name("name").uiName("设备名称").instance(),
							ItemBuilder.builder(StringOption.class).name("sn").uiName("设备序列号").instance().setValue("001122334455"),
							ItemBuilder.builder(IPv4Option.class).name("IP").uiName("IP地址").instance().setValue(ip).setReadonly(true),
							ItemBuilder.builder(MACOption.class).name("mac").uiName("物理地址").instance().setReadonly(true).setValue(mac).setReadonly(true),
							ItemBuilder.builder(StringOption.class).name("gps").uiName("位置(GPS)").instance().setReadonly(true),
							ItemBuilder.builder(PasswordOption.class).name("password").uiName("连接密码").instance().setValue(DEVINFO_PROVIDER.getPassword()),
							ItemBuilder.builder(StringOption.class).name("version").uiName("版本号").instance().setReadonly(true).setValue("unknow"))
					.instance();
		MenuItem redis = 
				ItemBuilder.builder(MenuItem.class)
					.name("redis")
					.uiName("REDIS 服务器")
					.addChilds(
							ItemBuilder.builder(StringOption.class).name("host").uiName("主机名称").instance().setValue(REDIS_HOST),
							ItemBuilder.builder(IntOption.class).name("port").uiName("端口号").instance().setValue(REDIS_PORT),
							ItemBuilder.builder(IntOption.class).name("db").uiName("数据库").instance().setValue(0),
							ItemBuilder.builder(PasswordOption.class).name("password").uiName("连接密码").instance().setValue(REDIS_PASSWORD))
					.instance();
		MenuItem test = 
				ItemBuilder.builder(MenuItem.class)
					.name("test")
					.uiName("类型测试")
					.addChilds(
							ItemBuilder.builder(DateOption.class).name("date").uiName("日期测试").instance().setValue(new Date()),
							ItemBuilder.builder(BoolOption.class).name("bool").uiName("BOOL测试").instance().setValue(true),
							ItemBuilder.builder(IPv4Option.class).name("ipv4").uiName("IPV4测试").instance().asValue("127.0.0.1"),
							ItemBuilder.builder(MACOption.class).name("mac").uiName("MAC测试").instance().asValue("22:35:ff:e0:3f:ab"),
							new ItemBuilder<>(new SwitchOption<Float>()).name("swith").uiName("SWITCH测试").instance()
							.addOption(0f, "zero")
							.addOption(0.5f, "half")
							.addOption(1f, "full")
							.setSelected(0.5f),
							new ItemBuilder<>(new CheckOption<String>()).name("check").uiName("CHECK测试").instance()
												.addOption("中国", "zero")
												.addOption("俄罗斯", "half")
												.addOption("美国", "full")
												.setValue(1),
							ItemBuilder.builder(OptionType.EMAIL).name("email").uiName("email测试").instance().asValue("my@hello.com"),
							ItemBuilder.builder(OptionType.MPHONE).name("mobilePhone").uiName("移动电话号码测试").instance().asValue("13611426411"),
							ItemBuilder.builder(OptionType.IDNUM).name("idnum").uiName("身份证号码测试").instance().asValue("320113199001133483"),
							ItemBuilder.builder(OptionType.INTEGER).name("integer").uiName("数字测试").instance().asValue("133483")

							)
					.instance();
		addChilds(device,redis,test);
		return this;
	}
}

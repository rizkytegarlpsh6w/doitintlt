package gu.dtalk.engine.demo;

import gu.dtalk.IntOption;
import gu.dtalk.ItemBuilder;
import gu.dtalk.MACOption;
import gu.dtalk.MenuItem;
import gu.dtalk.NumberValidator;
import gu.dtalk.OptionType;
import gu.dtalk.OptionViewCmd;
import gu.dtalk.PasswordOption;
import gu.dtalk.RootMenu;
import gu.dtalk.StringOption;
import gu.dtalk.SwitchOption;
import gu.dtalk.event.ValueListener;
import gu.simplemq.IMQConnParameterSupplier;

import java.util.Date;
import com.google.common.net.HostAndPort;

import gu.dtalk.BoolOption;
import gu.dtalk.CheckOption;
import gu.dtalk.CmdItem;
import gu.dtalk.DateOption;
import gu.dtalk.IPv4Option;

import static gu.dtalk.engine.DeviceUtils.DEVINFO_PROVIDER;
import static com.google.common.base.Preconditions.*;
import static gu.simplemq.IMessageQueueFactory.*;

public class DemoMenu extends RootMenu{
	private final IMQConnParameterSupplier paramSupplier;
	
	public DemoMenu() {
		this(null);
	}
	public DemoMenu(IMQConnParameterSupplier config) {
		this.paramSupplier = checkNotNull(config,"config is null");
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
						ItemBuilder.builder(StringOption.class).name("sn").uiName("设备序列号").instance().asValue("001122334455"),
						ItemBuilder.builder(IPv4Option.class).name("IP").uiName("IP地址").instance().setValue(ip).setReadOnly(true),
						ItemBuilder.builder(MACOption.class).name("mac").uiName("物理地址").instance().setReadOnly(true).setValue(mac).setReadOnly(true),
						ItemBuilder.builder(StringOption.class).name("gps").uiName("位置(GPS)").instance().setReadOnly(true),
						ItemBuilder.builder(PasswordOption.class).name("password").uiName("连接密码").instance().setValue(DEVINFO_PROVIDER.getPassword()),
						ItemBuilder.builder(StringOption.class).name("version").uiName("版本号").instance().setReadOnly(true).setValue("unknow"))
				.instance();
		MenuItem mq;
		switch (paramSupplier.getImplType()) {
		case REDIS:
		{
			HostAndPort hostAndPort = paramSupplier.getHostAndPort();
			mq = ItemBuilder.builder(MenuItem.class)
					.name("mq")
					.uiName("Message Queue 服务器")
					.addChilds(
							ItemBuilder.builder(StringOption.class).name("type").uiName("消息系统类型").instance().setValue(paramSupplier.getImplType().name()),
							ItemBuilder.builder(StringOption.class).name("host").uiName("主机名称").instance().setValue(hostAndPort.getHost()),
							ItemBuilder.builder(IntOption.class).name("port").uiName("端口号").instance().setValue(hostAndPort.getPort()),
							ItemBuilder.builder(PasswordOption.class).name("password").uiName("连接密码").instance().setValue((String) paramSupplier.getMQConnParameters().get(MQ_PASSWORD)))
					.instance();
			break;
		}
		case ACTIVEMQ:
		{
			HostAndPort hostAndPort = paramSupplier.getHostAndPort();
			mq = ItemBuilder.builder(MenuItem.class)
					.name("mq")
					.uiName("Message Queue 服务器")
					.addChilds(
							ItemBuilder.builder(StringOption.class).name("type").uiName("消息系统类型").instance().setValue(paramSupplier.getImplType().name()),
							ItemBuilder.builder(StringOption.class).name("host").uiName("主机名称").instance().setValue(hostAndPort.getHost()),
							ItemBuilder.builder(IntOption.class).name("port").uiName("端口号").instance().setValue(hostAndPort.getPort()))
					.instance();
			break;
		}
		default:
			throw new IllegalArgumentException("UNSUPPORTED message queue " + paramSupplier.getImplType());
		}
			
		MenuItem test = 
			ItemBuilder.builder(MenuItem.class)
				.name("test")
				.uiName("类型测试")
				.addChilds(
						ItemBuilder.builder(DateOption.class).name("date").uiName("日期测试").instance().setValue(new Date()),
						ItemBuilder.builder(BoolOption.class).name("bool").uiName("BOOL测试").instance().setValue(true),
						ItemBuilder.builder(IPv4Option.class).name("ipv4").uiName("IPV4测试").instance().asValue("127.0.0.1"),
						ItemBuilder.builder(MACOption.class).name("mac").uiName("MAC测试").instance().asValue("22:35:ff:e0:3f:ab"),
						ItemBuilder.builder(new SwitchOption<Float>()).name("swith").uiName("SWITCH测试").instance()
							.addOption(0f, "zero")
							.addOption(0.5f, "half")
							.addOption(1f, "full")
							.setSelected(0.5f),
						ItemBuilder.builder(new CheckOption<String>()).name("check").uiName("CHECK测试").instance()
							.addOption("中国", "zero")
							.addOption("俄罗斯", "half")
							.addOption("美国", "full")
							.setValue(1),
						OptionType.EMAIL.builder().name("email").uiName("email测试").asValue("my@hello.com").instance(),
						OptionType.MPHONE.builder().name("mphone").uiName("移动电话号码测试").asValue("13611426411").instance(),
						OptionType.IDNUM.builder().name("idnum").uiName("身份证号码测试").asValue("320113199001133483").instance(),
						OptionType.INTEGER.builder().name("integer").uiName("数字测试").value(0)
											.validator(NumberValidator.makeValidator(1024,256,128,0)).instance(),
						OptionType.URL.builder().name("url").uiName("URL测试").asValue("https://gitee.com/l0km/dtalk.git").instance(),
						ItemBuilder.builder(CmdItem.class).name("optionview").uiName("命令测试").description("显示指定选项")
							.addChilds(OptionType.STRING.builder().name(OptionViewCmd.QUERY).uiName("选项类型").instance())
							.instance().setCmdAdapter(new OptionViewCmd()),
						OptionType.IMAGE.builder().name("image").uiName("图像测试").instance()							
						)
				.instance();
		addChilds(device,mq,test);
		
		return this;
	}
	public DemoMenu register(ValueListener<Object> listener){
		listener.registerTo(this);
		return this;
	}

}

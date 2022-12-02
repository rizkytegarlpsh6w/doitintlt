package gu.dtalk;

import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.JavaBeanDeserializer;

import gu.simplemq.json.FastJsonInitializer;

/**
 * fastjson的全局初始化设置
 * @author guyadong
 *
 */
public class ItemInitializer implements FastJsonInitializer {

	public ItemInitializer() {
	}

	@Override
	public void init() {
		// 增加对Ack对象的序列化支持
		ParserConfig.global.putDeserializer(new TypeReference<Ack<?>>(){}.getType(), new JavaBeanDeserializer(ParserConfig.global, Ack.class));
		ParserConfig.global.putDeserializer(new TypeReference<Ack<String>>(){}.getType(), new JavaBeanDeserializer(ParserConfig.global, Ack.class));
		ParserConfig.global.putDeserializer(new TypeReference<Ack<BaseItem>>(){}.getType(), new JavaBeanDeserializer(ParserConfig.global, Ack.class));
		// 增加对 item 序列化支持
		ParserConfig.global.putDeserializer(BaseItem.class, ItemDeserializer.instance);
		ParserConfig.global.putDeserializer(CmdItem.class, new JavaBeanDeserializer(ParserConfig.global, CmdItem.class));
		ParserConfig.global.putDeserializer(MenuItem.class, new JavaBeanDeserializer(ParserConfig.global, MenuItem.class));

		// 增加对 option 序列化支持
		ParserConfig.global.putDeserializer(Base64Option.class, new JavaBeanDeserializer(ParserConfig.global, Base64Option.class));
		ParserConfig.global.putDeserializer(MACOption.class, new JavaBeanDeserializer(ParserConfig.global, MACOption.class));
		ParserConfig.global.putDeserializer(IPv4Option.class, new JavaBeanDeserializer(ParserConfig.global, IPv4Option.class));
		ParserConfig.global.putDeserializer(Base64Option.class, new JavaBeanDeserializer(ParserConfig.global, Base64Option.class));
		ParserConfig.global.putDeserializer(BoolOption.class, new JavaBeanDeserializer(ParserConfig.global, BoolOption.class));
		ParserConfig.global.putDeserializer(CheckOption.class, new JavaBeanDeserializer(ParserConfig.global, CheckOption.class));
		ParserConfig.global.putDeserializer(DateOption.class, new JavaBeanDeserializer(ParserConfig.global, DateOption.class));
		ParserConfig.global.putDeserializer(FloatOption.class, new JavaBeanDeserializer(ParserConfig.global, FloatOption.class));
		ParserConfig.global.putDeserializer(IntOption.class, new JavaBeanDeserializer(ParserConfig.global, IntOption.class));
		ParserConfig.global.putDeserializer(StringOption.class, new JavaBeanDeserializer(ParserConfig.global, StringOption.class));
		ParserConfig.global.putDeserializer(SwitchOption.class, new JavaBeanDeserializer(ParserConfig.global, SwitchOption.class));
		ParserConfig.global.putDeserializer(UrlOption.class, new JavaBeanDeserializer(ParserConfig.global, UrlOption.class));
		ParserConfig.global.putDeserializer(PasswordOption.class, new JavaBeanDeserializer(ParserConfig.global, PasswordOption.class));
		ParserConfig.global.putDeserializer(ImageOption.class, new JavaBeanDeserializer(ParserConfig.global, ImageOption.class));

	}

}

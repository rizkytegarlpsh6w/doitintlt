package gu.dtalk;

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
		// 增加对 IItem 序列化支持
		ParserConfig.global.putDeserializer(CmdItem.class, new JavaBeanDeserializer(ParserConfig.global, CmdItem.class));
		ParserConfig.global.putDeserializer(MenuItem.class, new JavaBeanDeserializer(ParserConfig.global, MenuItem.class));
		ParserConfig.global.putDeserializer(BaseItem.class, ItemDeserializer.instance);

		//SerializeConfig.globalInstance.put(BaseItem.class, ItemCodec.instance);
	}

}

package gu.dtalk;

import java.lang.reflect.Type;
import java.util.Map;

import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.JavaBeanDeserializer;
import com.alibaba.fastjson.parser.deserializer.MapDeserializer;

/**
 * item对象反序列化实现
 * @author guyadong
 *
 */
public class ItemDeserializer extends JavaBeanDeserializer{
	public static final ItemDeserializer instance = new ItemDeserializer();
	public ItemDeserializer() {
		super(ParserConfig.global, BaseItem.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
		Map<String,Object> map = MapDeserializer.instance.deserialze(parser, 
				new TypeReference<Map<String,Object>>(){}.getType(), fieldName);
		return (T) createInstance(map,null);
	}

	@Override
	public Object createInstance(Map<String, Object> map, ParserConfig config) {
		return ItemType.parseItem(map);
	}
}

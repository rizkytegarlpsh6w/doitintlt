package gu.dtalk;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONToken;
import com.alibaba.fastjson.parser.deserializer.MapDeserializer;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeConfig;

/**
 * item对象序列化/反序列化接口实现
 * @author guyadong
 *
 */
public class ItemCodec implements ObjectSerializer, ObjectDeserializer{
	public static final ItemCodec instance = new ItemCodec();
	public ItemCodec() {
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
		Map<String,Object> map = MapDeserializer.instance.deserialze(parser, 
				new TypeReference<Map<String,Object>>(){}.getType(), fieldName);
		return (T) ItemType.parseItem(map);
	}

	@Override
	public int getFastMatchToken() {
		return JSONToken.LBRACE;
	}

	@Override
	public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features)
			throws IOException {
		ObjectSerializer objectSerializer = SerializeConfig.globalInstance.createJavaBeanSerializer(object.getClass());
		objectSerializer.write(serializer, object, fieldName, fieldType, features);
	}

}

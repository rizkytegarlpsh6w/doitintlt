package gu.dtalk;

import static gu.dtalk.CommonConstant.OPTION_FIELD_VALUE;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import gu.simplemq.json.BaseJsonEncoder;
import net.gdface.utils.FaceUtilits;

public class ItemTest {
	private Logger logger = LoggerFactory.getLogger(ItemTest.class);
	/** 指定生成的json中field不带引号 */
	private static final int NO_FIELD_QUOTE_FEATURE = SerializerFeature.config(
			JSON.DEFAULT_GENERATE_FEATURE, SerializerFeature.QuoteFieldNames, false);
	@Test
	public void testWriteMethod() {
		BaseItem opt = new BoolOption().setValue(true).setName("hello");
//		String json = BaseJsonEncoder.getEncoder().toJsonString(opt);
		String json = JSON.toJSONString(opt,NO_FIELD_QUOTE_FEATURE);
		logger.info("json={}", json);
		BoolOption parsed = BaseJsonEncoder.getEncoder().fromJson(json, BoolOption.class);
		logger.info("json parsed={}",parsed);
	}

	@Test
	public void test2Json(){
		int intarray[] = {1,2,4};
		logger.info("json of int array {}",JSON.toJSONString(intarray));		
	}
	@Test
	public void test3IP(){
		String ipaddress = "127.0.0.1";
		String ip = "[" + ipaddress.replace(".", ",") + "]";
		byte[] parsed = JSON.parseObject(ip,	byte[].class);
		logger.info("pares = {}",parsed);
	}
	@Test
	public void test4MAC(){
		String macstr = "00:4f:Fe:ea:98:3a";
		String hex = macstr.replace(":", "");
		logger.info("mac hex ={}",FaceUtilits.hex2Bytes(hex));
	}
}

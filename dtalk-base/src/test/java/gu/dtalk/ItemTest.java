package gu.dtalk;

import java.net.URL;
import java.util.Date;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.util.TypeUtils;

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
	@Test
	public void test4URL(){		
		try {
			String intArrayJson = BaseJsonEncoder.getEncoder().toJsonString(new int[]{1,2,3});
			logger.info("intArrayJson={}",intArrayJson);
			int[] parsedIntArray = BaseJsonEncoder.getEncoder().fromJson(intArrayJson, int[].class);
			logger.info("parsedInt={}",parsedIntArray);
			String intJson = BaseJsonEncoder.getEncoder().toJsonString(2014);
			logger.info("intJson={}",intJson);
			int parsedInt = BaseJsonEncoder.getEncoder().fromJson(intJson, int.class);
			logger.info("parsedInt={}",parsedInt);
			String dateJson = BaseJsonEncoder.getEncoder().toJsonString(new Date());
			logger.info("dateJson={}",dateJson);
			Date parsedDate = BaseJsonEncoder.getEncoder().fromJson(dateJson, Date.class);
			logger.info("parsedDate={}",parsedDate);
			String urlJson = BaseJsonEncoder.getEncoder().toJsonString(new URL("https://gitee.com/l0km/dtalk.git"));
			logger.info("urlJson={}",urlJson);
			URL parseUrl = BaseJsonEncoder.getEncoder().fromJson(urlJson, URL.class);
			logger.info("parseUrl={}",parseUrl);
			BaseJsonEncoder.getEncoder().fromJson("https://gitee.com/l0km/dtalk.git", URL.class);

		} catch (Exception e) {
			e.printStackTrace();
		}
		//BaseJsonEncoder.getEncoder().fromJson("https://gitee.com/l0km/dtalk.git", URL.class);
	}
}

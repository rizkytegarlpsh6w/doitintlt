package gu.dtalk;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.Date;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import gu.simplemq.json.BaseJsonEncoder;
import net.gdface.utils.BinaryUtils;

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
		logger.info("mac hex ={}",BinaryUtils.hex2Bytes(hex));
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

		} catch (Throwable e) {
			e.printStackTrace();
		}
		//BaseJsonEncoder.getEncoder().fromJson("https://gitee.com/l0km/dtalk.git", URL.class);
	}
	@Test
	public void test5Image(){
		try {
			Method valueSetter = ImageOption.class.getMethod("setValue", byte[].class);
			logger.info("DeclaringClass {}",valueSetter.getDeclaringClass().getName());
			Method valueSetter2 = ImageOption.class.getMethod("setValue", Object.class);
			logger.info("DeclaringClass {}",valueSetter2.getDeclaringClass().getName());
			ImageOption img = OptionBuilder.builder(ImageOption.class).name("testimg").value(BinaryUtils.getBytes(ItemTest.class.getResource("/images/dg.png"))).instance();
			String jsonStr = BaseJsonEncoder.getEncoder().toJsonString(img);
			ImageOption parsed = BaseJsonEncoder.getEncoder().fromJson(jsonStr, ImageOption.class);
			logger.info("{}",parsed.toString());
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	@Test
	public void test6Menu(){
		MenuItem menu1 = ItemBuilder.builder(MenuItem.class).name("menu1").uiName("菜单1").addChilds(
				ItemBuilder.builder(MenuItem.class).name("menu1_1").uiName("菜单1.1").addChilds(
						OptionType.STRING.builder().name("option1").uiName("选项1").instance(),
						OptionType.INTEGER.builder().name("option2").uiName("选项2").instance()
						).instance()
				).instance();
		MenuItem menu2 = ItemBuilder.builder(MenuItem.class).name("menu2").uiName("菜单2").addChilds(
				ItemBuilder.builder(MenuItem.class).name("menu2_1").uiName("菜单2.1").addChilds(
							ItemBuilder.builder(CmdItem.class).name("cmd1").uiName("命令1").instance().addParameters(
									OptionType.STRING.builder().name("param1").uiName("命令参数1").instance()
									)
						).instance()
				).instance();
		RootMenu root = new RootMenu();
		root.addChilds(menu1,menu2);
		
		logger.info(BaseJsonEncoder.getEncoder().toJsonString(root));
	}
}

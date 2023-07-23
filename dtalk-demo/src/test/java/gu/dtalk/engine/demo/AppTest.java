package gu.dtalk.engine.demo;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

public class AppTest {
	private static final Logger logger = LoggerFactory.getLogger(AppTest.class);

	@Test
	public void test() {
		String jsonstr = JSON.toJSONString("hello!!!!");
		logger.info("[{}]",jsonstr);
		Object parsed = JSON.parse(jsonstr);

		logger.info("parsed [{}]",parsed);
		parsed = JSON.parseObject(jsonstr,String.class);
		logger.info("parsed [{}]",parsed);
	}

}

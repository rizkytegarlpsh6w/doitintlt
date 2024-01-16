package gu.dtalk.engine.demo;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Function;

import gu.dtalk.engine.DtalkHttpServer.Body;
import net.gdface.utils.BinaryUtils;

/**
 * ImageServ 示例代码
 * @author guyadong
 *
 */
public class ImageServDemo implements Function<String, Body> {

	public ImageServDemo() {
	}

	/** 
	 * 根据http请求的路径,解析出图像id,
	 * 如果找到指定的图像则返回{@link Body}对象，否则返回null
	 * @see com.google.common.base.Function#apply(java.lang.Object)
	 */
	@Override
	public Body apply(String input) {
		try {
			Pattern pattern = Pattern.compile("/\\w+/(\\d+)");
			Matcher m = pattern.matcher(input);
			if(m.matches()){
				Integer id = Integer.valueOf(m.group(1));
				URL resource = ImageServDemo.class.getResource("/images/person/p" + id + ".jpg");
				if(resource != null){
					return new Body("image/jpeg", BinaryUtils.getBytes(resource));
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

}

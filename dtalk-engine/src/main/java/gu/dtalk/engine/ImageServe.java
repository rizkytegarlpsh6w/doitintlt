package gu.dtalk.engine;

import java.io.IOException;

import com.google.common.base.Function;
import fi.iki.elonen.NanoHTTPD.Response;
import gu.dtalk.engine.DtalkHttpServer.Body;
import net.gdface.utils.BaseVolatile;
import net.gdface.utils.BinaryUtils;
import net.gdface.utils.ILazyInitVariable;

/**
 * 实现GET方法获取图像请求
 * 如果没有找到图像(返回null),则返回默认的404图像 
 * @author guyadong
 *
 */
public class ImageServe extends RESTfulServe {
	private static final ILazyInitVariable<Body> ERROR_IMAGE = new BaseVolatile<Body>(){

		@Override
		protected Body doGet() {
			try {
				return new Body(Response.Status.NOT_FOUND, 
					"image/jpeg", 
					BinaryUtils.getBytesNotEmpty(ImageServe.class.getResourceAsStream("/web/images/404.jpg")));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}};

	public ImageServe() {
		super(ImageServe.class.getSimpleName());
	}

	public ImageServe(String pathPrefix) {
		super(pathPrefix);
	}

	public ImageServe(String pathPrefix, Function<String, Body> bodyGetter) {
		super(pathPrefix, bodyGetter);
	}
	@Override
	protected Body nullBodyInstead(){
		return ERROR_IMAGE.get();
	}
}

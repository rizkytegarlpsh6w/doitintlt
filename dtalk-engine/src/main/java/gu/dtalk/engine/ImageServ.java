package gu.dtalk.engine;

import static gu.dtalk.engine.DtalkHttpServer.makeResponse;

import java.io.IOException;

import com.google.common.base.MoreObjects;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import gu.dtalk.engine.DtalkHttpServer.Body;
import net.gdface.utils.BaseVolatile;
import net.gdface.utils.FaceUtilits;
import net.gdface.utils.ILazyInitVariable;

/**
 * 实现GET方法获取图像请求
 * 如果没有找到图像(返回null),则返回默认的404图像 
 * @author guyadong
 *
 */
public class ImageServ extends RESTfulServe {
	private static final ILazyInitVariable<Body> ERROR_IMAGE = new BaseVolatile<Body>(){

		@Override
		protected Body doGet() {
			try {
				return new Body(Response.Status.NOT_FOUND, 
					"image/jpeg", 
					FaceUtilits.getBytesNotEmpty(ImageServ.class.getResourceAsStream("/web/images/404.jpg")));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}};

	@Override
	public Response apply(IHTTPSession input) {
		return MoreObjects.firstNonNull(super.apply(input), makeResponse(ERROR_IMAGE.get()));
	}

}

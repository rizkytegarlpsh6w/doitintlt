package gu.dtalk.engine;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import static gu.dtalk.engine.DtalkHttpServer.*;

/**
 * 通用RESTful方法实现扩展http请求接口
 * 应用层可使用此类根据http请求的路径来返回相应的数据
 * @author guyadong
 *
 */
public class RESTfulServe implements Function<IHTTPSession, Response>{

	public RESTfulServe() {
	}

	private static final Function<String, Body> nullBodyGetter = new Function<String, Body>(){

		@Override
		public Body apply(String input) {
			return null;
		}};
	/**
	 * 根据资源path返回响应数据(Body)的Function<br>
	 * INPUT (String) http请求路径
	 * OUTPU (Body) http响应数据
	 */
	private Function<String, Body> bodyGetter = nullBodyGetter;
	/**
	 * 根据http请求路径返回对应的HTTP 响应对象
	 * 应用层可重写此方法
	 * @param path
	 * @return 没有找到指定的资源则返回{@code null}
	 */
	protected Body getResponseBody(String path){
		return bodyGetter.apply(path);
	}
	/**
	 * 当前请求
	 */
	private static final ThreadLocal<IHTTPSession> currentRequest = new ThreadLocal<IHTTPSession>();
	@Override
	public Response apply(IHTTPSession input) {
		currentRequest.set(input);
		try {
			Body body = getResponseBody(input.getUri());
			return makeResponse(body);
		} finally {
			currentRequest.remove();
		}
	}
	/**
	 * @return 当前请求
	 */
	protected final IHTTPSession getCurrentRequest() {
		return currentRequest.get();
	}
	/**
	 * @return bodyGetter
	 */
	public Function<String, Body> getBodyGetter() {
		return bodyGetter;
	}

	/**
	 * bodyGetter根据http请求的path(uri)返回响应数据对象 ({@link Body})，
	 * 应用层可以实现此接口，实现特定的HTTPG请求
	 * @param bodyGetter 要设置的 bodyGetter
	 * @return 当前对象
	 */
	public RESTfulServe setBodyGetter(Function<String, Body> bodyGetter) {
		this.bodyGetter = MoreObjects.firstNonNull(bodyGetter,nullBodyGetter);
		return this;
	}

}

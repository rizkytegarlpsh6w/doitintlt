package gu.dtalk.engine;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import gu.dtalk.engine.DtalkHttpServer.Body;

import static gu.dtalk.engine.DtalkHttpServer.*;

import static com.google.common.base.Preconditions.*;

/**
 * 通用RESTful方法实现扩展http请求接口
 * 应用层可使用此类根据http请求的路径来返回相应的数据
 * @author guyadong
 *
 */
public class RESTfulServe implements Function<IHTTPSession, Response>{

	private final String pathPrefix;
	
	public RESTfulServe(String pathPrefix) {
		this.pathPrefix = checkNotNull(pathPrefix,"pathPrefix is null");
	}
	public RESTfulServe(String pathPrefix,Function<String, Body> bodyGetter) {
		this(pathPrefix);
		setBodyGetter(bodyGetter);
	}
	public RESTfulServe() {
		this(RESTfulServe.class.getSimpleName());
	}

	private static final Function<String, Body> nullBodyGetter = new Function<String, Body>(){

		@Override
		public Body apply(String input) {
			return null;
		}};
	/**
	 * 根据资源path返回响应数据(Body)的Function<br>
	 */
	private Function<String, Body> bodyGetter = nullBodyGetter;
	/**
	 * 根据http请求路径返回对应的HTTP 响应对象
	 * 应用层可重写此方法实现自己的业务逻辑
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

	/**
	 * 返回当{@link #getResponseBody(String) }返回{@code null}时，用于替代的对象,
	 * 默认返回{@code null},子类可重写此方法
	 * @return {@link Body}对象
	 */
	protected Body nullBodyInstead(){
		return null;
	}
	@Override
	public final Response apply(IHTTPSession input) {
		currentRequest.set(input);
		try {
			if(input.getUri().startsWith(pathPrefix)){
				Body body = getResponseBody(input.getUri());
				return makeResponse(body == null ? nullBodyInstead() :  body);
			}else{
				// 路径前缀不匹配则报错
				return NanoHTTPD.newFixedLengthResponse(
						Status.BAD_REQUEST, 
						NanoHTTPD.MIME_PLAINTEXT, 
						"MISMATCH PREFIX:" + pathPrefix);
			}
			
		} finally {
			currentRequest.remove();
		}
	}
	/**
	 * 子类可以通过这个方法获取当前请求的完整数据
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
	 * 应用层可以实现此接口，实现特定的HTTPG请求<br>
	 * 根据资源path返回响应数据(Body)的Function<br>
	 * INPUT (String) http请求路径
	 * OUTPU (Body) http响应数据
	 * @param bodyGetter 要设置的 bodyGetter
	 * @return 当前对象
	 */
	public RESTfulServe setBodyGetter(Function<String, Body> bodyGetter) {
		this.bodyGetter = MoreObjects.firstNonNull(bodyGetter,nullBodyGetter);
		return this;
	}

	/**
	 * @return 返回当前对象接受的HTTP请求路径前缀
	 */
	public String getPathPrefix(){
		return pathPrefix;
	}

}

package gu.dtalk.engine;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static gu.dtalk.CommonConstant.DEFAULT_IDLE_TIME_MILLS;
import static gu.dtalk.engine.DeviceUtils.DEVINFO_PROVIDER;

import java.io.IOException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import gu.dtalk.Ack;
import gu.simplemq.exceptions.SmqUnsubscribeException;
import gu.simplemq.json.BaseJsonEncoder;
import net.gdface.utils.FaceUtilits;

import static gu.dtalk.CommonConstant.*;

/**
 * dtalk http 服务
 * @author guyadong
 *
 */
public class ItemAdapterHttpServer extends NanoHTTPD {
	private static final Logger logger = LoggerFactory.getLogger(ItemAdapterHttpServer.class);
	private static final String DTALK_SESSION="dtalk-session"; 
	public static final String APPICATION_JSON="application/json";
	private static final String POST_DATA="postData";
	private static final String DTALK_PREFIX="/dtalk";
	private static class SingletonTimer{
		private static final Timer instnace = new Timer(true);
	}
	private Timer timer;
	private Timer getTimer(){
		// 懒加载
		if(timer == null){
			timer = SingletonTimer.instnace;
		}
		return timer;
	}
	private long idleTimeLimit = DEFAULT_IDLE_TIME_MILLS;
	private long timerPeriod = 2000;

	private String selfMac;
	
	/**
	 * 当前对话ID
	 */
	private String dtalkSession;
	private ItemEngineHttpImpl engine;
	public ItemAdapterHttpServer()  {
		this(DEFAULT_HTTP_PORT);
	}
    public ItemAdapterHttpServer(int port)  {
        super(port);
        selfMac = FaceUtilits.toHex(DeviceUtils.DEVINFO_PROVIDER.getMac());
		// 定时检查itemAdapter工作状态，当itemAdapter 空闲超时，则中止频道
		getTimer().schedule(new TimerTask() {

			@Override
			public void run() {
				try{
					if(null != dtalkSession && null != engine && ItemAdapterHttpServer.this.isAlive()){
						long lasthit = engine.lastHitTime();
						if(System.currentTimeMillis() - lasthit > idleTimeLimit){
							dtalkSession = null;
						}
					}
				}catch (Exception e) {
					logger.error(e.getMessage());
				}
			}
		}, 0, timerPeriod);
    }

    private <T> Response responseAck(Ack<T> ack){
    	String json=BaseJsonEncoder.getEncoder().toJsonString(ack);
    	return newFixedLengthResponse(
    			Ack.Status.OK.equals(ack.getStatus()) ? Status.OK: Status.INTERNAL_ERROR, 
    			APPICATION_JSON, 
    			json);
    }
    @Override
    public Response serve(IHTTPSession session) {
    	Ack<Object> ack = new Ack<Object>().setStatus(Ack.Status.OK).setDeviceMac(selfMac);
    	try{
    		switch(session.getUri()){
    		case "/login":
    			login(session, ack);
    			break;
    		case "/logout":
    			logout(session, ack);
    			break;
    		case "/":
    		{
    			String msg = "<html><body><h1>Dtalk server</h1>\n</body></html>\n";
    			return newFixedLengthResponse(Status.OK, NanoHTTPD.MIME_HTML, msg);
    		}
    		default:
    			if(session.getUri().startsWith(DTALK_PREFIX )){
    				checkState(dtalkSession != null && dtalkSession.equals(session.getCookies().read(DTALK_SESSION)),
    						"UNAUTHORIZATION SESSION");
    				if(DTALK_PREFIX.equals(session.getUri())){
    					checkState(Method.POST.equals(session.getMethod()),"POST method supported only");
    				}
    				JSONObject jsonObject = getJSONObject(session);
    				if(session.getUri().startsWith(DTALK_PREFIX + '/')){
	    				String path=session.getUri().substring(DTALK_PREFIX.length());
	    				jsonObject.put("path", path);
    				} 
    				try {    					
    					checkNotNull(engine,"engine is uninitialized").onSubscribe(jsonObject);
    					return engine.getResponse();
					} catch (SmqUnsubscribeException e) {
						logout(session, ack);
						break;
					}     				
    			}
    			ack.setStatus(Ack.Status.ERROR).setStatusMessage("error " + session.getUri());
    		}
    	}catch (Exception e) {
    		ack.setStatus(Ack.Status.ERROR).setException(e.getClass().getName()).setStatusMessage(e.getMessage());
    	}
    	return responseAck(ack);
    }
	/**
	 * @param isMd5 
	 * @return 
	 */
	public boolean validate(String pwd, boolean isMd5) {
		checkArgument(pwd != null,"NULL PASSWORD");
		
		String admPwd = checkNotNull(DEVINFO_PROVIDER.getPassword(),"admin password for device is null");
		checkArgument(!Strings.isNullOrEmpty(pwd),"NULL REQUEST PASSWORD");
		checkArgument(!Strings.isNullOrEmpty(admPwd),"NULL ADMIN PASSWORD");
		if(isMd5){
			String pwdmd5 = FaceUtilits.getMD5String(admPwd.getBytes());
			return pwdmd5.equalsIgnoreCase(pwd);
		}else{
			return admPwd.equals(pwd);
		}
	}
	
	private Map<String, String> getParamOfPostBody(IHTTPSession session) throws IOException, ResponseException{
		Map<String,String> postData = Maps.newHashMap();
		session.parseBody(postData);
		return BaseJsonEncoder.getEncoder().fromJson(postData.get(POST_DATA), 
				new TypeReference<Map<String, String>>(){}.getType());		
	}
	@SuppressWarnings("deprecation")
	private JSONObject getJSONObject(IHTTPSession session) throws IOException, ResponseException{
    	String jsonstr;
    	if(Method.POST.equals(session.getMethod())){ 
    		Map<String,String> postData = Maps.newHashMap();
    		session.parseBody(postData);
    		jsonstr = postData.get(POST_DATA);
    	}else{
    		jsonstr = BaseJsonEncoder.getEncoder().toJsonString(session.getParms());
    	}
		return BaseJsonEncoder.getEncoder().fromJson(jsonstr,JSONObject.class);
	}
	@SuppressWarnings("deprecation")
	protected synchronized void login(IHTTPSession session, Ack<Object> ack) throws IOException, ResponseException{

    	Map<String,String> parms = Maps.newHashMap();
    	if(Method.POST.equals(session.getMethod())){    		
			parms = getParamOfPostBody(session);			
    	}else{
    		parms = session.getParms();
    	}
    	String sid=parms.get(DTALK_SESSION);
    	if(sid ==null){
    		sid=session.getCookies().read(DTALK_SESSION);
    	}
    	
    	if (dtalkSession == null || sid == null ){
			checkState(validate(parms.get("password"), 
					Boolean.valueOf(MoreObjects.firstNonNull(parms.get("isMd5"), "true"))),"INVALID REQUEST PASSWORD");
			sid = dtalkSession = Long.toHexString(System.nanoTime());
	    	session.getCookies().set(DTALK_SESSION, dtalkSession, 1);
    	}
    	checkState(Objects.equal(dtalkSession, sid),"ANOTHER CLIENT LOCKED");
        ack.setStatus(Ack.Status.OK).setStatusMessage("AUTHORIZATION OK");

	}
    @SuppressWarnings("deprecation")
	protected synchronized void logout(IHTTPSession session, Ack<Object> ack) throws IOException, ResponseException{

    	Map<String,String> parms = Maps.newHashMap();
    	if(Method.POST.equals(session.getMethod())){
    		parms = getParamOfPostBody(session);
    	}else{
    		parms = session.getParms();
    	}
    	String sid=parms.get(DTALK_SESSION);
    	if(sid ==null){
    		sid=session.getCookies().read(DTALK_SESSION);
    	}

    	if (dtalkSession != null && Objects.equal(dtalkSession, sid)){
    		dtalkSession = null;
        	ack.setStatus(Ack.Status.OK).setStatusMessage("logout OK");
    	}        
    }

	/**
	 * @return engine
	 */
	public ItemEngineHttpImpl getEngine() {
		return engine;
	}

	/**
	 * @param engine 要设置的 engine
	 * @return 
	 */
	public ItemAdapterHttpServer setItemAdapter(ItemEngineHttpImpl engine) {
		this.engine = engine;
		return this;
	}
	/**
	 * 设置定义检查连接的任务时间间隔(毫秒)
	 * @param timerPeriod
	 * @return
	 */
	public ItemAdapterHttpServer setTimerPeriod(long timerPeriod) {
		if(timerPeriod > 0){
			this.timerPeriod = timerPeriod;
		}
		return this;
	}
}

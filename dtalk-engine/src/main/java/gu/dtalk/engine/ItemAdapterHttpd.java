package gu.dtalk.engine;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static gu.dtalk.engine.DeviceUtils.DEVINFO_PROVIDER;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.TypeReference;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import gu.dtalk.Ack;
import gu.simplemq.json.BaseJsonEncoder;
import net.gdface.utils.FaceUtilits;

public class ItemAdapterHttpd extends NanoHTTPD {
	private static final Logger logger = LoggerFactory.getLogger(ItemAdapterHttpd.class);
	private static final String DTALK_SESSION="dtalk-session"; 
	private static final String APPICATION_JSON="application/json";
	private static final String POST_DATA="postData";
	private String selfMac;
	private String dtalkSession;
    public ItemAdapterHttpd(int port)  {
        super(port);
        selfMac = FaceUtilits.toHex(DeviceUtils.DEVINFO_PROVIDER.getMac());
    }

    public static void main(String[] args) {
        try {
        	logger.info("Running! Point your browsers to http://localhost:{}/",8080);
            new ItemAdapterHttpd(8080).start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        } catch (IOException ioe) {
            logger.error("Couldn't start server:" + ioe);
        }
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
    			if(!session.getUri().startsWith("/dtalk")){
    				return newFixedLengthResponse(Status.BAD_REQUEST, NanoHTTPD.MIME_HTML, "error " + session.getUri());
    			}
    			return null;
    		}
    	}catch (Exception e) {
    		ack.setStatus(Ack.Status.ERROR).setException(e.getClass().getName()).setStatusMessage(e.getMessage());
    	}
    	return responseAck(ack);
    }
	/**
	 * @return 
	 */
	public boolean validate(String pwd) {
		checkArgument(pwd != null,"NULL PASSWORD");
		
		String admPwd = checkNotNull(DEVINFO_PROVIDER.getPassword(),"admin password for device is null");
		checkArgument(!Strings.isNullOrEmpty(pwd),"NULL REQUEST PASSWORD");
		checkArgument(!Strings.isNullOrEmpty(admPwd),"NULL ADMIN PASSWORD");
		String pwdmd5 = FaceUtilits.getMD5String(admPwd.getBytes());

		return pwdmd5.equalsIgnoreCase(pwd);
	}
	
	private Map<String, String> getParamOfPostBody(IHTTPSession session) throws IOException, ResponseException{
		Map<String,String> postData = Maps.newHashMap();
		session.parseBody(postData);
		return BaseJsonEncoder.getEncoder().fromJson(postData.get(POST_DATA), 
				new TypeReference<Map<String, String>>(){}.getType());		
	}
	@SuppressWarnings("deprecation")
	protected void login(IHTTPSession session, Ack<Object> ack) throws IOException, ResponseException{

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

    	if (dtalkSession == null || sid == null || !Objects.equal(dtalkSession, sid)){
    		checkState(validate(parms.get("password")),"INVALID REQUEST PASSWORD");
    		dtalkSession = Long.toHexString(System.nanoTime());
        	session.getCookies().set(DTALK_SESSION, dtalkSession, 1);
        	ack.setStatus(Ack.Status.OK).setStatusMessage(session.getUri() + "\n" + "authorization OK");
    	}        
        ack.setStatus(Ack.Status.OK).setStatusMessage("authorization OK");
    }
    @SuppressWarnings("deprecation")
	protected void logout(IHTTPSession session, Ack<Object> ack) throws IOException, ResponseException{

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
}

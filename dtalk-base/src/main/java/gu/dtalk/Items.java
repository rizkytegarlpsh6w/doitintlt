package gu.dtalk;

import java.util.Map;

import gu.simplemq.IMessageAdapter;
import gu.simplemq.exceptions.SmqUnsubscribeException;

public class Items {
	public static final String QUIT_NAME="quit";
	public static CmdItem makeQuit(){
		CmdItem quit = new CmdItem();
		quit.setName(QUIT_NAME);
		quit.setCmdAdapter(new IMessageAdapter<Map<String,Object>>() {
			
			@Override
			public void onSubscribe(Map<String, Object> t) throws SmqUnsubscribeException {
				throw new SmqUnsubscribeException(true);
			}
		});
		return quit;
	}
}

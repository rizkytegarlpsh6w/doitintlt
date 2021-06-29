package gu.dtalk.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

import gu.dtalk.BaseOption;
import gu.dtalk.CmdItem;
import gu.dtalk.IItem;
import gu.dtalk.IMenu;
import gu.dtalk.ItemType;
import gu.dtalk.RootMenu;
import gu.simplemq.IMessageAdapter;
import gu.simplemq.exceptions.SmqUnsubscribeException;
import static com.google.common.base.Preconditions.*;

public class IItemAdapter implements IMessageAdapter<JSONObject>{
	private static final Logger logger = LoggerFactory.getLogger(IItemAdapter.class);
	private IMenu root = new RootMenu(); 
	public IItemAdapter() {
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onSubscribe(JSONObject t) throws SmqUnsubscribeException {
		try{
			IItem item = ItemType.parseItem(t);
			
			IItem found = root.recursiveFind(item.getName());
			if(null == found){
				logger.warn("UNSUPPORTED ITEM"); 
				return;
			}
			switch(found.getCatalog()){
			case OPTION:{
				Object v = ((BaseOption<Object>)item).getObjectValue();
				((BaseOption<Object>)found).setObjectValue(v);
				break;
			}
			case CMD:{
				CmdItem foundItem = (CmdItem)found;
				foundItem.runCmd();
				break;
			}
			case MENU:{
				IMenu menuItem = (IMenu) item;
				break;
			}
			default:
				logger.warn("UNSUPPORTED CATALOG"); 
			}
		}catch(Exception e){
			logger.warn(e.getMessage());
		}
	}

	public IItem getRoot() {
		return root;
	}

	public void setRoot(IMenu root) {
		this.root = checkNotNull(root);
	}

}

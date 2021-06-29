package gu.dtalk.engine;

import gu.dtalk.IItem;
import gu.simplemq.IMessageAdapter;
import gu.simplemq.exceptions.SmqUnsubscribeException;

public class IItemAdapter implements IMessageAdapter<IItem>{
	private IItem root; 
	public IItemAdapter() {
	}

	@Override
	public void onSubscribe(IItem t) throws SmqUnsubscribeException {
		
	}

	public IItem getRoot() {
		return root;
	}

	public void setRoot(IItem root) {
		this.root = root;
	}

}

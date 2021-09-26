package gu.dtalk.engine.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gu.dtalk.BaseItem;
import gu.dtalk.BaseOption;
import gu.dtalk.event.ValueListener;
import static com.google.common.base.Preconditions.*;

public class DemoListener extends ValueListener<Object> {
	static final DemoListener INSTANCE = new DemoListener();
	private static final Logger logger =LoggerFactory.getLogger(DemoListener.class);
	public DemoListener() {
	}

	@Override
	protected void doUpdte(BaseOption<Object> o, Object arg) {
		logger.info("VALUE UPDATE {}({}) = {}",o.getUiName(),o.getPath(),o.contentOfValue());
	}
	public void register(BaseItem root){
		checkNotNull(root);
		if(root instanceof BaseOption){
			@SuppressWarnings("unchecked")
			BaseOption<Object> option = (BaseOption<Object>)root;
			option.addListener(this);
		}
	}
}

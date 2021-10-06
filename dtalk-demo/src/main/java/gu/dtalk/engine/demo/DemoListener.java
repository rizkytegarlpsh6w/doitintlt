package gu.dtalk.engine.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gu.dtalk.BaseOption;
import gu.dtalk.event.ValueChangeEvent;
import gu.dtalk.event.ValueListener;

public class DemoListener extends ValueListener<Object> {
	static final DemoListener INSTANCE = new DemoListener();
	private static final Logger logger =LoggerFactory.getLogger(DemoListener.class);
	public DemoListener() {
	}

	@Override
	protected void doUpdte(ValueChangeEvent<BaseOption<Object>> event) {
		logger.info("VALUE UPDATE {}({}) = {}",
				event.option().getUiName(),
				event.option().getPath(),
				event.option().contentOfValue());
	}

}
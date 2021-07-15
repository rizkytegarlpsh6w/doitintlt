package gu.dtalk.client;

import java.io.PrintStream;

import com.google.common.base.Strings;

import gu.dtalk.Ack;
import gu.dtalk.IItem;
import gu.dtalk.IMenu;
import gu.dtalk.Ack.Status;

public class TextRender {

	private PrintStream stream = System.out;
	
	public TextRender() {
	}
	public void rendeAck(Ack<?> ack){
		Status status = ack.getStatus();
		stream.append(status.name());		
		if(status != Ack.Status.OK && !Strings.isNullOrEmpty(ack.getErrorMessage())){
			stream.append(":").append(ack.getErrorMessage());
		}
		stream.append('\n');
	}
	public void rendeItem(IMenu menu){
		stream.append(menu.getPath()).append('\n');
		int i=0;
		for(IItem item: menu.getChilds()){
			stream.printf("[%d] [%s] %s \n",
					i,
					item.isDisable()?"x":" ",
					item.getUiName());
		}
	}
	public TextRender setStream(PrintStream stream) {
		if(null != stream){
			this.stream = stream;
		}
		return this;
	}
	public PrintStream getStream() {
		return stream;
	}
}

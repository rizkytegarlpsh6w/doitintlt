package gu.dtalk.client;

import java.io.PrintStream;
import java.net.URL;

import com.google.common.base.Strings;

import gu.dtalk.Ack;
import gu.dtalk.BaseItem;
import gu.dtalk.BaseOption;
import gu.dtalk.CheckOption;
import gu.dtalk.MenuItem;
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
	public void rendeItem(MenuItem menu){
		stream.println("=========Device Menu============");
		stream.printf("-->%s\n",menu.getPath());
		int i=0;
		for(BaseItem item: menu.getChilds()){
			String acc = item.isDisable() ? "x" : " ";			
			String content = "";
			if(item instanceof BaseOption){
				BaseOption<?> option = (BaseOption<?>)item;
				content = option.contentOfValue();
				acc = option.isReadOnly() ? "R" : acc;
			}
			if(!content.isEmpty()){
				content = ": " +content;
			}			
			
			stream.printf("[%d] [%s] %s %s\n",
					i++,
					acc,
					item.getUiName(),
					content);
			if(item instanceof CheckOption){
				CheckOption<?> checkOption = (CheckOption<?>)item;
				stream.println(checkOption.contentOfOptions());
			}
		}
		stream.println("==Press number to seleect menu item(按数字选项菜单)==");
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

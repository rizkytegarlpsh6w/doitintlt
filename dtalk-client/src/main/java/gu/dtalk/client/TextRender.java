package gu.dtalk.client;

import java.io.PrintStream;

import com.alibaba.fastjson.JSONArray;
import com.google.common.base.Strings;

import gu.dtalk.Ack;
import gu.dtalk.BaseItem;
import gu.dtalk.BaseOption;
import gu.dtalk.CheckOption;
import gu.dtalk.MenuItem;
import gu.dtalk.Ack.Status;

/**
 * 文本渲染器，向{@link PrintStream}输出从设备端收到的消息
 * @author guyadong
 *
 */
public class TextRender implements ClientRender {

	private PrintStream stream = System.out;
	
	public TextRender() {
	}
	/* （非 Javadoc）
	 * @see gu.dtalk.client.ClientRender#rendeAck(gu.dtalk.Ack, boolean)
	 */
	@Override
	public void rendeAck(Ack<?> ack, boolean renderValueIfOk){
		Status status = ack.getStatus();
		if(renderValueIfOk && Ack.Status.OK == status && ack.getValue() != null){
			Object v = ack.getValue();
			if(v instanceof JSONArray){
				for (Object element : (JSONArray)v) {
					stream.append(element.toString()).append("\n");
				}				
			}else{
				stream.append(v.toString());
			}
		}else{
			stream.append(status.name());		
			if(status != Ack.Status.OK && !Strings.isNullOrEmpty(ack.getErrorMessage())){
				stream.append(":").append(ack.getErrorMessage());
			}
		}
		stream.append('\n');
	}
	/* （非 Javadoc）
	 * @see gu.dtalk.client.ClientRender#rendeItem(gu.dtalk.MenuItem)
	 */
	@Override
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
	public ClientRender setStream(PrintStream stream) {
		if(null != stream){
			this.stream = stream;
		}
		return this;
	}
	public PrintStream getStream() {
		return stream;
	}
}

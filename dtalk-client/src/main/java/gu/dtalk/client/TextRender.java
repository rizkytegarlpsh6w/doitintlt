package gu.dtalk.client;

import java.io.PrintStream;

import com.alibaba.fastjson.JSONArray;
import com.google.common.base.Strings;

import gu.dtalk.Ack;
import gu.dtalk.BaseItem;
import gu.dtalk.BaseOption;
import gu.dtalk.CheckOption;
import gu.dtalk.CmdItem;
import gu.dtalk.MenuItem;
import gu.dtalk.Ack.Status;

/**
 * 文本渲染器，向{@link PrintStream}输出从设备端收到的消息
 * @author guyadong
 *
 */
public class TextRender implements IMessageRender {

	private PrintStream stream = System.out;
	
	public TextRender() {
	}

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
				// 只读标志
				acc = option.isReadOnly() ? "R" : acc;
			}else if (item instanceof CmdItem){
				CmdItem cmd = (CmdItem)item;
				// task任务标志
				acc = (Strings.isNullOrEmpty(cmd.getTaskQueue()) || item.isDisable()) ? acc : "T"; 
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
		stream.println("==Press number to seleect menu item,'.' show current menu(按数字选项菜单,'.'显示当前菜单)==");
	}
	public IMessageRender setStream(PrintStream stream) {
		if(null != stream){
			this.stream = stream;
		}
		return this;
	}
	public PrintStream getStream() {
		return stream;
	}
}

package gu.dtalk.client;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import gu.dtalk.DeviceInstruction;
import gu.simplemq.Channel;

public class FreshedChannelSupplier implements Supplier<Channel<DeviceInstruction>>{
	private final Supplier<String> taskQueueSupplier;
	private Channel<DeviceInstruction> taskChannel;
	public FreshedChannelSupplier(Supplier<String> taskQueueSupplier) {
		this.taskQueueSupplier = checkNotNull(taskQueueSupplier,"taskQueueSupplier is null");
	}
	public FreshedChannelSupplier(String taskQueue) {
		this(Suppliers.ofInstance(checkNotNull(Strings.emptyToNull(taskQueue),"taskQueue is null or empty")));
	}
	@Override
    public Channel<DeviceInstruction> get(){
    	String name = checkNotNull(taskQueueSupplier.get(),"taskQueue provided by taskQueueSupplier  is null");
    	if(taskChannel == null || !taskChannel.name.equals(name)){
    		Channel<DeviceInstruction> ch = new Channel<DeviceInstruction>(name){};
    		taskChannel = ch;
    	}
    	return taskChannel;
    }
}

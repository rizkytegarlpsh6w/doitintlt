package gu.dtalk.engine;

import java.util.Iterator;
import java.util.ServiceLoader;

import gu.dtalk.DeviceInfoProvider;

public class DeviceUtils {

	public final static DeviceInfoProvider DEVINFO_PROVIDER = getDeviceInfoProvider();

	/**
	 * SPI(Service Provider Interface)机制加载 {@link DeviceInfoProvider}实例,没有找到返回默认实例
	 * @return {@link DeviceInfoProvider}
	 */
	private static DeviceInfoProvider getDeviceInfoProvider() {		
		ServiceLoader<DeviceInfoProvider> providers = ServiceLoader.load(DeviceInfoProvider.class);
		Iterator<DeviceInfoProvider> itor = providers.iterator();
		if(!itor.hasNext()){
			return DefaultDevInfoProvider.INSTANCE;
		}
		return itor.next();
	}

}

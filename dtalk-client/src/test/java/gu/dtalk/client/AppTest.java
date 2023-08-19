package gu.dtalk.client;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Arrays;

import org.junit.Test;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.net.HostAndPort;

import net.gdface.utils.BinaryUtils;
import net.gdface.utils.NetworkUtil;

import static com.google.common.base.Preconditions.checkArgument;

public class AppTest {
	@Test
	public void test() {

		try {
			byte[] mac = NetworkUtil.getCurrentMac("192.168.10.1", 80);
			System.out.printf("MAC:%s\n", BinaryUtils.toHex(mac));
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}

	}

}

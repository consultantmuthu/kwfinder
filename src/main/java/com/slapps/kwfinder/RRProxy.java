package com.slapps.kwfinder;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Throwables;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.nio.file.Files;

/**
 * 
 * @author Muthukumaran
 *
 */
public class RRProxy {

	private static String fqcn = RRProxy.class.getName();
	private static List<String> proxies = null;
	private static int usageCounrer = 0;

	synchronized public static Proxy getProxy() {
		if (proxies != null && proxies.size() > 0) {
			if (usageCounrer == proxies.size()) {
				usageCounrer = 0;
			}
			String proxyStr = proxies.get(usageCounrer);
			String [] proxyArr = proxyStr.split(":");
			SocketAddress addr = new InetSocketAddress(
					proxyArr[0].trim(), Integer.parseInt(proxyArr[1].trim()));
		    Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);
			Logit.log(fqcn, "Using Proxy " + proxyStr);
		    return proxy;
		} else {
			Logit.log(fqcn, "There is no Proxy to use !!!!");

		}
		return null;
	}

	synchronized public static void changeProxy() {
		usageCounrer++;
	}
	public static void setProxyFile(File file) {
		if (file != null) {
			try (Stream<String> lines = Files.lines(file.toPath())) {
				proxies = lines.filter(line -> !line.startsWith("#")).collect(Collectors.toList());
			} catch (IOException e) {
				Logit.log(fqcn, Throwables.getStackTraceAsString(e));
			}
		} else {
			Logit.log(fqcn, "Proxies are not configured...");
		}
	}
}

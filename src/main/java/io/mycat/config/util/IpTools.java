package io.mycat.config.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * 获取主机ip
 * 
 * @author shy
 * 
 * 
 */
public class IpTools {
	static private final char COLON = ':';

	public static String getMyIPLocal(String netCard) throws IOException {
		// InetAddress ia = InetAddress.getLocalHost();
		// return ia.getHostAddress();
		/**
		 * 获取当前机器ip地址 据说多网卡的时候会有问题.
		 */
		// 获取ip最后三位
		if (System.getProperty("file.separator").equals("\\")) {
			return IpTools.getMyIPWindows();
		} else {
			Enumeration<NetworkInterface> netInterfaces;
			try {
				netInterfaces = NetworkInterface.getNetworkInterfaces();
				InetAddress ip;
				while (netInterfaces.hasMoreElements()) {
					NetworkInterface netInterface = netInterfaces.nextElement();
					if (netCard.equals(netInterface.getName())) {
						Enumeration<InetAddress> addresses = netInterface
								.getInetAddresses();
						while (addresses.hasMoreElements()) {
							ip = addresses.nextElement();
							if (ip.getHostAddress().indexOf(COLON) == -1) {
								return ip.getHostAddress();
							}
						}
					}
				}
				return "";
			} catch (Exception e) {
				return "";
			}
		}
	}

	public static String getMyIPWindows() throws UnknownHostException {
		InetAddress ia = InetAddress.getLocalHost(); // 获得本机网络地址对象
		// System.out.println(ia.getHostName()); // 获得对应主机名
		return ia.getHostAddress(); // 获得对应主机地址

	}

	public static void main(String[] args) throws UnknownHostException {
		System.out.println(getMyIPWindows());
	}
}

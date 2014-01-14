package io.evercam.network.ipscan;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class PortScan
{
	// check ip:port is reachable or not, using socket connection
	public static boolean isPortReachable(String ip, int port)
	{
		try
		{
			InetAddress ip_net = InetAddress.getByName(ip);
			new Socket(ip_net, port);
			return true;
		}
		catch (IOException e)
		{
			return false;
		}
	}
	
}

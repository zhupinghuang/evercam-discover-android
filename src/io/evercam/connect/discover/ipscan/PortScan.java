package io.evercam.connect.discover.ipscan;

import io.evercam.connect.db.CameraOperation;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import android.content.Context;

public class PortScan
{

	String ssid;
	public final int[] STANDARD_PORTS = { 20, 21, 22, 80, 443, 554 };
	CameraOperation cameraOperation;

	public PortScan(String ip, String ssid, Context ctxt)
	{
		this.ssid = ssid;
		cameraOperation = new CameraOperation(ctxt);
		startScan(ip);
	}

	// scan both stand and common ports
	public void startScan(String ip)
	{
		scanByStandard(ip, STANDARD_PORTS, 0);
		scanByStandard(ip, getCommonPorts(ip), 1);
	}

	// get common ports
	public int[] getCommonPorts(String ip)
	{
		int[] commonPorts = new int[2];
		String subIp = ip.substring(ip.lastIndexOf(".") + 1, ip.length());
		int subIpInt = Integer.parseInt(subIp);
		int common_http = 8000 + subIpInt;
		int common_rtsp = 9000 + subIpInt;
		commonPorts[0] = common_http;
		commonPorts[1] = common_rtsp;
		return commonPorts;
	}

	// scan port range
	public void scanBySequence(int start, int end, String ip)
	{
		for (int port = 1; port <= end; port++)
		{
			if (isPortReachable(ip, port))
			{

			}
			else
			{

			}
		}
	}

	public void scanByStandard(String ip, int[] ports, int type)
	{
		// type = 0: stantard port
		// type = 1: common port
		int port;
		for (int i = 0; i < ports.length; i++)
		{
			port = ports[i];
			if (isPortReachable(ip, port))
			{
				String port_s = String.valueOf(port);
				switch (type)
				{
				case 0:
					if (port == 80)
					{
						cameraOperation.updateAttributeInt(ip, ssid, "http",
								port);
					}
					if (port == 554)
					{
						cameraOperation.updateAttributeInt(ip, ssid, "rtsp",
								port);
					}
					break;
				case 1:
					if (port_s.startsWith("8"))
					{
						cameraOperation.updateAttributeInt(ip, ssid, "http",
								port);

					}
					else if (port_s.startsWith("9"))
					{
						cameraOperation.updateAttributeInt(ip, ssid, "rtsp",
								port);
					}
					break;
				}
			}
			else
			{

			}
		}
	}

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

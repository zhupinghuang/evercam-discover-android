package io.evercam.connect.scan;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class IpScan
{

	public static final int DEFAULT_TIME_OUT = 2500;
	public ScanRange scanRange;
	
	public IpScan(ScanRange scanRange)
	{
		this.scanRange = scanRange;
	}
	
	public void scanAll()
	{
		
	}
	
	public static boolean scanSingleIp(String ip, int timeout)
	{
		try
		{
			InetAddress h = InetAddress.getByName(ip);
			if (h.isReachable(timeout))
			{
				return true;
			}
		}
		catch (UnknownHostException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return false;
	}

}

package io.evercam.connect.scan;

import io.evercam.connect.discover.ipscan.Host;
import io.evercam.connect.net.NetInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import android.util.Log;

public class IpScan
{
	private ScanResult scanResult;
	public static final int DEFAULT_TIME_OUT = 2500;
	private ExecutorService pool;
	private int pt_move = 2; // 1=backward 2=forward
	
	public IpScan(ScanResult scanResult)
	{
		this.scanResult = scanResult;
	}
	
	public void scanAll(ScanRange scanRange)
	{
		long ip = scanRange.getNetworkIp();
		long start = scanRange.getNetworkStart();
		long end = scanRange.getNetworkEnd();
		pool = Executors.newFixedThreadPool(10);
		if (ip <= end && ip >= start)
		{
			launch(start);

			// hosts
			long pt_backward = ip;
			long pt_forward = ip + 1;
			long size_hosts = scanRange.countSize() - 1;

			for (int i = 0; i < size_hosts; i++)
			{
				// Set pointer if of limits
				if (pt_backward <= start)
				{
					pt_move = 2;
				}
				else if (pt_forward > end)
				{
					pt_move = 1;
				}
				// Move back and forth
				if (pt_move == 1)
				{
					launch(pt_backward);
					pt_backward--;
					pt_move = 2;
				}
				else if (pt_move == 2)
				{
					launch(pt_forward);
					pt_forward++;
					pt_move = 1;
				}
			}
		}
		else
		{
			for (long i = start; i <= end; i++)
			{
				launch(i);
			}

		}
		pool.shutdown();
		try
		{
			if (!pool.awaitTermination(3600, TimeUnit.SECONDS))
			{
				pool.shutdownNow();
				if (!pool.awaitTermination(10, TimeUnit.SECONDS))
				{
					Log.e("IP Scan", "Pool did not terminate");
				}
			}
		}
		catch (InterruptedException e)
		{
			pool.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}
	
	private void launch(long i)
	{
		if (!pool.isShutdown())
		{
			pool.execute(new CheckRunnable(IpTranslator
					.getIpFromLongUnsigned(i),scanResult));
		}
	}
	
	public boolean scanSingleIp(String ip, int timeout)
	{
		try
		{
			InetAddress h = InetAddress.getByName(ip);
			if (h.isReachable(timeout))
			{
				scanResult.onActiveIp();
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

	private class CheckRunnable implements Runnable
	{
		private String addr;
		private ScanResult scanResult;

		CheckRunnable(String addr, ScanResult scanResult)
		{
			this.addr = addr;
			this.scanResult = scanResult;
		}

		@Override
		public void run()
		{
			Host host = new Host();
			host.ipAddress = addr;
			
			// Arp Check
			host.hardwareAddress = NetInfo.getHardwareAddress(addr);
			if (!host.hardwareAddress.equals(NetInfo.EMPTY_MAC))
			{
				scanResult.onActiveIp();
				return;
			}
			// Ping
			InetAddress h;
			try
			{
				h = InetAddress.getByName(addr);
				if (h.isReachable(2500))
				{
					scanResult.onActiveIp();
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
			

		}

	}
}

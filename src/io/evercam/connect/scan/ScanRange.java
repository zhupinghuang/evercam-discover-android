package io.evercam.connect.scan;

public class ScanRange
{
	private long networkIp = 0;
	private long networkStart = 0;
	private long networkEnd = 0;
	
	public ScanRange(String ip, String subnetMask)
	{
		networkIp = IpTranslator.getUnsignedLongFromIp(ip);

		int cidr = IpTranslator.maskIpToCidr(subnetMask);
		int shift = (32 - cidr);
		if (cidr < 31)
		{
			networkStart = (networkIp >> shift << shift) + 1;
			networkEnd = (networkStart | ((1 << shift) - 1)) - 1;
		}
		else
		{
			networkStart = (networkIp >> shift << shift);
			networkEnd = (networkStart | ((1 << shift) - 1));
		}
	}
	
	public long getNetworkIp()
	{
		return networkIp;
	}

	public long getNetworkStart()
	{
		return networkStart;
	}

	public long getNetworkEnd()
	{
		return networkEnd;
	}

	public void setNetworkIp(long networkIp)
	{
		this.networkIp = networkIp;
	}

	public void setNetworkStart(long networkStart)
	{
		this.networkStart = networkStart;
	}

	public void setNetworkEnd(long networkEnd)
	{
		this.networkEnd = networkEnd;
	}

}

package io.evercam.network.ipscan;

public class ScanRange
{
	private long networkIp;
	private long networkStart;
	private long networkEnd;
	
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
	
	protected int countSize()
	{
		return (int) (networkEnd - networkStart + 1);
	}
	
	protected long getNetworkIp()
	{
		return networkIp;
	}

	protected long getNetworkStart()
	{
		return networkStart;
	}

	protected long getNetworkEnd()
	{
		return networkEnd;
	}

	protected void setNetworkIp(long networkIp)
	{
		this.networkIp = networkIp;
	}

	protected void setNetworkStart(long networkStart)
	{
		this.networkStart = networkStart;
	}

	protected void setNetworkEnd(long networkEnd)
	{
		this.networkEnd = networkEnd;
	}

}

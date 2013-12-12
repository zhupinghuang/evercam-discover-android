package io.evercam.connect.discover.ipscan;

import io.evercam.connect.Constants;
import io.evercam.connect.net.NetInfo;

public class Host
{
	public int deviceType;
	public String ipAddress;
	public String hardwareAddress;
	public String vendor;

	public Host()
	{
		deviceType = Constants.TYPE_OTHERS;
		ipAddress = null;
		hardwareAddress = NetInfo.EMPTY_MAC;
		vendor = "Unknown";
	}
}

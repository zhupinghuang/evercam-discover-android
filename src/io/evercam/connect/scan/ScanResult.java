package io.evercam.connect.scan;

import io.evercam.connect.discover.ipscan.Host;

public interface ScanResult
{
	public void onActiveIp(Host host);
	public void onActiveIp();
}

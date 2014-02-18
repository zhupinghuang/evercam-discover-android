package io.evercam.connect.discover.ipscan;

import io.evercam.connect.db.CameraOperation;
import io.evercam.network.ipscan.PortScan;
import io.evercam.network.ipscan.PortScanResult;

import android.content.Context;

public class EvercamPortScan
{

	String ssid;
	CameraOperation cameraOperation;

	public EvercamPortScan(final String ip, final String ssid, Context ctxt)
	{
		this.ssid = ssid;
		cameraOperation = new CameraOperation(ctxt);
		PortScan portScan = new PortScan(new PortScanResult(){
			@Override
			public void onPortActive(int port, int type)
			{
				String port_s = String.valueOf(port);
				switch (type)
				{
				case PortScan.TYPE_STANDARD:
					if (port == 80)
					{
						cameraOperation.updateAttributeInt(ip, ssid, "http", port);
					}
					if (port == 554)
					{
						cameraOperation.updateAttributeInt(ip, ssid, "rtsp", port);
					}
					break;
				case PortScan.TYPE_COMMON:
					if (port_s.startsWith("8"))
					{
						cameraOperation.updateAttributeInt(ip, ssid, "http", port);

					}
					else if (port_s.startsWith("9"))
					{
						cameraOperation.updateAttributeInt(ip, ssid, "rtsp", port);
					}
					break;
				}
			}
		});
		portScan.start(ip);
	}
}

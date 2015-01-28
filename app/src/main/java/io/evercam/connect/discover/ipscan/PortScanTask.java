package io.evercam.connect.discover.ipscan;

import io.evercam.connect.db.CameraOperation;
import io.evercam.network.discovery.PortScan;
import io.evercam.network.discovery.PortScanResult;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class PortScanTask extends AsyncTask<Void, Void, Void>
{
	private final String TAG = "evercamdiscover-PortScanTask";
	private String ssid;
	private String ip;
	private CameraOperation cameraOperation;

	public PortScanTask(final String ip, final String ssid, Context ctxt)
	{
		this.ip = ip;
		this.ssid = ssid;
		cameraOperation = new CameraOperation(ctxt);
	}

	@Override
	protected Void doInBackground(Void... params)
	{
		try
		{
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
		catch (Exception e)
		{
			Log.e(TAG, e.toString());
		}
		return null;
	}
}

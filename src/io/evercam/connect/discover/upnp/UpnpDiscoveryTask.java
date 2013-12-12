package io.evercam.connect.discover.upnp;

import io.evercam.connect.DiscoverMainActivity;
import io.evercam.connect.db.Camera;
import io.evercam.connect.db.CameraOperation;
import io.evercam.connect.net.NetInfo;

import java.io.IOException;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import net.sbbi.upnp.Discovery;
import net.sbbi.upnp.devices.UPNPRootDevice;

public class UpnpDiscoveryTask extends AsyncTask<Void, Void, Void>
{
	private UPNPRootDevice[] devices = null;
	private CameraOperation cameraOperation;
	private NetInfo netInfo;

	public UpnpDiscoveryTask(Context ctxt)
	{
		cameraOperation = new CameraOperation(ctxt);
		netInfo = new NetInfo(ctxt);
	}

	@Override
	protected Void doInBackground(Void... arg0)
	{
		discoverAll();
		return null;
	}

	public void discoverAll()
	{
		try
		{
			devices = Discovery.discover(Discovery.DEFAULT_TIMEOUT,
					Discovery.DEFAULT_TTL, Discovery.DEFAULT_MX,
					"upnp:rootdevice", null);
			if (devices.length != 0) for (int i = 0; i < devices.length; i++)
			{
				Camera deviceFromUPNP = getDeviceFromUpnp(getDevices()[i]);
				if (deviceFromUPNP != null)
				{
					if (cameraOperation.isExisting(deviceFromUPNP.getIP(),
							netInfo.getSsid()))
					{
						cameraOperation.updateUpnpCamera(deviceFromUPNP,
								netInfo.getSsid());
					}
					else
					{
						cameraOperation.insertCamera(deviceFromUPNP,
								netInfo.getSsid());
					}
				}

			}
			else
			{
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (NullPointerException e)
		{
			Log.e("UPnPDiscovery", "no upnp device");
		}

	}

	public String getIPFromUpnp(UPNPRootDevice upnpDevice)
	{
		if (upnpDevice.getPresentationURL() != null)
		{
			return upnpDevice.getPresentationURL().getHost();
		}
		else
		{
			return null;
		}
	}

	public int getPortFromUpnp(UPNPRootDevice upnpDevice)
	{
		if (upnpDevice.getPresentationURL() != null)
		{
			return upnpDevice.getPresentationURL().getPort();
		}

		return 0;
	}

	public String getModelFromUpnp(UPNPRootDevice upnpDevice)
	{
		String modelName = upnpDevice.getModelName();
		return modelName;
	}

	public UPNPRootDevice[] getDevices()
	{
		return devices;
	}

	public Camera getDeviceFromUpnp(UPNPRootDevice upnpDevice)
	{
		if (getIPFromUpnp(upnpDevice) != null)
		{
			Camera camera = new Camera(getIPFromUpnp(upnpDevice));
			camera.setModel(getModelFromUpnp(upnpDevice));
			camera.setHttp(getPortFromUpnp(upnpDevice));
			camera.setUpnp(1);
			camera.setFirstSeen(DiscoverMainActivity.getSystemTime());
			camera.setLastSeen(DiscoverMainActivity.getSystemTime());
			return camera;
		}
		else
		{
			return null;
		}
	}

}

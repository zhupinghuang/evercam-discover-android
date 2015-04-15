package io.evercam.connect.discover.upnp;

import android.content.Context;
import android.os.AsyncTask;

import io.evercam.connect.DiscoverMainActivity;
import io.evercam.connect.db.Camera;
import io.evercam.connect.db.CameraOperation;
import io.evercam.connect.net.NetInfo;
import io.evercam.network.discovery.UpnpDevice;
import io.evercam.network.discovery.UpnpDiscovery;
import io.evercam.network.discovery.UpnpResult;

public class UpnpDiscoveryTask extends AsyncTask<Void, Void, Void>
{
	private CameraOperation cameraOperation;
	private NetInfo netInfo;
	private UpnpDiscovery upnpDiscovery;

	public UpnpDiscoveryTask(Context ctxt)
	{
		cameraOperation = new CameraOperation(ctxt);
		netInfo = new NetInfo(ctxt);
	}

	@Override
	protected Void doInBackground(Void... arg0)
	{
		upnpDiscover();
		return null;
	}

	private void upnpDiscover()
	{
		upnpDiscovery = new UpnpDiscovery(new UpnpResult(){

			@Override
			public void onUpnpDeviceFound(UpnpDevice upnpDevice)
			{
				Camera deviceFromUPNP = getDeviceFromUpnp(upnpDevice);
				if (deviceFromUPNP != null)
				{
					if (cameraOperation.isExisting(deviceFromUPNP.getIP(), netInfo.getSsid()))
					{
						cameraOperation.updateUpnpCamera(deviceFromUPNP, netInfo.getSsid());
					}
					else
					{
						cameraOperation.insertCamera(deviceFromUPNP, netInfo.getSsid());
					}
				}
			}
		});
		upnpDiscovery.discoverAll();
	}

	public Camera getDeviceFromUpnp(UpnpDevice upnpDevice)
	{
		if (upnpDevice.getIp()!= null && !upnpDevice.getIp().isEmpty())
		{
			Camera camera = new Camera(upnpDevice.getIp());
			camera.setModel(upnpDevice.getModel());
			camera.setHttp(upnpDevice.getPort());
			camera.setUpnp(1);
			camera.setActive(1);
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

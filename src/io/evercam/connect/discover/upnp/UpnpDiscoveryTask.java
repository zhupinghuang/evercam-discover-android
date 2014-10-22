package io.evercam.connect.discover.upnp;

import io.evercam.connect.DiscoverMainActivity;
import io.evercam.connect.db.Camera;
import io.evercam.connect.db.CameraOperation;
import io.evercam.connect.net.NetInfo;
import io.evercam.network.discovery.UpnpDiscovery;
import io.evercam.network.discovery.UpnpResult;

import android.content.Context;
import android.os.AsyncTask;
import net.sbbi.upnp.devices.UPNPRootDevice;

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
			public void onUpnpDeviceFound(UPNPRootDevice upnpDevice)
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

	public Camera getDeviceFromUpnp(UPNPRootDevice upnpDevice)
	{
		if (UpnpDiscovery.getIPFromUpnp(upnpDevice) != null)
		{
			Camera camera = new Camera(UpnpDiscovery.getIPFromUpnp(upnpDevice));
			camera.setModel(UpnpDiscovery.getModelFromUpnp(upnpDevice));
			camera.setHttp(UpnpDiscovery.getPortFromUpnp(upnpDevice));
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

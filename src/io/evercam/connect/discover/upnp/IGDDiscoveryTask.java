package io.evercam.connect.discover.upnp;

import io.evercam.connect.db.Camera;
import io.evercam.connect.db.CameraOperation;
import io.evercam.connect.net.NetInfo;
import io.evercam.network.discovery.GatewayDevice;
import io.evercam.network.discovery.NatMapEntry;

import java.io.IOException;
import net.sbbi.upnp.messages.UPNPResponseException;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/**
 * gatewayDeviceTask
 * 
 * AsyncTask using gatewayDevice, along with database operations.
 */

public class IGDDiscoveryTask extends AsyncTask<Void, Void, Void>
{
	private final String TAG = "evercamconnect-gatewayDeviceTask";
	private GatewayDevice gatewayDevice;
	private NetInfo netInfo;
	private CameraOperation cameraOperation;

	public IGDDiscoveryTask(Context ctxt)
	{
		this.netInfo = new NetInfo(ctxt);
		cameraOperation = new CameraOperation(ctxt);
	}

	@Override
	protected Void doInBackground(Void... params)
	{
		try
		{
			gatewayDevice = new GatewayDevice(netInfo.getGatewayIp());
			fillRouter();
			fillAllEntries();
		}
		catch (Exception e)
		{
			Log.e(TAG, e.toString());
		}
		return null;
	}

	public void fillRouter()
	{
		if (gatewayDevice.isRouter())
		{
			cameraOperation
					.updateAttributeInt(netInfo.getGatewayIp(), netInfo.getSsid(), "upnp", 1);
		}
		else
		{
			cameraOperation
					.updateAttributeInt(netInfo.getGatewayIp(), netInfo.getSsid(), "upnp", 0);
		}
	}

	public void fillAllEntries()
	{
		for (int sizeIndex = 0; sizeIndex < gatewayDevice.getTableSize(); sizeIndex++)
		{
			try
			{
				NatMapEntry mapEntry = new NatMapEntry(gatewayDevice.getIGD().getGenericPortMappingEntry(sizeIndex));
				String natIP = mapEntry.getIpAddress();
				int natInternalPort = mapEntry.getInternalPort();
				int natExternalPort = mapEntry.getExternalPort();
				if (cameraOperation.isExisting(natIP, netInfo.getSsid()))
				{
					Camera camera = cameraOperation.getCamera(natIP, netInfo.getSsid());
					if (natInternalPort == camera.getHttp())
					{
						cameraOperation.updateAttributeInt(natIP, netInfo.getSsid(), "exthttp",
								natExternalPort);
					}
					else if (natInternalPort == camera.getRtsp())
					{
						cameraOperation.updateAttributeInt(natIP, netInfo.getSsid(), "extrtsp",
								natExternalPort);
					}
				}

			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (UPNPResponseException e)
			{
				e.printStackTrace();
			}
		}
	}

}

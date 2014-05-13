package io.evercam.connect.discover.upnp;

import io.evercam.connect.db.Camera;
import io.evercam.connect.db.CameraOperation;
import io.evercam.connect.net.NetInfo;
import io.evercam.network.upnp.IGDDiscovery;
import io.evercam.network.upnp.UpnpDiscovery;

import java.io.IOException;

import com.bugsense.trace.BugSenseHandler;

import net.sbbi.upnp.messages.ActionResponse;
import net.sbbi.upnp.messages.UPNPResponseException;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/**
 * IGDDiscoveryTask
 * 
 * AsyncTask using IGDDiscovery, along with database operations.
 */

public class IGDDiscoveryTask extends AsyncTask<Void, Void, Void>
{
	private final String TAG = "evercamconnect-IGDDiscoveryTask";
	private IGDDiscovery igdDiscovery;
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
			igdDiscovery = new IGDDiscovery(netInfo.getGatewayIp());
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
		if (igdDiscovery.isRouterIGD)
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
		for (int sizeIndex = 0; sizeIndex < igdDiscovery.tableSize; sizeIndex++)
		{
			try
			{
				ActionResponse mapEntry = igdDiscovery.IGD.getGenericPortMappingEntry(sizeIndex);
				String natIP = mapEntry
						.getOutActionArgumentValue(UpnpDiscovery.UPNP_KEY_INTERNAL_CLIENT);
				int natInternalPort = Integer.parseInt(mapEntry
						.getOutActionArgumentValue(UpnpDiscovery.UPNP_KEY_INTERNAL_PORT));
				int natExternalPort = Integer.parseInt(mapEntry
						.getOutActionArgumentValue(UpnpDiscovery.UPNP_KEY_EXTERNAL_PORT));
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

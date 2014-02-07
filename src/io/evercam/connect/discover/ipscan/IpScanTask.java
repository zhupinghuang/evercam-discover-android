package io.evercam.connect.discover.ipscan;

import io.evercam.connect.DiscoverMainActivity;
import io.evercam.connect.db.Camera;
import io.evercam.connect.db.CameraOperation;
import io.evercam.connect.db.SimpleDBConnect;
import io.evercam.connect.helper.AwsS3Uploader;
import io.evercam.connect.helper.Constants;
import io.evercam.connect.helper.JsonMessage;
import io.evercam.connect.helper.PropertyReader;
import io.evercam.connect.net.NetInfo;
import io.evercam.network.ipscan.IpScan;
import io.evercam.network.ipscan.ScanRange;
import io.evercam.network.ipscan.ScanResult;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

public class IpScanTask extends AsyncTask<Void, Host, Void>
{
	private ExecutorService pool;
	final protected WeakReference<DiscoverMainActivity> mainDiscover;
	ScanRange scanRange;

	public IpScanTask(DiscoverMainActivity ipmainDiscover, ScanRange scanRange)
	{
		mainDiscover = new WeakReference<DiscoverMainActivity>(ipmainDiscover);
		this.scanRange = scanRange;
	}
	
	@Override
	protected void onProgressUpdate(Host... host)
	{
		if (mainDiscover != null)
		{
			final DiscoverMainActivity discover = mainDiscover.get();
			if (discover != null)
			{
				if (!isCancelled())
				{
					if (host[0] != null)
					{
						discover.addHost(host[0]);
					}
				}
			}
		}
	}

	@Override
	protected Void doInBackground(Void... params)
	{
		if (mainDiscover != null)
		{
			final DiscoverMainActivity discover = mainDiscover.get();
			if (discover != null)
			{
				IpScan ipScan = new IpScan(new ScanResult(){

					@Override
					public void onActiveIp(String ip)
					{
						Host host = new Host();
						host.setIpAddress(ip);
						host.setHardwareAddress(NetInfo.getHardwareAddress(ip));
						if (!host.hardwareAddress.equals(NetInfo.EMPTY_MAC))
						{
							publish(host);
						}
						
					}});
				this.pool = ipScan.pool;
				ipScan.scanAll(scanRange);
			}
		}
		return null;
	}

	@Override
	protected void onPostExecute(Void unused)
	{
		if (mainDiscover != null)
		{
			final DiscoverMainActivity discover = mainDiscover.get();
			if (discover != null)
			{
				discover.stopDiscovery();

				// Evercam data collection, only enabled if property
				// 'EnableDataCollection' exists in property file.
				startEvercamDataCollection();
			}
		}
	}

	@Override
	protected void onCancelled()
	{
		if (pool != null)
		{
			synchronized (pool)
			{
				pool.shutdownNow();
			}
		}
		super.onCancelled();
	}

	private void publish(final Host host)
	{
		if (host == null)
		{
			publishProgress((Host) null);
			return;
		}

		if (mainDiscover != null)
		{
			final DiscoverMainActivity discover = mainDiscover.get();
			if (discover != null)
			{
				// Mac Addr not already detected
				if (!host.hardwareAddress.equals(NetInfo.EMPTY_MAC))
				{
					host.hardwareAddress = NetInfo
							.getHardwareAddress(host.ipAddress);
				}

				// NIC vendor
				SimpleDBConnect simpleDBConnect = new SimpleDBConnect(
						mainDiscover.get().getApplicationContext());
				host.vendor = simpleDBConnect
						.getVendorFromMac(host.hardwareAddress);

				// Is camera
				if (simpleDBConnect.isCameraVendor(host.hardwareAddress))
				{
					host.deviceType = Constants.TYPE_CAMERA;
				}

				// Is gateway
				if (discover.netInfo.getGatewayIp().equals(host.ipAddress))
				{
					host.deviceType = Constants.TYPE_ROUTER;
				}
			}
		}

		publishProgress(host);
	}

	private void sendFeedBack()
	{
		SharedPreferences sharedPrefs;
		NetInfo netInfo = new NetInfo(mainDiscover.get()
				.getApplicationContext());
		sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(mainDiscover.get()
						.getApplicationContext());
		String userFirstName = "";
		String userLastName = "";
		String userEmail = "";
		if (sharedPrefs.getString(Constants.KEY_USER_EMAIL, null) != null)
		{
			userEmail = sharedPrefs.getString(Constants.KEY_USER_EMAIL, null);
			userFirstName = sharedPrefs.getString(
					Constants.KEY_USER_FIRST_NAME, null);
			userLastName = sharedPrefs.getString(Constants.KEY_USER_LAST_NAME,
					null);
		}

		CameraOperation cameraOperation = new CameraOperation(mainDiscover
				.get().getApplicationContext());
		ArrayList<Camera> list = cameraOperation.selectAllIP(netInfo.getSsid());

		JsonMessage jsonMessage = new JsonMessage();
		String uploadContent = jsonMessage.getAllDataJsonMsg(list,
				userFirstName + " " + userLastName, userEmail, netInfo);

		Date date = new Date(System.currentTimeMillis());
		String uploadTitle = userEmail + " " + date;
		new AwsS3Uploader(uploadTitle, uploadContent, mainDiscover.get()
				.getApplicationContext());

	}

	private void startEvercamDataCollection()
	{
		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(mainDiscover.get()
						.getApplicationContext());
		boolean isDataCollectionAllowed = sharedPrefs.getBoolean(
				Constants.KEY_USER_DATA, true);
		PropertyReader propertyReader = new PropertyReader(mainDiscover.get()
				.getApplicationContext());
		if (isDataCollectionAllowed
				&& propertyReader
						.isPropertyExist(Constants.PROPERTY_KEY_DATA_COLLECTION))
		{
			sendFeedBack();
		}
	}
}

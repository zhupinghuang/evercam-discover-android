package io.evercam.connect.discover.ipscan;

import io.evercam.connect.Constants;
import io.evercam.connect.DiscoverMainActivity;
import io.evercam.connect.PropertyReader;
import io.evercam.connect.db.AwsS3Uploader;
import io.evercam.connect.db.Camera;
import io.evercam.connect.db.CameraOperation;
import io.evercam.connect.db.JsonMessage;
import io.evercam.connect.db.SimpleDBConnect;
import io.evercam.connect.net.NetInfo;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

public class IpScan extends AsyncTask<Void, Host, Void>
{
	private ExecutorService pool;
	final protected WeakReference<DiscoverMainActivity> mainDiscover;
	protected long ip;
	protected long start = 0;
	protected long end = 0;
	protected long size = 0;
	private int pt_move = 2; // 1=backward 2=forward

	public IpScan(DiscoverMainActivity ipmainDiscover)
	{
		mainDiscover = new WeakReference<DiscoverMainActivity>(ipmainDiscover);
	}

	public void setNetwork(long ip, long start, long end)
	{
		this.ip = ip;
		this.start = start;
		this.end = end;
	}

	private final static String TAG = "IP Scan";

	@Override
	protected void onPreExecute()
	{
		size = (int) (end - start + 1);
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
					if (host[0]!= null)
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
				pool = Executors.newFixedThreadPool(10);
				if (ip <= end && ip >= start)
				{
					launch(start);

					// hosts
					long pt_backward = ip;
					long pt_forward = ip + 1;
					long size_hosts = size - 1;

					for (int i = 0; i < size_hosts; i++)
					{
						// Set pointer if of limits
						if (pt_backward <= start)
						{
							pt_move = 2;
						}
						else if (pt_forward > end)
						{
							pt_move = 1;
						}
						// Move back and forth
						if (pt_move == 1)
						{
							launch(pt_backward);
							pt_backward--;
							pt_move = 2;
						}
						else if (pt_move == 2)
						{
							launch(pt_forward);
							pt_forward++;
							pt_move = 1;
						}
					}
				}
				else
				{
					for (long i = start; i <= end; i++)
					{
						launch(i);
					}

				}
				pool.shutdown();
				try
				{
					if (!pool.awaitTermination(3600, TimeUnit.SECONDS))
					{
						pool.shutdownNow();
						if (!pool.awaitTermination(10, TimeUnit.SECONDS))
						{
							Log.e(TAG, "Pool did not terminate");
						}
					}
				}
				catch (InterruptedException e)
				{
					pool.shutdownNow();
					Thread.currentThread().interrupt();
				}
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
				
				//Evercam data collection, only enabled if property 'EnableDataCollection' exists in property file.
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

	private void launch(long i)
	{
		if (!pool.isShutdown())
		{
			pool.execute(new CheckRunnable(NetInfo.getIpFromLongUnsigned(i)));
		}
	}

	private class CheckRunnable implements Runnable
	{
		private String addr;

		CheckRunnable(String addr)
		{
			this.addr = addr;
		}

		@Override
		public void run()
		{
			final Host host = new Host();
			host.ipAddress = addr;
			try
			{
				InetAddress h = InetAddress.getByName(addr);

				// Arp Check #1
				host.hardwareAddress = NetInfo.getHardwareAddress(addr);
				if (!host.hardwareAddress.equals(NetInfo.EMPTY_MAC))
				{
					publish(host);
					return;
				}
				// Native InetAddress check (PING)
				if (h.isReachable(2500))
				{
					host.hardwareAddress = NetInfo.getHardwareAddress(addr);
					publish(host);
					return;
				}
				// Arp Check #2
				host.hardwareAddress = NetInfo.getHardwareAddress(addr);
				if (!host.hardwareAddress.equals(NetInfo.EMPTY_MAC))
				{
					publish(host);
					return;
				}

			}
			catch (IOException e)
			{
				Log.e(TAG, e.getMessage());
			}
		}
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
		PropertyReader propertyReader = new PropertyReader(mainDiscover.get().getApplicationContext());
		if (isDataCollectionAllowed
				&& propertyReader.isPropertyExist(Constants.PROPERTY_KEY_DATA_COLLECTION))
		{
			sendFeedBack();
		}
	}
}

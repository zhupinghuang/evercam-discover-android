package io.evercam.connect;

import io.evercam.connect.db.Camera;
import io.evercam.connect.db.CameraOperation;
import io.evercam.connect.db.ResourceHelper;
import io.evercam.connect.discover.bonjour.JmdnsDiscover;
import io.evercam.connect.discover.ipscan.Host;
import io.evercam.connect.discover.ipscan.IpScanTask;
import io.evercam.connect.discover.ipscan.PortScan;
import io.evercam.connect.discover.upnp.IGDDiscoveryTask;
import io.evercam.connect.discover.upnp.UpnpDiscoveryTask;
import io.evercam.connect.net.NetInfo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import com.bugsense.trace.BugSenseHandler;

import io.evercam.connect.R;
import io.evercam.network.ipscan.IpTranslator;
import io.evercam.network.ipscan.ScanRange;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * DiscoverMainActivitysetScanRange()
 * 
 * App Entry page, start scan and show discovered devices.
 */

public class DiscoverMainActivity extends Activity
{

	private Handler handler = new Handler();
	private SimpleAdapter deviceAdapter;
	private ArrayList<HashMap<String, Object>> deviceArraylist;
	public NetInfo netInfo;
	private Context ctxt;
	private TextView scanning_text;
	private ProgressBar progressbar;
	private IpScanTask ipScanTask = null;
	private ArrayList<Camera> cameraList;
	private CameraOperation cameraOperation;
	private Camera camera;
	private MenuItem menuRefresh;
	private MenuItem menuSignIn;
	private MenuItem menuSignOut;
	private MenuItem menuSettings;
	private UpnpDiscoveryTask upnpDiscoveryTask;
	private SharedPreferences sharedPrefs;
	private boolean isWifiConnected = false;
	private boolean isEthernetConnected = false;
	private boolean isShowCameraOnly;
	private PropertyReader propertyReader;
	ScanRange scanRange;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		ctxt = getApplicationContext();
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(false);

		cameraOperation = new CameraOperation(ctxt);

		propertyReader = new PropertyReader(ctxt);

		// Bug Sense
		if (propertyReader.isPropertyExist(Constants.PROPERTY_KEY_BUG_SENSE))
		{
			String bugSenseCode = propertyReader.getPropertyStr(Constants.PROPERTY_KEY_BUG_SENSE);
			BugSenseHandler.initAndStartSession(DiscoverMainActivity.this,
					bugSenseCode);
		}
		setContentView(R.layout.activity_evercam_discover);
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);

		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		netInfo = new NetInfo(ctxt);

		// discovered device list
		final ListView deviceList = (ListView) findViewById(R.id.device_list);
		deviceList.addHeaderView(LayoutInflater.from(this).inflate(
				R.layout.header_layout, null));

		scanning_text = (TextView) findViewById(R.id.scanning_text1);
		progressbar = (ProgressBar) findViewById(R.id.processBar1);
		deviceArraylist = new ArrayList<HashMap<String, Object>>();
		deviceAdapter = new SimpleAdapter(this, deviceArraylist,
				R.layout.ditail_relative_layout, new String[] { "device_img",
						"device_name", "device_mac", "device_vendor",
						"device_model", "device_http", "device_rtsp",
						"device_timediff" }, new int[] { R.id.device_img,
						R.id.device_name, R.id.device_mac, R.id.device_vendor,
						R.id.device_model, R.id.device_http, R.id.device_rtsp,
						R.id.time_diff });
		deviceList.setAdapter(deviceAdapter);

		LinearLayout sampleLayout = (LinearLayout) findViewById(R.id.sample_layout);
		sampleLayout.setFocusable(true);
		sampleLayout.setClickable(true);
		sampleLayout.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v)
			{
				if (!netInfo.hasActiveNetwork())
				{
					makeToast(ctxt.getResources().getString(
							R.string.pleaseConnectNetwork));
				}
				else
				{
					Intent intent = new Intent();
					intent.setClass(DiscoverMainActivity.this,
							CameraDetailActivity.class);
					intent.putExtra("IP", propertyReader.getPropertyStr(Constants.PROPERTY_KEY_SAMPLE_IP));
					intent.putExtra("SSID", "sample");
					startActivity(intent);
				}
			}
		});

		deviceList.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3)
			{
				@SuppressWarnings("unchecked")
				HashMap<String, Object> map = (HashMap<String, Object>) deviceList
						.getItemAtPosition(position);
				final String deviceIp = (String) map.get("device_name");

				if (cameraOperation.isExisting(deviceIp, netInfo.getSsid()))
				{
					if ((cameraOperation.getCamera(deviceIp, netInfo.getSsid())
							.getFlag() == Constants.TYPE_ROUTER))
					{
						Intent intent = new Intent();
						intent.setClass(DiscoverMainActivity.this,
								RouterActivity.class);
						startActivity(intent);
					}
					else
					{
						Intent intent = new Intent();
						intent.setClass(DiscoverMainActivity.this,
								CameraDetailActivity.class);
						intent.putExtra("IP", deviceIp);
						intent.putExtra("SSID", netInfo.getSsid());
						startActivity(intent);
					}
				}
				else
				{
					makeToast("Device not exists!");
				}
			}

		});

		scanning_text.setClickable(true);
		scanning_text.setFocusable(true);
		scanning_text.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v)
			{
				AlertDialog alertMsg = new AlertDialog.Builder(
						DiscoverMainActivity.this)

						.setMessage(R.string.confirmStopScan)
						.setPositiveButton(R.string.yes,
								new DialogInterface.OnClickListener(){
									@Override
									public void onClick(DialogInterface dialog,
											int which)
									{
										stopDiscovery();
									}
								})
						.setNegativeButton(R.string.no,
								new DialogInterface.OnClickListener(){
									@Override
									public void onClick(DialogInterface dialog,
											int which)
									{
										return;
									}
								}).create();
				alertMsg.show();
			}
		});
		handler.postDelayed(new Runnable(){
			@Override
			public void run()
			{
				setUp();
				startIpScan();
			}
		}, 1000);
	}

	@Override
	protected void onStart()
	{
		super.onStart();

		if (propertyReader.isPropertyExist(Constants.PROPERTY_KEY_BUG_SENSE))
		{
			BugSenseHandler.startSession(this);
		}
	}

	@Override
	protected void onRestart()
	{
		super.onRestart();
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		if (netInfo.isWifiConnected(ctxt))
		{
			isShowCameraOnly = sharedPrefs.getBoolean(
					Constants.KEY_SHOW_CAMERA_ONLY, false);
			if (ipScanTask == null)
			{
				updateShowList();
			}
		}

	}

	@Override
	protected void onStop()
	{
		super.onStop();
		if (propertyReader.isPropertyExist(Constants.PROPERTY_KEY_BUG_SENSE))
		{
			BugSenseHandler.closeSession(this);
		}
	}

	private void setUp()
	{

		TextView ssid_text = (TextView) findViewById(R.id.ssid_text1);

		// get wifi info
		ctxt = getApplicationContext();
		netInfo = new NetInfo(ctxt);

		if (!netInfo.isWifiConnected(ctxt))
		{
			if (netInfo.isEthernetConnected())
			{
				ssid_text.setText("Connected to : Ethernet");
				isEthernetConnected = true;
			}
			else
			{
				ssid_text.setText(R.string.no_wifi);
				showWifiNotConnectDialog();
				showLastScanResults(Constants.TYPE_SHOW_ALL);
			}
		}
		else
		{
			isWifiConnected = true;
			ssid_text.setText("Connected to: " + netInfo.getSsid());
			SharedPreferences.Editor editor = sharedPrefs.edit();
			editor.putString(Constants.KEY_LAST_SSID, netInfo.getSsid());
			editor.commit();

			// Bonjour
			JmdnsDiscover jmdnsDiscover = new JmdnsDiscover(netInfo, ctxt);
			jmdnsDiscover.startJmdnsDiscovery();

			// upnp
			upnpDiscoveryTask = new UpnpDiscoveryTask(ctxt);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			{
				upnpDiscoveryTask
						.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
			else
			{
				upnpDiscoveryTask.execute();
			}
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		menu.clear();
		getMenuInflater().inflate(R.menu.evercam_discover, menu);
		menuRefresh = menu.findItem(R.id.action_refresh);
		menuSignIn = menu.findItem(R.id.action_signIn);
		menuSignOut = menu.findItem(R.id.action_signOut);
		menuSettings = menu.findItem(R.id.action_settings);

		menuSettings.setVisible(true);

		if (ipScanTask != null)
		{
			menuRefresh.setIcon(R.drawable.ic_cancel);

		}
		else
		{
			menuRefresh.setIcon(R.drawable.ic_menu_refresh);
		}
		menuSignIn.setVisible(false);
		menuSignOut.setVisible(false);

		if (sharedPrefs.getString("UserEmail", null) != null)
		{
			menuSignIn.setVisible(false);
			menuSignOut.setVisible(true);
		}
		else
		{
			menuSignIn.setVisible(true);
			menuSignOut.setVisible(false);
		}

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item)
	{
		if (item.getItemId() == R.id.action_refresh)
		{
			if (ipScanTask != null)
			{
				showConfirmScanDialog();
			}
			else
			{
				makeToast("Refreshing");

				deviceArraylist.clear();

				handler.postDelayed(new Runnable(){
					@Override
					public void run()
					{
						if (!(ipScanTask == null))
						{
							cancelTasks();
						}
						setUp();
						startIpScan();
					}
				}, 1000);
			}
		}
		else if (item.getItemId() == R.id.action_signIn)

		{
			Intent intentSignIn = new Intent();
			intentSignIn.setClass(DiscoverMainActivity.this,
					LoginActivity.class);
			startActivity(intentSignIn);
		}

		else if (item.getItemId() == R.id.action_signOut)

		{
			AlertDialog confirmSignoutDialog = new AlertDialog.Builder(
					DiscoverMainActivity.this)

					.setMessage(R.string.confirmSignOutMsg)
					.setPositiveButton(R.string.yes,
							new DialogInterface.OnClickListener(){
								@Override
								public void onClick(DialogInterface dialog,
										int which)
								{
									SharedPreferences.Editor editor = sharedPrefs
											.edit();
									editor.putString(Constants.KEY_USER_EMAIL,
											null);
									editor.putString(
											Constants.KEY_USER_FIRST_NAME, null);
									editor.putString(
											Constants.KEY_USER_LAST_NAME, null);
									editor.commit();
									menuSignIn.setVisible(true);
									menuSignOut.setVisible(false);
								}
							})
					.setNegativeButton(R.string.no,
							new DialogInterface.OnClickListener(){
								@Override
								public void onClick(DialogInterface dialog,
										int which)
								{
									return;
								}
							}).create();
			confirmSignoutDialog.show();
		}

		else if (item.getItemId() == R.id.action_settings)
		{
			Intent intent = new Intent();
			intent.setClass(DiscoverMainActivity.this, SettingsActivity.class);
			startActivity(intent);
		}
		return false;
	}

	// start ip scan
	public void startDiscovery()
	{
		cancelTasks();
		scanRange = new ScanRange(netInfo.getLocalIp(), IpTranslator.cidrToMask(netInfo.getCidr()));
		ipScanTask = new IpScanTask(DiscoverMainActivity.this, scanRange);
		ipScanTask.execute();

		showProgress(true);

		deviceArraylist.clear();

	}

	public void stopDiscovery()
	{
		cancelTasks();
		showProgress(false);

		updateShowList();

		IGDDiscoveryTask igdDiscoveryTask = new IGDDiscoveryTask(
				ctxt);
		igdDiscoveryTask.execute();
	}

	// convert discovered devices into camera objects and add to list
	public void addHost(Host host)
	{
		if (!host.hardwareAddress.equals(NetInfo.EMPTY_MAC))
		{
			if (host.deviceType == Constants.TYPE_CAMERA)
			{
				camera = getDeviceFromScan(host, Constants.TYPE_CAMERA);

				if (cameraOperation.isExisting(camera.getIP(),
						netInfo.getSsid()))
				{
					camera.setLastSeen(getSystemTime());
					cameraOperation.updateScanCamera(camera, netInfo.getSsid());
				}
				else
				{
					cameraOperation.insertScanCamera(camera, netInfo.getSsid());
				}

				new PortScan(camera.getIP(), netInfo.getSsid(),
						ctxt);
				addToDeviceList(camera);
			}
			// not a camera, but record device info
			else
			{
				camera = getDeviceFromScan(host, Constants.TYPE_OTHERS);
				if ((host.deviceType == Constants.TYPE_ROUTER))
				{
					camera.setFlag(Constants.TYPE_ROUTER);
				}

				if (cameraOperation.isExisting(camera.getIP(),
						netInfo.getSsid()))
				{
					camera.setLastSeen(getSystemTime());
					cameraOperation.updateScanCamera(camera, netInfo.getSsid());
				}
				else
				{
					cameraOperation.insertScanCamera(camera, netInfo.getSsid());
				}
				addToDeviceList(camera);
			}

		}
	}

	// get current system time in format "dd/MM/yyyy HH:mm"
	public static String getSystemTime()
	{
		String time = null;
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		Date date = new Date(System.currentTimeMillis());
		time = formatter.format(date);
		return time;
	}

	private Camera getDeviceFromScan(Host host, int flag)
	{
		Camera scannedCamera = new Camera(host.ipAddress);
		scannedCamera.setMAC(host.hardwareAddress);
		scannedCamera.setVendor(host.vendor);
		scannedCamera.setFlag(flag);
		scannedCamera.setFirstSeen(getSystemTime());
		scannedCamera.setLastSeen(getSystemTime());
		return scannedCamera;
	}

	protected void cancelTasks()
	{
		if (ipScanTask != null)
		{
			ipScanTask.cancel(true);
			ipScanTask = null;
		}
	}

	public void makeToast(String toast_str)
	{
		Toast toast = Toast.makeText(getApplicationContext(), toast_str,
				Toast.LENGTH_SHORT);
		toast.show();
	}

	// show label of scanning process
	public void showProgress(Boolean isTrue)
	{
		if (isTrue)
		{
			scanning_text.setVisibility(View.VISIBLE);
			progressbar.setVisibility(View.VISIBLE);
			menuRefresh.setIcon(R.drawable.ic_cancel);
		}
		else
		{
			scanning_text.setVisibility(View.GONE);
			progressbar.setVisibility(View.GONE);
			menuRefresh.setIcon(R.drawable.ic_menu_refresh);
		}
	}

	// display Camera object in listView
	private void addToDeviceList(Camera camera)
	{
		String listIP = camera.getIP();
		String listMAC = camera.getMAC().toUpperCase();
		String listVendor = camera.getVendor();
		final HashMap<String, Object> deviceMap = new HashMap<String, Object>();

		deviceMap.put("device_name", listIP);
		deviceMap.put("device_mac", listMAC);
		if (camera.getModel() != "" && camera.getModel() != null)
		{
			deviceMap.put("device_vendor", camera.getModel());
		}
		else
		{
			deviceMap.put("device_vendor", listVendor);
		}

		deviceMap.put("device_timediff", getTimeDifference(camera.getLastSeen()
				+ ":00"));

		if (camera.getFlag() == Constants.TYPE_ROUTER)
		{
			deviceMap.put("device_img", R.drawable.tplink_trans);
		}
		else if (camera.getFlag() == Constants.TYPE_CAMERA)
		{
			ResourceHelper resourceHelper = new ResourceHelper(ctxt);
			deviceMap.put("device_img", resourceHelper.getCameraImageId(camera));
		}
		else
		{
			deviceMap.put("device_img", R.drawable.question_img_trans);
		}

		// display http/rtsp if not empty
		if (camera.hasHTTP())
		{
			deviceMap.put("device_http", "HTTP\u2713");
		}
		if (camera.hasRTSP())
		{
			deviceMap.put("device_rtsp", "RTSP\u2713");
		}

		deviceArraylist.add(deviceMap);
		sortByIp();
		deviceAdapter.notifyDataSetChanged();
	}

	private void startIpScan()
	{
		handler.postDelayed(new Runnable(){

			@Override
			public void run()
			{

				if (isWifiConnected || isEthernetConnected)
				{
					if (NetInfo.getExternalIP() != null)
					{
						startDiscovery();
					}
					else
					{
						makeToast(getResources().getString(R.string.checkInternetConnection));
					}
				}
			}

		}, 1000);

	}

	// show all devices in database
	private void displayAll()
	{
		deviceArraylist.clear();
		cameraList = cameraOperation.selectAllIP(netInfo.getSsid());
		Iterator<Camera> iterator = cameraList.iterator();
		while (iterator.hasNext())
		{
			Camera camera = iterator.next();
			addToDeviceList(camera);
		}
	}

	private void showLastScanResults(int type)
	{
		String lastSSID = sharedPrefs.getString(Constants.KEY_LAST_SSID, null);

		if (lastSSID != null)
		{
			if (type == Constants.TYPE_SHOW_ALL)
			{
				deviceArraylist.clear();
				ArrayList<Camera> cameraList = cameraOperation
						.selectAllIP(lastSSID);
				Iterator<Camera> iterator = cameraList.iterator();
				while (iterator.hasNext())
				{
					Camera camera = iterator.next();
					addToDeviceList(camera);
				}
			}
			else if (type == Constants.TYPE_SHOW_CAMERA)
			{

			}
		}
	}

	private void updateShowList()
	{
		isShowCameraOnly = sharedPrefs.getBoolean(
				Constants.KEY_SHOW_CAMERA_ONLY, false);
		if (isShowCameraOnly)
		{
			displayCameraOnly();
		}
		else
		{
			displayAll();
		}
	}

	private void displayCameraOnly()
	{
		cameraList = cameraOperation.selectCameraOnly(netInfo.getSsid());
		Iterator<Camera> iterator = cameraList.iterator();
		if (!iterator.hasNext())
		{

			AlertDialog alertDialog = new AlertDialog.Builder(
					DiscoverMainActivity.this)

					.setMessage(R.string.alertNoCamera)
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener(){
								@Override
								public void onClick(DialogInterface dialog,
										int which)
								{
									displayAll();
								}
							}).create();
			alertDialog.show();
		}
		else
		{
			deviceArraylist.clear();
			while (iterator.hasNext())
			{
				Camera camera = iterator.next();
				addToDeviceList(camera);
			}
		}
	}

	// calculate time difference between two time points
	public String getTimeDifference(String time)
	{

		String diff = "";
		long day = 0;
		long hour = 0;
		long min = 0;
		long sec = 0;
		Date now = new Date(System.currentTimeMillis());
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date date;
		try
		{
			date = formatter.parse(time);
			long diff_long = now.getTime() - date.getTime();

			day = diff_long / (24 * 60 * 60 * 1000);
			hour = (diff_long / (60 * 60 * 1000) - day * 24);
			min = ((diff_long / (60 * 1000)) - day * 24 * 60 - hour * 60);
			sec = (diff_long / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
			diff = "" + day + "day" + hour + "hour" + min + "min" + sec + "sec";
		}
		catch (ParseException e)
		{
			Log.e("Error", "Time difference error");
		}
		if (day != 0)
		{
			if (day == 1)
			{
				diff = day + " " + "day ago";
			}
			else
			{
				diff = day + " " + "days ago";
			}
		}
		else if (day == 0)
		{
			if (hour != 0)
			{
				if (hour == 1)
				{
					diff = hour + " " + "hour ago";
				}
				else
				{
					diff = hour + " " + "hours ago";
				}
			}
			else if (hour == 0)
			{
				if (min == 1)
				{
					diff = min + " " + "minute ago";
				}
				else if (min == 0)
				{
					diff = "now";
				}
				else
				{
					diff = min + " " + "minutes ago";
				}
			}
		}
		return diff;
	}

	private void sortByIp()
	{

		Collections.sort(deviceArraylist,
				new Comparator<HashMap<String, Object>>(){
					@Override
					public int compare(HashMap<String, Object> arg0,
							HashMap<String, Object> arg1)
					{
						try
						{
							String ip1 = (String) arg0.get("device_name");
							int digit1 = Integer.parseInt(ip1.substring(
									ip1.lastIndexOf(".") + 1, ip1.length()));
							String ip2 = (String) arg1.get("device_name");
							int digit2 = Integer.parseInt(ip2.substring(
									ip2.lastIndexOf(".") + 1, ip2.length()));
							return (digit1 - digit2);
						}
						catch (NumberFormatException e)
						{
							e.printStackTrace();
							return 1;
						}
					}
				});

	}

	private void showWifiNotConnectDialog()
	{
		AlertDialog.Builder connectDialogBuilder = new AlertDialog.Builder(this);
		connectDialogBuilder.setMessage(R.string.dialogMsgMustConnect);

		connectDialogBuilder.setPositiveButton(R.string.wifiSettings,
				new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
					}
				});
		connectDialogBuilder.setNegativeButton(R.string.notNow,
				new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						return;
					}
				});
		connectDialogBuilder.setTitle(R.string.notConnected);
		connectDialogBuilder.setCancelable(false);
		connectDialogBuilder.show();
	}

	private void showConfirmScanDialog()
	{
		AlertDialog alertMsg = new AlertDialog.Builder(
				DiscoverMainActivity.this)

				.setMessage(R.string.confirmStopScan)
				.setPositiveButton(R.string.yes,
						new DialogInterface.OnClickListener(){
							@Override
							public void onClick(DialogInterface dialog,
									int which)
							{
								stopDiscovery();
							}
						})
				.setNegativeButton(R.string.no,
						new DialogInterface.OnClickListener(){
							@Override
							public void onClick(DialogInterface dialog,
									int which)
							{
								return;
							}
						}).create();
		alertMsg.show();
	}
}

package io.evercam.connect;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
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

import com.bugsense.trace.BugSenseHandler;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import io.evercam.API;
import io.evercam.connect.db.Camera;
import io.evercam.connect.db.CameraOperation;
import io.evercam.connect.discover.bonjour.JmdnsDiscover;
import io.evercam.connect.discover.ipscan.Host;
import io.evercam.connect.discover.ipscan.IpScanTask;
import io.evercam.connect.discover.ipscan.PortScanTask;
import io.evercam.connect.discover.upnp.IGDDiscoveryTask;
import io.evercam.connect.discover.upnp.UpnpDiscoveryTask;
import io.evercam.connect.helper.Constants;
import io.evercam.connect.helper.CustomedDialog;
import io.evercam.connect.helper.PropertyReader;
import io.evercam.connect.helper.SharedPrefsManager;
import io.evercam.connect.helper.TimeHelper;
import io.evercam.connect.net.CheckInternetTaskMain;
import io.evercam.connect.net.NetInfo;
import io.evercam.network.cambase.CambaseAPI;
import io.evercam.network.cambase.CambaseException;
import io.evercam.network.discovery.IpTranslator;
import io.evercam.network.discovery.ScanRange;

/**
 * MainActivity
 * <p/>
 * App Entry page, start scan and show discovered devices.
 */

public class DiscoverMainActivity extends Activity
{
    public final String TAG = "DiscoverMainActivity";
    public final String ADAPTER_KEY_IMAGE = "device_img";
    public final String ADAPTER_KEY_NAME = "device_name";
    public final String ADAPTER_KEY_MAC = "device_mac";
    public final String ADAPTER_KEY_VENDOR = "device_vendor";
    public final String ADAPTER_KEY_HTTP = "device_http";
    public final String ADAPTER_KEY_RTSP = "device_rtsp";
    public final String ADAPTER_KEY_TIMEDIFF = "device_timediff";
    public final String ADAPTER_KEY_LOGO = "evercamlogo";
    public final String ADAPTER_KEY_ACTIVE = "device_active";

    private Handler handler = new Handler();
    private SimpleAdapter deviceAdapter;
    private ArrayList<HashMap<String, Object>> deviceArraylist;
    public NetInfo netInfo;
    private Context ctxt;
    private TextView scanning_text;
    private ProgressBar progressbar;
    private IpScanTask ipScanTask = null;
    private ArrayList<Camera> cameraList;
    public ArrayList<io.evercam.Camera> evercamCameraList;
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
    public static HashMap<String, Bitmap> thumbnailMap = new HashMap<String, Bitmap>();

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
        if(propertyReader.isPropertyExist(PropertyReader.KEY_BUG_SENSE))
        {
            String bugSenseCode = propertyReader.getPropertyStr(PropertyReader.KEY_BUG_SENSE);
            BugSenseHandler.initAndStartSession(DiscoverMainActivity.this, bugSenseCode);
        }
        setContentView(R.layout.activity_evercam_discover);

        EvercamDiscover.sendScreenAnalytics(this, getString(R.string.screen_discovery));

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        netInfo = new NetInfo(ctxt);
        setEvercamUserApiKey();

        // discovered device list
        final ListView deviceList = (ListView) findViewById(R.id.device_list);
        deviceList.addHeaderView(LayoutInflater.from(this).inflate(R.layout.header_layout, null));

        scanning_text = (TextView) findViewById(R.id.scanning_text1);
        progressbar = (ProgressBar) findViewById(R.id.processBar1);
        deviceArraylist = new ArrayList<HashMap<String, Object>>();
        deviceAdapter = new DeviceListAdapter(this, deviceArraylist, R.layout
                .ditail_relative_layout, new String[]{ADAPTER_KEY_IMAGE, ADAPTER_KEY_NAME,
                ADAPTER_KEY_MAC, ADAPTER_KEY_VENDOR, ADAPTER_KEY_HTTP, ADAPTER_KEY_RTSP,
                ADAPTER_KEY_TIMEDIFF, ADAPTER_KEY_LOGO, ADAPTER_KEY_ACTIVE}, new int[]{R.id
                .device_img, R.id.device_name, R.id.device_mac, R.id.device_vendor, R.id
                .device_http, R.id.device_rtsp, R.id.time_diff, R.id.evercamglobe_img, R.id
                .device_active});
        deviceList.setAdapter(deviceAdapter);

        LinearLayout sampleLayout = (LinearLayout) findViewById(R.id.sample_layout);
        sampleLayout.setFocusable(true);
        sampleLayout.setClickable(true);
        sampleLayout.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                EvercamDiscover.sendEventAnalytics(DiscoverMainActivity.this, R.string
                        .category_main_click, R.string.action_demo, R.string.label_demo);
                if(!netInfo.hasActiveNetwork())
                {
                    makeToast(ctxt.getResources().getString(R.string.pleaseConnectNetwork));
                }
                else
                {
                    Intent intent = new Intent(DiscoverMainActivity.this, CameraDetailActivity
                            .class);
                    intent.putExtra(Constants.BUNDLE_KEY_IP, propertyReader.getPropertyStr
                            (PropertyReader.KEY_SAMPLE_IP));
                    intent.putExtra(Constants.BUNDLE_KEY_SSID, Constants.SAMPLE);
                    startActivity(intent);
                }
            }
        });

        deviceList.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3)
            {
                @SuppressWarnings("unchecked") HashMap<String, Object> map = (HashMap<String,
                        Object>) deviceList.getItemAtPosition(position);
                final String deviceIp = (String) map.get(ADAPTER_KEY_NAME);
                if(cameraOperation.isExisting(deviceIp, netInfo.getSsid()))
                {
                    if((cameraOperation.getCamera(deviceIp, netInfo.getSsid()).getFlag() ==
                            Constants.TYPE_ROUTER))
                    {
                        Intent intent = new Intent();
                        intent.setClass(DiscoverMainActivity.this, RouterActivity.class);
                        startActivity(intent);
                    }
                    else
                    {
                        Intent intent = new Intent();
                        intent.setClass(DiscoverMainActivity.this, CameraDetailActivity.class);
                        intent.putExtra(Constants.BUNDLE_KEY_IP, deviceIp);
                        intent.putExtra(Constants.BUNDLE_KEY_SSID, netInfo.getSsid());
                        startActivity(intent);
                    }
                }
                else
                {
                    makeToast(getString(R.string.msg_device_not_exist));
                }
            }
        });

        scanning_text.setClickable(true);
        scanning_text.setFocusable(true);
        scanning_text.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AlertDialog alertMsg = new AlertDialog.Builder(DiscoverMainActivity.this)

                        .setMessage(R.string.confirmStopScan).setPositiveButton(R.string.yes, new
                                DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                stopDiscovery();
                            }
                        }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                return;
                            }
                        }).create();
                alertMsg.show();
            }
        });
        handler.postDelayed(new Runnable()
        {
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

        if(propertyReader.isPropertyExist(PropertyReader.KEY_BUG_SENSE))
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

        if(netInfo.isWifiConnected(ctxt))
        {
            isShowCameraOnly = sharedPrefs.getBoolean(Constants.KEY_SHOW_CAMERA_ONLY, false);
            if(ipScanTask == null)
            {
                updateShowList();
            }
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if(propertyReader.isPropertyExist(PropertyReader.KEY_BUG_SENSE))
        {
            BugSenseHandler.closeSession(this);
        }
    }

    @Override
    public void onBackPressed()
    {
        finish();
    }

    private void setUp()
    {

        TextView ssid_text = (TextView) findViewById(R.id.ssid_text1);

        // get wifi info
        ctxt = getApplicationContext();
        netInfo = new NetInfo(ctxt);

        if(!netInfo.isWifiConnected(ctxt))
        {
            if(netInfo.isEthernetConnected())
            {
                ssid_text.setText("Connected to : Ethernet");
                isEthernetConnected = true;
            }
            else
            {
                ssid_text.setText(R.string.no_wifi);

                CustomedDialog.getNoInternetDialog(this).show();
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

            cameraOperation.resetActive(netInfo.getSsid());

            // Bonjour
            JmdnsDiscover jmdnsDiscover = new JmdnsDiscover(netInfo, ctxt);
            jmdnsDiscover.startJmdnsDiscovery();

            // UPnP
            upnpDiscoveryTask = new UpnpDiscoveryTask(ctxt);
            upnpDiscoveryTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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

        if(ipScanTask != null)
        {
            menuRefresh.setIcon(R.drawable.ic_cancel);

        }
        else
        {
            menuRefresh.setIcon(R.drawable.ic_menu_refresh);
        }
        menuSignIn.setVisible(false);
        menuSignOut.setVisible(false);

        if(SharedPrefsManager.isSigned(sharedPrefs))
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
        if(item.getItemId() == R.id.action_refresh)
        {
            if(ipScanTask != null)
            {
                showConfirmCancelScanDialog();
            }
            else
            {
                EvercamDiscover.sendEventAnalytics(this, R.string.category_main_click, R.string
                        .action_refresh, R.string.label_refresh);
                makeToast(getString(R.string.refreshing));

                deviceArraylist.clear();

                handler.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if(!(ipScanTask == null))
                        {
                            cancelTasks();
                        }
                        setUp();
                        startIpScan();
                    }
                }, 1000);
            }
        }
        else if(item.getItemId() == R.id.action_signIn)
        {
            EvercamDiscover.sendEventAnalytics(this, R.string.category_main_click, R.string
                    .action_sign_in_out, R.string.label_menu_login);
            Intent intentWelcome = new Intent(DiscoverMainActivity.this, SlideActivity.class);
            startActivity(intentWelcome);
        }
        else if(item.getItemId() == R.id.action_signOut)
        {
            AlertDialog confirmSignoutDialog = new AlertDialog.Builder(DiscoverMainActivity.this)

                    .setMessage(R.string.confirmSignOutMsg).setPositiveButton(R.string.yes, new
                            DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            EvercamDiscover.sendEventAnalytics(DiscoverMainActivity.this, R
                                    .string.category_main_click, R.string.action_sign_in_out, R
                                    .string.label_confirm_logout);
                            SharedPrefsManager.clearAllUserInfo(sharedPrefs);
                            menuSignIn.setVisible(true);
                            menuSignOut.setVisible(false);
                            Intent intentWelcome = new Intent(DiscoverMainActivity.this,
                                    SlideActivity.class);
                            startActivity(intentWelcome);
                        }
                    }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            EvercamDiscover.sendEventAnalytics(DiscoverMainActivity.this, R
                                    .string.category_main_click, R.string.action_sign_in_out, R
                                    .string.label_cancel_logout);
                            return;
                        }
                    }).create();
            confirmSignoutDialog.show();
        }
        else if(item.getItemId() == R.id.action_settings)
        {
            EvercamDiscover.sendEventAnalytics(DiscoverMainActivity.this, R.string
                    .category_main_click, R.string.action_settings, R.string.label_settings);
            Intent intent = new Intent();
            intent.setClass(DiscoverMainActivity.this, SettingsActivity.class);
            startActivity(intent);
        }
        return false;
    }

    public void startDiscovery()
    {
        if(SharedPrefsManager.isSignedWithEvercam(sharedPrefs))
        {
            GetAllCameraTask getAllCameraTask = new GetAllCameraTask(DiscoverMainActivity.this);
            getAllCameraTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        cancelTasks();
        cameraOperation.clearEvercamStatus();// clear 'added to Evercam' status
        // before scanning
        try
        {
            scanRange = new ScanRange(netInfo.getLocalIp(), IpTranslator.cidrToMask(netInfo
                    .getCidr()));
        }
        catch(Exception e)
        {
            Log.e(TAG, e.toString());
        }
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

        IGDDiscoveryTask igdDiscoveryTask = new IGDDiscoveryTask(ctxt);
        igdDiscoveryTask.execute();
    }

    // convert discovered devices into camera objects and add to list
    public void addHost(Host host)
    {
        if(!host.hardwareAddress.equals(NetInfo.EMPTY_MAC))
        {
            if(host.deviceType == Constants.TYPE_CAMERA)
            {
                camera = getDeviceFromScan(host, Constants.TYPE_CAMERA);

                if(cameraOperation.isExisting(camera.getIP(), netInfo.getSsid()))
                {
                    camera.setLastSeen(getSystemTime());
                    cameraOperation.updateScanCamera(camera, netInfo.getSsid());
                }
                else
                {
                    cameraOperation.insertCamera(camera, netInfo.getSsid());
                }

                new PortScanTask(camera.getIP(), netInfo.getSsid(), ctxt).executeOnExecutor
                        (AsyncTask.THREAD_POOL_EXECUTOR);
                ;

                addToDeviceList(camera);
            }
            // not a camera, but record device info
            else
            {
                camera = getDeviceFromScan(host, Constants.TYPE_OTHERS);
                if((host.deviceType == Constants.TYPE_ROUTER))
                {
                    camera.setFlag(Constants.TYPE_ROUTER);
                }

                if(cameraOperation.isExisting(camera.getIP(), netInfo.getSsid()))
                {
                    camera.setLastSeen(getSystemTime());
                    cameraOperation.updateScanCamera(camera, netInfo.getSsid());
                }
                else
                {
                    cameraOperation.insertCamera(camera, netInfo.getSsid());
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
        scannedCamera.setActive(1);
        return scannedCamera;
    }

    protected void cancelTasks()
    {
        if(ipScanTask != null)
        {
            ipScanTask.cancel(true);
            ipScanTask = null;
        }
    }

    public void makeToast(String toast_str)
    {
        Toast toast = Toast.makeText(getApplicationContext(), toast_str, Toast.LENGTH_SHORT);
        toast.show();
    }

    // show label of scanning process
    public void showProgress(Boolean isTrue)
    {
        if(isTrue)
        {
            scanning_text.setVisibility(View.VISIBLE);
            progressbar.setVisibility(View.VISIBLE);
            if(menuRefresh != null)
            {
                menuRefresh.setIcon(R.drawable.ic_cancel);
            }
        }
        else
        {
            scanning_text.setVisibility(View.GONE);
            progressbar.setVisibility(View.GONE);
            if(menuRefresh != null)
            {
                menuRefresh.setIcon(R.drawable.ic_menu_refresh);
            }
        }
    }

    // display Camera object in listView
    private void addToDeviceList(Camera camera)
    {
        checkIsEvercam(camera);
        camera = cameraOperation.getCamera(camera.getIP(), netInfo.getSsid());
        String listIP = camera.getIP();
        String listVendor = camera.getVendor();
        final HashMap<String, Object> deviceMap = new HashMap<String, Object>();

        deviceMap.put(ADAPTER_KEY_NAME, listIP);
        if(camera.hasMac())
        {
            deviceMap.put(ADAPTER_KEY_MAC, camera.getMAC().toUpperCase());
        }
        else
        {
            deviceMap.put(ADAPTER_KEY_MAC, NetInfo.EMPTY_MAC);
        }
        if(camera.hasModel())
        {
            if(camera.modelContainsVendorName())
            {
                deviceMap.put(ADAPTER_KEY_VENDOR, camera.getModel());
            }
            else
            {
                // If model does not contain vendor name, show as vendor name +
                // model name.
                deviceMap.put(ADAPTER_KEY_VENDOR, camera.getVendor() + " " + camera.getModel());
            }
        }
        else if(camera.hasVendor())
        {
            deviceMap.put(ADAPTER_KEY_VENDOR, listVendor);
        }
        else
        {
            deviceMap.put(ADAPTER_KEY_VENDOR, getString(R.string.unknown_vendor));
        }

        deviceMap.put(ADAPTER_KEY_TIMEDIFF, TimeHelper.getTimeDifference(camera.getLastSeen() +
                ":00"));

        // display http/rtsp if not empty
        if(camera.hasHTTP())
        {
            deviceMap.put(ADAPTER_KEY_HTTP, "HTTP\u2713");
        }
        if(camera.hasRTSP())
        {
            deviceMap.put(ADAPTER_KEY_RTSP, "RTSP\u2713");
        }
        if(camera.isEvercam())
        {
            deviceMap.put(ADAPTER_KEY_LOGO, R.drawable.icon_50x50);
        }

        // Device is active or not
        if(camera.isActive())
        {
            deviceMap.put(ADAPTER_KEY_ACTIVE, "active");
        }

        if(camera.getFlag() == Constants.TYPE_CAMERA)
        {
            EvercamTask evercamTask = new EvercamTask(camera, ctxt);
            evercamTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            if(!thumbnailMap.containsKey(camera.getIP()))
            {
                new ThumbnailTask(camera).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }

        deviceArraylist.add(deviceMap);
        sortByIp();
        deviceAdapter.notifyDataSetChanged();
    }

    private class ThumbnailTask extends AsyncTask<Void, Void, Bitmap>
    {
        private Camera camera;

        public ThumbnailTask(Camera camera)
        {
            this.camera = camera;
        }

        @Override
        protected Bitmap doInBackground(Void... params)
        {
            try
            {
                String thumbnailUrl = CambaseAPI.getThumbnailUrlFor(camera.getVendor()
                        .toLowerCase(Locale.UK), camera.getModel());
                Bitmap bitmap = null;

                if(!thumbnailUrl.isEmpty())
                {
                    try
                    {
                        InputStream stream = Unirest.get(thumbnailUrl).asBinary().getRawBody();
                        bitmap = BitmapFactory.decodeStream(stream);
                    }
                    catch(UnirestException e)
                    {
                        Log.e(TAG, e.getStackTrace()[0].toString());
                    }
                }

                return bitmap;
            }
            catch(CambaseException e)
            {
                Log.e(TAG, e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap)
        {
            if(bitmap != null)
            {
                thumbnailMap.put(camera.getIP(), bitmap);
                deviceAdapter.notifyDataSetChanged();
            }
        }
    }

    private void startIpScan()
    {
        handler.postDelayed(new Runnable()
        {

            @Override
            public void run()
            {
                if(isWifiConnected || isEthernetConnected)
                {
                    new CheckInternetTaskMain(DiscoverMainActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
        while(iterator.hasNext())
        {
            Camera camera = iterator.next();
            addToDeviceList(camera);
        }
    }

    private void showLastScanResults(int type)
    {
        String lastSSID = sharedPrefs.getString(Constants.KEY_LAST_SSID, null);

        if(lastSSID != null)
        {
            if(type == Constants.TYPE_SHOW_ALL)
            {
                deviceArraylist.clear();
                ArrayList<Camera> cameraList = cameraOperation.selectAllIP(lastSSID);
                Iterator<Camera> iterator = cameraList.iterator();
                while(iterator.hasNext())
                {
                    Camera camera = iterator.next();
                    addToDeviceList(camera);
                }
            }
            else if(type == Constants.TYPE_SHOW_CAMERA)
            {

            }
        }
    }

    private void updateShowList()
    {
        isShowCameraOnly = sharedPrefs.getBoolean(Constants.KEY_SHOW_CAMERA_ONLY, false);
        if(isShowCameraOnly)
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
        if(!iterator.hasNext())
        {

            final AlertDialog alertDialog = new AlertDialog.Builder(DiscoverMainActivity.this)

                    .setMessage(R.string.alertNoCamera).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            displayAll();
                        }
                    }).create();
            if(!DiscoverMainActivity.this.isFinishing())
            {
                alertDialog.show();
            }
        }
        else
        {
            deviceArraylist.clear();
            while(iterator.hasNext())
            {
                Camera camera = iterator.next();
                addToDeviceList(camera);
            }
        }
    }

    private void sortByIp()
    {
        Collections.sort(deviceArraylist, new Comparator<HashMap<String, Object>>()
        {
            @Override
            public int compare(HashMap<String, Object> arg0, HashMap<String, Object> arg1)
            {
                try
                {
                    String ip1 = (String) arg0.get(ADAPTER_KEY_NAME);
                    int digit1 = Integer.parseInt(ip1.substring(ip1.lastIndexOf(".") + 1, ip1.length()));
                    String active1 = (String) arg0.get(ADAPTER_KEY_ACTIVE);

                    boolean isActive1 = active1 != null && !active1.isEmpty();
                    String ip2 = (String) arg1.get(ADAPTER_KEY_NAME);
                    int digit2 = Integer.parseInt(ip2.substring(ip2.lastIndexOf(".") + 1, ip2.length()));
                    String active2 = (String) arg1.get(ADAPTER_KEY_ACTIVE);

                    boolean isActive2 = active2 != null && !active2.isEmpty();

                    // If both device are active, order by ip.
                    if((isActive1 && isActive2) || (!isActive1 && !isActive2))
                    {
                        return (digit1 - digit2);
                    }
                    // Else put active device on top of the list.
                    else if(isActive1 && !isActive2)
                    {
                        return -1;
                    }
                    else if(!isActive1 && isActive2)
                    {
                        return 1;
                    }
                    else
                    {
                        return 0;
                    }
                }
                catch(NumberFormatException e)
                {
                    e.printStackTrace();
                    return 1;
                }
            }
        });

    }

    private void showConfirmCancelScanDialog()
    {
        AlertDialog alertMsg = new AlertDialog.Builder(DiscoverMainActivity.this)

                .setMessage(R.string.confirmStopScan).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        EvercamDiscover.sendEventAnalytics(DiscoverMainActivity.this, R.string.category_main_click, R.string.action_refresh, R.string.label_cancel_refresh);
                        stopDiscovery();
                    }
                }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        return;
                    }
                }).create();
        alertMsg.show();
    }

    private void checkIsEvercam(Camera camera)
    {
        boolean isEvercam = false;
        if(evercamCameraList != null)
        {
            for(io.evercam.Camera evercamCamera : evercamCameraList)
            {
                if(!camera.getMAC().isEmpty())
                {
                    if(camera.getMAC().equalsIgnoreCase(evercamCamera.getMacAddress()))
                    {
                        cameraOperation.updateAttributeInt(camera.getIP(), camera.getSsid(), "evercam", 1);
                        isEvercam = true;
                    }
                }
            }
            if(!isEvercam)
            {
                cameraOperation.updateAttributeInt(camera.getIP(), camera.getSsid(), "evercam", 0);
            }
        }
    }

    private void setEvercamUserApiKey()
    {
        String userApiKey = SharedPrefsManager.getUserApiKey(sharedPrefs);
        String userApiId = SharedPrefsManager.getUserApiId(sharedPrefs);
        API.setUserKeyPair(userApiKey, userApiId);
    }
}

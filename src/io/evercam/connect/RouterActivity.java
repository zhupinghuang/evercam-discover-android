package io.evercam.connect;

import io.evercam.connect.db.Camera;
import io.evercam.connect.db.CameraOperation;
import io.evercam.connect.net.NetInfo;

import java.util.Locale;

import com.bugsense.trace.BugSenseHandler;

import io.evercam.connect.R;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class RouterActivity extends Activity
{

	private NetInfo netInfo;
	private Context ctxt;
	private CameraOperation cameraOperation;
	private Handler handler = new Handler();
	private TextView external_ip;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_router);

		TextView router_ip = (TextView) findViewById(R.id.routerip_detail);
		TextView router_model = (TextView) findViewById(R.id.routermodel_detail);
		external_ip = (TextView) findViewById(R.id.externalip_detail);
		TextView router_mac = (TextView) findViewById(R.id.routermac_detail);
		TextView router_netmask = (TextView) findViewById(R.id.netmask_detail);
		TextView router_upnp = (TextView) findViewById(R.id.routerUpnp_detail);

		TextView device_title = (TextView) findViewById(R.id.device_title);
		TextView device_ip = (TextView) findViewById(R.id.deviceIP_detail);
		TextView device_mac = (TextView) findViewById(R.id.deviceMAC_detail);

		LinearLayout routerModelLayout = (LinearLayout) findViewById(R.id.routermodel_layout);

		ctxt = getApplicationContext();
		netInfo = new NetInfo(ctxt);

		cameraOperation = new CameraOperation(ctxt);

		if (!netInfo.getGatewayIp().equals(NetInfo.EMPTY_IP))
		{
			router_ip.setText(netInfo.getGatewayIp());
			router_mac.setText(NetInfo.getHardwareAddress(netInfo.getGatewayIp()).toUpperCase(
					Locale.UK));
		}

		try
		{
			displayExternalIP();

			router_netmask.setText(netInfo.getNetmaskIp());
			Camera camera = cameraOperation.getCamera(netInfo.getGatewayIp(), netInfo.getSsid());

			// show model if exists
			if (camera.getModel() != null && camera.getModel() != "")
			{
				routerModelLayout.setVisibility(View.VISIBLE);
				router_model.setText(camera.getModel());
			}

			// upnp status
			if (camera.getUpnp() == 1)
			{
				router_upnp.setText(R.string.enabled);
			}
			else
			{
				router_upnp.setText(R.string.disabled);
			}

			device_title.setText(this.getResources().getString(R.string.networkInterface)
					+ netInfo.getInterfaceName());

			device_ip.setText(netInfo.getLocalIp());
			device_mac.setText(netInfo.getMacAddress().toUpperCase(Locale.UK));
		}
		catch (Exception e)
		{
			BugSenseHandler.sendException(e);
		}
		Button b = (Button) findViewById(R.id.button_routerweb);
		b.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0)
			{
				if (!netInfo.getGatewayIp().equals(NetInfo.EMPTY_IP))
				{
					String url = "http://" + netInfo.getGatewayIp() + "/";
					Intent intent = new Intent();
					intent.setAction("android.intent.action.VIEW");
					Uri content_url = Uri.parse(url);
					intent.setData(content_url);
					startActivity(intent);
				}
				else
				{
					Toast.makeText(ctxt, "Router is not avaliable.", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	private void displayExternalIP()
	{
		new AsyncTask<Void, Void, String>()
		{
			@Override
			protected String doInBackground(Void... params)
			{

				return NetInfo.getExternalIP();
			}
			@Override
			protected void onPostExecute(String externalIp)
			{
				if (externalIp != null)
				{
					external_ip.setText(externalIp);
				}
			}
		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);;
	}
}

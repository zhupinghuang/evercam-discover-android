package io.evercam.connect;

import io.evercam.connect.R;
import io.evercam.connect.db.Camera;
import io.evercam.connect.db.CameraOperation;
import io.evercam.connect.db.ResourceHelper;
import io.evercam.connect.db.SimpleDBConnect;
import io.evercam.connect.net.NetInfo;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * CameraDetailActivity
 *
 * Camera detail page, show camera thumnails, model , ports info, live view etc. 
 */

public class CameraDetailActivity extends Activity
{

	private String ipstring;
	private String ssid;
	private String rtspURL;
	private CameraOperation cameraOperation;
	private Camera camera;
	private Button http_button;
	private Button rtsp_button;
	private Button editBtn;
	private Button portForwardBtn;
	private Button setDeviceBtn;
	private Button setCameraBtn;
	private Context ctxt;

	private NetInfo netInfo;
	private ImageView snapshot;
	private Handler handler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera_detail);

		ctxt = getApplicationContext();
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayHomeAsUpEnabled(true);

		netInfo = new NetInfo(ctxt);
		// get camera infos
		Bundle extras = getIntent().getExtras();
		ipstring = extras.getString("IP");
		ssid = extras.getString("SSID");
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);
		cameraOperation = new CameraOperation(ctxt);
		setUpPage();

		http_button.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0)
			{
				String commonUrl;
				if (camera.getSsid().equals("sample"))
				{
					commonUrl = ResourceHelper.getExternalHttpURL(camera);
				}
				else
				{
					commonUrl = ResourceHelper.getInternalHttpURL(camera);
				}

				Intent intent = new Intent();
				intent.setAction("android.intent.action.VIEW");
				Uri content_url = Uri.parse(commonUrl);
				intent.setData(content_url);
				startActivity(intent);
			}

		});

		rtsp_button.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0)
			{
				if (camera.getVendor().equals(Constants.VENDOR_HIKVISION))
				{
					rtspURL = "rtsp://" + camera.getUsername() + ":"
							+ camera.getPassword() + "@" + ipstring + ":"
							+ camera.getRtsp() + "/h264/ch1/main/av_stream";
				}
				else if (camera.getVendor().equals(Constants.VENDOR_AXIS))
				{
					rtspURL = "rtsp://" + camera.getUsername() + ":"
							+ camera.getPassword() + "@" + ipstring + ":"
							+ camera.getRtsp() + "/axis-media/media.amp";
				}
				else if (camera.getVendor().equals(Constants.VENDOR_UBIQUITI))
				{
					rtspURL = "rtsp://" + ipstring + ":" + camera.getRtsp()
							+ "/live/ch00_0";
				}
				else if (camera.getVendor().equals(Constants.VENDOR_YCAM))
				{
					rtspURL = "rtsp://" + camera.getUsername() + ":"
							+ camera.getPassword() + "@" + ipstring + ":"
							+ camera.getRtsp() + "/live_mpeg4.sdp";
				}
				else
				{
					rtspURL = null;
				}

				if (rtspURL != null)
				{
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri
							.parse(rtspURL));
					startActivity(intent);
					if (ssid.equals("sample"))
					{
						for (int i = 11; i >= 1; i--)
						{
							Toast toast = Toast.makeText(
									ctxt,
									"Loading video stream..." + i,
									Toast.LENGTH_SHORT);
							toast.setGravity(Gravity.CENTER, 0, 0);
							toast.show();
						}

					}
					else
					{
						for (int i = 4; i >= 1; i--)
						{
							Toast toast = Toast.makeText(
									ctxt,
									"Loading video stream..." + i,
									Toast.LENGTH_SHORT);
							toast.setGravity(Gravity.CENTER, 0, 0);
							toast.show();
						}
					}
				}

				else if (rtspURL == null)
				{
					Toast toast = Toast
							.makeText(
									ctxt,
									"Sorry, RTSP Stream is not availiable for this device.",
									Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}
			}
		});

		portForwardBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0)
			{

				AlertDialog.Builder alertDialog = new AlertDialog.Builder(
						CameraDetailActivity.this);
				alertDialog.setTitle(R.string.title_activity_main_tab);
				alertDialog
						.setMessage(R.string.forwardGuideMsg);

				if (camera.isDemoCamera())
				{

					alertDialog.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener(){

								@Override
								public void onClick(DialogInterface dialog,
										int which)
								{

								}
							}).show();
				}
				else
				{

					alertDialog.setPositiveButton(R.string.next,
							new DialogInterface.OnClickListener(){

								@Override
								public void onClick(DialogInterface dialog,
										int which)
								{

									Intent intentForward = new Intent();
									intentForward.setClass(
											CameraDetailActivity.this,
											MainTabActivity.class);
									intentForward.putExtra("IP", ipstring);
									intentForward.putExtra("SSID",
											netInfo.getSsid());
									startActivity(intentForward);
								}
							}).show();

				}

			}

		});

		setCameraBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v)
			{
				if (camera.isDemoCamera())
				{
					showDemoSetAsCameraDialog();
				}
				else
				{
					showSetAsCameraDialog();
				}
			}

		});

		setDeviceBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v)
			{
				if (camera.isDemoCamera())
				{
					showDemoSetAsCameraDialog();
				}
				else
				{
					showSetAsDeviceDialog();
				}

			}

		});

		snapshot.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v)
			{

				Toast toast = Toast.makeText(ctxt,
						"Refreshing snapshot...", Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
				handler.postDelayed(new Runnable(){

					@Override
					public void run()
					{
						if (camera.getSsid().equals("sample"))
						{
							snapshot.setImageBitmap(getSnapshot(
									"http://89.101.225.158:8101/Streaming/channels/1/picture",
									camera.getUsername(), camera.getPassword()));
						}
						else
						{
							snapshot.setImageBitmap(getSnapshot(
									getSnapshotURL(), camera.getUsername(),
									camera.getPassword()));
						}
					}
				}, 1000);
			}
		});

		editBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v)
			{

				showEditDialog();
			}

		});
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		setUpPage();

	}

	@Override
	public Intent getParentActivityIntent()
	{
		this.finish();
		return super.getParentActivityIntent();

	}

	public void setUpPage()
	{
		camera = cameraOperation.getCamera(ipstring, ssid);
		ctxt = getApplicationContext();

		TextView ip = (TextView) findViewById(R.id.ipvalue_detail);
		TextView mac = (TextView) findViewById(R.id.macvalue_detail);
		TextView vendor = (TextView) findViewById(R.id.vendorvalue_detail);
		TextView iscamera = (TextView) findViewById(R.id.iscameravalue_detail);
		TextView isbonjour = (TextView) findViewById(R.id.isbonjourvalue_detail);
		TextView isupnp = (TextView) findViewById(R.id.isupnpvalue_detail);
		TextView isonvif = (TextView) findViewById(R.id.isonvifvalue_detail);
		TextView model = (TextView) findViewById(R.id.modelvalue_detail);
		TextView http = (TextView) findViewById(R.id.httpvalue_detail);
		TextView exthttp = (TextView) findViewById(R.id.exthttpvalue_detail);
		TextView rtsp = (TextView) findViewById(R.id.rtspvalue_detail);
		TextView https = (TextView) findViewById(R.id.httpsvalue_detail);
		TextView ftp = (TextView) findViewById(R.id.ftpvalue_detail);
		TextView ssh = (TextView) findViewById(R.id.sshvalue_detail);

		TextView extrtsp = (TextView) findViewById(R.id.extrtspvalue_detail);
		TextView firstseen = (TextView) findViewById(R.id.firstseenvalue_detail);
		TextView lastseen = (TextView) findViewById(R.id.lastseenvalue_detail);
		LinearLayout model_layout = (LinearLayout) findViewById(R.id.model_layout);
		LinearLayout bonjour_layout = (LinearLayout) findViewById(R.id.bonjour_layout);
		LinearLayout upnp_layout = (LinearLayout) findViewById(R.id.upnp_layout);
		LinearLayout onvif_layout = (LinearLayout) findViewById(R.id.onvif_layout);
		LinearLayout http_layout = (LinearLayout) findViewById(R.id.http_layout);
		LinearLayout rtsp_layout = (LinearLayout) findViewById(R.id.rtsp_layout);
		LinearLayout https_layout = (LinearLayout) findViewById(R.id.https_layout);
		LinearLayout ftp_layout = (LinearLayout) findViewById(R.id.ftp_layout);
		LinearLayout ssh_layout = (LinearLayout) findViewById(R.id.ssh_layout);
		LinearLayout portforward_layout = (LinearLayout) findViewById(R.id.portforward_layout);
		LinearLayout exthttp_layout = (LinearLayout) findViewById(R.id.exthttp_layout);
		LinearLayout extrtsp_layout = (LinearLayout) findViewById(R.id.extrtsp_layout);
		LinearLayout evercam_layout = (LinearLayout) findViewById(R.id.evercam_layout);
		LinearLayout username_layout = (LinearLayout) findViewById(R.id.username_layout);
		LinearLayout password_layout = (LinearLayout) findViewById(R.id.password_layout);

		TextView username_value = (TextView) findViewById(R.id.usernamevalue_detail);
		TextView password_value = (TextView) findViewById(R.id.passwordvalue_detail);

		snapshot = (ImageView) findViewById(R.id.snapshot_img);

		http_button = (Button) findViewById(R.id.camerabutton_http);
		rtsp_button = (Button) findViewById(R.id.camerabutton_rtsp);
		editBtn = (Button) findViewById(R.id.editButton);
		portForwardBtn = (Button) findViewById(R.id.portForwardButton);
		setDeviceBtn = (Button) findViewById(R.id.setAsDeviceButton);
		setCameraBtn = (Button) findViewById(R.id.setAsCameraButton);

		// Is a camera
		if (camera.getFlag() == Constants.TYPE_CAMERA)
		{
			// if username and password not exist in database
			if (camera.getUsername() == null)
			{
				SimpleDBConnect simpleDB = new SimpleDBConnect(
						ctxt);
				simpleDB.queryDefaultPassword(camera.getVendor());
				cameraOperation.updateAttributeString(camera.getIP(),
						camera.getSsid(), "username", simpleDB.username);
				cameraOperation.updateAttributeString(camera.getIP(),
						camera.getSsid(), "password", simpleDB.password);
				camera.setUsername(simpleDB.username);
				camera.setPassword(simpleDB.password);
			}

			setDeviceBtn.setVisibility(View.VISIBLE);
			setCameraBtn.setVisibility(View.GONE);
			editBtn.setVisibility(View.VISIBLE);
		}
		// Is not a camera
		else
		{
			setDeviceBtn.setVisibility(View.GONE);
			setCameraBtn.setVisibility(View.VISIBLE);
			editBtn.setVisibility(View.GONE);

			http_button.setVisibility(View.GONE);
			rtsp_button.setVisibility(View.GONE);
		}

		// set images
		ImageView img = (ImageView) findViewById(R.id.cameradetail_img);
		if (camera.getFlag() == Constants.TYPE_CAMERA)
		{
			ResourceHelper resourceHelper = new ResourceHelper(ctxt);
			img.setImageResource(resourceHelper.getCameraImageId(camera));
		}
		else
		{
			img.setImageResource(R.drawable.question_img_trans);			
		}

		ip.setText(camera.getIP());
		mac.setText(camera.getMAC().toUpperCase(Locale.UK));
		vendor.setText(camera.getVendor());

		// If is demo camera
		if (camera.getSsid().equals("sample"))
		{
			snapshot.setVisibility(View.VISIBLE);
			setDeviceBtn.setVisibility(View.VISIBLE);
			setCameraBtn.setVisibility(View.GONE);
			editBtn.setVisibility(View.VISIBLE);
			portForwardBtn.setVisibility(View.VISIBLE);

			handler.postDelayed(new Runnable(){

				@Override
				public void run()
				{
					snapshot.setImageBitmap(getSnapshot(
							"http://89.101.225.158:8101/Streaming/channels/1/picture",
							camera.getUsername(), camera.getPassword()));
				}
			}, 1000);
		}
		// Show snapshot
		else if (camera.isSupportedCamera() && camera.getHttp() != 0)
		{
			snapshot.setVisibility(View.VISIBLE);

			handler.postDelayed(new Runnable(){

				@Override
				public void run()
				{
					snapshot.setImageBitmap(getSnapshot(getSnapshotURL(),
							camera.getUsername(), camera.getPassword()));
				}
			}, 1000);
		}
		else if (camera.getHttp() == 0)
		{
			snapshot.setVisibility(View.GONE);
			http_layout.setVisibility(View.GONE);
		}

		if (camera.getFlag() == Constants.TYPE_CAMERA)
		{
			iscamera.setText(R.string.yes);
		}
		else
		{
			iscamera.setText(R.string.no);
		}

		// display model
		if (camera.hasModel())
		{
			model_layout.setVisibility(View.VISIBLE);
			model.setText(camera.getModel());
		}
		else
		{
			model_layout.setVisibility(View.GONE);
		}

		// show bonjour, upnp, onvif
		if (!(camera.getBonjour() == 0))
		{
			bonjour_layout.setVisibility(View.VISIBLE);
			isbonjour.setText(R.string.yes);
		}
		else
		{
			bonjour_layout.setVisibility(View.GONE);
		}

		if (!(camera.getUpnp() == 0))
		{
			upnp_layout.setVisibility(View.VISIBLE);
			isupnp.setText(R.string.yes);
		}
		else
		{
			upnp_layout.setVisibility(View.GONE);
		}
		if (!(camera.getOnvif() == 0))
		{
			onvif_layout.setVisibility(View.VISIBLE);
			isonvif.setText(R.string.yes);
		}
		else
		{
			onvif_layout.setVisibility(View.GONE);
		}

		// display rtsp if not null
		if (!(camera.getRtsp() == 0))
		{
			rtsp_layout.setVisibility(View.VISIBLE);
			rtsp.setText(String.valueOf(camera.getRtsp()));
			if (camera.getFlag() == Constants.TYPE_CAMERA)
			{
				rtsp_button.setVisibility(View.VISIBLE);
			}
			// If not camera
			else
			{
				rtsp_button.setVisibility(View.GONE);
			}

		}
		else
		{
			rtsp_layout.setVisibility(View.GONE);
			rtsp_button.setVisibility(View.GONE);
		}
		// display http if not null
		if (camera.hasHTTP())
		{
			http_layout.setVisibility(View.VISIBLE);
			http_button.setVisibility(View.VISIBLE);
			http.setText(String.valueOf(camera.getHttp()));
		}
		else
		{
			http_layout.setVisibility(View.GONE);
			http_button.setVisibility(View.GONE);
		}

		// display internal https if exists
		if (camera.hasHTTPS())
		{
			https_layout.setVisibility(View.VISIBLE);
			https.setText(String.valueOf(camera.getHttps()));
		}
		else
		{
			https_layout.setVisibility(View.GONE);
		}
		// display internal ftp if exists
		if (camera.hasFTP())
		{
			ftp_layout.setVisibility(View.VISIBLE);
			ftp.setText(String.valueOf(camera.getFtp()));
		}
		else
		{
			ftp_layout.setVisibility(View.GONE);
		}

		// display internal ssh if exists
		if (camera.hasSSH())
		{
			ssh_layout.setVisibility(View.VISIBLE);
			ssh.setText(String.valueOf(camera.getSsh()));
		}
		else
		{
			ssh_layout.setVisibility(View.GONE);
		}

		// display external http if not null
		if (camera.hasExternalHttp())
		{
			exthttp_layout.setVisibility(View.VISIBLE);
			exthttp.setText(String.valueOf(camera.getExthttp()));
			// show port forward
			portforward_layout.setVisibility(View.VISIBLE);
			evercam_layout.setVisibility(View.VISIBLE);

		}
		else
		{
			exthttp_layout.setVisibility(View.GONE);
			portforward_layout.setVisibility(View.GONE);
			evercam_layout.setVisibility(View.GONE);
		}

		// display external rtsp if not equals 0
		if (camera.hasExternalRtsp())
		{
			extrtsp_layout.setVisibility(View.VISIBLE);
			extrtsp.setText(String.valueOf(camera.getExtrtsp()));
			// show port forward
			portforward_layout.setVisibility(View.VISIBLE);
			evercam_layout.setVisibility(View.VISIBLE);
		}
		else
		{
			extrtsp_layout.setVisibility(View.GONE);
			portforward_layout.setVisibility(View.GONE);
			evercam_layout.setVisibility(View.GONE);
		}

		// username and password
		if (camera.getUsername() != null && !camera.getUsername().isEmpty())
		{
			username_layout.setVisibility(View.VISIBLE);
			password_layout.setVisibility(View.VISIBLE);
			username_value.setText(camera.getUsername());
			if (!camera.getPassword().equals(""))
			{
				password_value.setText(camera.getPassword());
			}
			else
			{
				password_value.setText("<blank>");
			}

			if (camera.getSsid().equals("sample"))
			{
				username_layout.setVisibility(View.GONE);
				password_layout.setVisibility(View.GONE);
			}

		}
		else
		{
			username_layout.setVisibility(View.GONE);
			password_layout.setVisibility(View.GONE);
		}
		firstseen.setText(camera.getFirstSeen());
		lastseen.setText(camera.getLastSeen());
	}

	public Bitmap getSnapshot(String URL, String username, String password)
	{
		Bitmap bitmap = null;
		if (URL != null && username != null)
		{
			try
			{
				DefaultHttpClient client = new DefaultHttpClient();
				client.getCredentialsProvider().setCredentials(
						new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
						new UsernamePasswordCredentials(username, password));
				HttpGet request = new HttpGet(URL);
				HttpResponse response;
				response = client.execute(request);

				HttpEntity entity = response.getEntity();
				InputStream inputStream = entity.getContent();
				bitmap = BitmapFactory.decodeStream(inputStream);
			}
			catch (ClientProtocolException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return bitmap;
	}

	public String getSnapshotURL()
	{
		if (camera.getVendor().equals(Constants.VENDOR_HIKVISION)
				&& camera.getHttp() != 0)
		{
			return "http://" + this.ipstring + ":" + camera.getHttp()
					+ "/Streaming/channels/1/picture";
		}
		else if (camera.getVendor().equals(Constants.VENDOR_AXIS)
				&& camera.getHttp() != 0)
		{
			return "http://" + this.ipstring + ":" + camera.getHttp()
					+ "/jpg/image.jpg";
		}
		else if (camera.getVendor().equals(Constants.VENDOR_UBIQUITI)
				&& camera.getHttp() != 0)
		{
			return "http://" + this.ipstring + ":" + camera.getHttp()
					+ "/snapshot.cgi";
		}
		else if (camera.getVendor().equals(Constants.VENDOR_YCAM)
				&& camera.getHttp() != 0)
		{
			return "http://" + this.ipstring + ":" + camera.getHttp()
					+ "/snapshot.jpg";
		}
		else if (camera.getVendor().equals(Constants.VENDOR_TPLINK)
				&& camera.getHttp() != 0)
		{
			return "http://" + this.ipstring + ":" + camera.getHttp()
					+ "/jpg/image.jpg";
		}
		else
		{
			return null;
		}
	}

	private void showSetAsCameraDialog()
	{
		AlertDialog alertDialog = new AlertDialog.Builder(
				CameraDetailActivity.this)
				.setMessage(R.string.confirmIsCamera)
				.setPositiveButton(R.string.yes,
						new DialogInterface.OnClickListener(){
							@Override
							public void onClick(DialogInterface dialog,
									int which)
							{
								cameraOperation.updateAttributeInt(ipstring,
										ssid, "flag", Constants.TYPE_CAMERA);
								setUpPage();
								showSendFeedback(Constants.TITLE_SETDEVICE);
							}
						}).setNegativeButton(R.string.no, null).create();
		alertDialog.show();
	}

	private void showSetAsDeviceDialog()
	{
		AlertDialog alertDialog = new AlertDialog.Builder(
				CameraDetailActivity.this)
				.setMessage(R.string.confirmNotCamera)
				.setPositiveButton(R.string.yes,
						new DialogInterface.OnClickListener(){
							@Override
							public void onClick(DialogInterface dialog,
									int which)
							{
								cameraOperation.updateAttributeInt(ipstring,
										ssid, "flag", Constants.TYPE_OTHERS);
								setUpPage();
								showSendFeedback(Constants.TITLE_SETCAMERA);

							}
						}).setNegativeButton(R.string.no, null).create();
		alertDialog.show();
	}

	private void showSendFeedback(final String type)
	{
		AlertDialog alertDialog1 = new AlertDialog.Builder(
				CameraDetailActivity.this)

				.setMessage(R.string.reportwarning)
				.setPositiveButton(R.string.yes,
						new DialogInterface.OnClickListener(){
							@Override
							public void onClick(DialogInterface dialog,
									int which)
							{
								Intent data = new Intent(Intent.ACTION_SENDTO);
								data.setData(Uri
										.parse("mailto:liuting.du@mhlabs.net"));
								data.putExtra(Intent.EXTRA_SUBJECT, type);
								data.putExtra(Intent.EXTRA_TEXT, "Device info:"
										+ camera.toString() + "From IP:"
										+ NetInfo.getExternalIP());
								startActivity(data);
							}
						}).setNegativeButton(R.string.no, null).create();
		alertDialog1.show();
	}

	private void showEditDialog()
	{
		LayoutInflater mInflater = LayoutInflater.from(ctxt);
		final View editView = mInflater.inflate(R.layout.edit_dialog, null);
		final AlertDialog.Builder editBuilder = new AlertDialog.Builder(this);
		editBuilder.setView(editView);

		final EditText editModel = (EditText) editView
				.findViewById(R.id.editModel_value);
		final EditText editHttp = (EditText) editView
				.findViewById(R.id.editHttp_edit);
		final EditText editRtsp = (EditText) editView
				.findViewById(R.id.editRtsp_edit);
		final EditText editHttps = (EditText) editView
				.findViewById(R.id.editHttps_edit);
		final EditText editFtp = (EditText) editView
				.findViewById(R.id.editFtp_edit);
		final EditText editSsh = (EditText) editView
				.findViewById(R.id.editSsh_edit);
		final EditText editUsername = (EditText) editView
				.findViewById(R.id.editUsername_edit);
		final EditText editPassword = (EditText) editView
				.findViewById(R.id.editPassword_edit);

		editModel.setText(camera.getModel());

		if (camera.getHttp() == 0)
		{
			editHttp.setText(null);
		}
		else
		{
			editHttp.setText(String.valueOf(camera.getHttp()));
		}

		if (camera.getRtsp() == 0)
		{
			editRtsp.setText(null);
		}
		else
		{
			editRtsp.setText(String.valueOf(camera.getRtsp()));
		}
		if (camera.getHttps() == 0)
		{
			editHttps.setText(null);
		}
		else
		{
			editHttps.setText(String.valueOf(camera.getHttps()));
		}
		if (camera.getFtp() == 0)
		{
			editFtp.setText(null);
		}
		else
		{
			editFtp.setText(String.valueOf(camera.getFtp()));
		}
		if (camera.getSsh() == 0)
		{
			editSsh.setText(null);
		}
		else
		{
			editSsh.setText(String.valueOf(camera.getSsh()));
		}

		editUsername.setText(camera.getUsername());
		editPassword.setText(camera.getPassword());

		if (camera.getSsid().equals("sample"))
		{
			editUsername.setText("");
			editPassword.setText("");
		}
		editBuilder.setPositiveButton(R.string.save,
				new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						if (camera.isDemoCamera())
						{
							closeDialog(dialog);
						}
						else
						{
							String newModel = editModel.getText().toString();
							String newUsername = editUsername.getText()
									.toString();
							String newPassword = editPassword.getText()
									.toString();
							String httpStr = editHttp.getText().toString();
							String rtspStr = editRtsp.getText().toString();
							String httpsStr = editHttps.getText().toString();
							String ftpStr = editFtp.getText().toString();
							String sshStr = editSsh.getText().toString();

							if (httpStr.length() == 0)
							{
								httpStr = "0";
							}
							if (rtspStr.length() == 0)
							{
								rtspStr = "0";
							}
							if (httpsStr.length() == 0)
							{
								httpsStr = "0";
							}
							if (ftpStr.length() == 0)
							{
								ftpStr = "0";
							}
							if (sshStr.length() == 0)
							{
								sshStr = "0";
							}

							try
							{
								int newHttp = Integer.parseInt(httpStr);
								int newRtsp = Integer.parseInt(rtspStr);
								int newHttps = Integer.parseInt(httpsStr);
								int newFtp = Integer.parseInt(ftpStr);
								int newSsh = Integer.parseInt(sshStr);

								// If ports all in 0-65535
								if (isInPortRange(newHttp)
										&& isInPortRange(newRtsp)
										&& isInPortRange(newHttps)
										&& isInPortRange(newFtp)
										&& isInPortRange(newSsh))
								{
									cameraOperation.updateAttributeString(
											camera.getIP(), camera.getSsid(),
											"model", newModel);
									cameraOperation.updateAttributeInt(
											camera.getIP(), camera.getSsid(),
											"http", newHttp);
									cameraOperation.updateAttributeInt(
											camera.getIP(), camera.getSsid(),
											"rtsp", newRtsp);
									cameraOperation.updateAttributeInt(
											camera.getIP(), camera.getSsid(),
											"https", newHttps);
									cameraOperation.updateAttributeInt(
											camera.getIP(), camera.getSsid(),
											"ftp", newFtp);
									cameraOperation.updateAttributeInt(
											camera.getIP(), camera.getSsid(),
											"ssh", newSsh);
									cameraOperation.updateAttributeString(
											camera.getIP(), camera.getSsid(),
											"username", newUsername);
									cameraOperation.updateAttributeString(
											camera.getIP(), camera.getSsid(),
											"password", newPassword);
									closeDialog(dialog);
									setUpPage();
								}
								else
								{
									showPortNotInRange();
									keepDialog(dialog);
								}
							}
							catch (NumberFormatException e)
							{
								showPortNotInRange();
								keepDialog(dialog);
							}
						}

					}
				});
		editBuilder.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						closeDialog(dialog);
					}
				});
		editBuilder.setTitle("Edit Details  (" + camera.getIP() + ")");
		editBuilder.setCancelable(false);
		editBuilder.show();
	}

	private void showDemoSetAsCameraDialog()
	{
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(
				CameraDetailActivity.this);
		alertDialog.setTitle(R.string.userGuide);
		alertDialog
				.setMessage(R.string.userGuideMsg);
		alertDialog.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which)
					{

					}
				});

		alertDialog.show();
	}

	private boolean isInPortRange(int port)
	{
		if (port >= 0 && port <= 65535)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public static void keepDialog(DialogInterface dialog)
	{
		try
		{
			Field field = dialog.getClass().getSuperclass()
					.getDeclaredField("mShowing");
			field.setAccessible(true);
			field.set(dialog, false);
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
	}

	private void showPortNotInRange()
	{
		Toast toast = Toast.makeText(ctxt,
				R.string.portRangeMsg1, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	public static void closeDialog(DialogInterface dialog)
	{
		try
		{
			Field field = dialog.getClass().getSuperclass()
					.getDeclaredField("mShowing");
			field.setAccessible(true);
			field.set(dialog, true);
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
	}
}
package io.evercam.connect;

import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

import io.evercam.API;
import io.evercam.CameraBuilder;
import io.evercam.CameraDetail;
import io.evercam.EvercamException;
import io.evercam.Model;
import io.evercam.Vendor;
import io.evercam.connect.db.Camera;
import io.evercam.connect.db.CameraOperation;
import io.evercam.connect.helper.SharedPrefsManager;
import io.evercam.connect.net.NetInfo;
import io.evercam.network.ipscan.PortScan;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

public class AddToEvercamActivity extends Activity
{
	private Camera camera;
	private EditText idEdit;
	private EditText nameEdit;
	private EditText snapshotEdit;
	private EditText usernameEdit;
	private EditText passwordEdit;
	private EditText exthttpEdit;
	private EditText modelEdit;
	private EditText vendorEdit;
	private EditText macEdit;
	private RadioButton publicRadioBtn;
	private Button addBtn;
	private CreateCameraTask createCameraTask;
	private SharedPreferences sharedPrefs;
	private String cameraId;
	private String cameraName;
	private String snapshotPath;
	private boolean isPublic;
	private int exthttp;
	private String cameraUsername;
	private String cameraPassword;
	private String cameraModel;
	private String cameraVendor;
	private String cameraMac;
	private ProgressDialog progressDialog;
	private String externalIp = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_to_evercam);

		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		camera = (Camera) getIntent().getSerializableExtra("camera");
		initPage();
		fillPage();

		addBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v)
			{
				if (isPassed())
				{
					if (detailsChecked())
					{
						if (createCameraTask != null)
						{
							createCameraTask = null;
						}
						createCameraTask = new CreateCameraTask();
						createCameraTask.execute();
					}
				}
				else
				{
					showShortToast(R.string.extPortNotOpen);
				}
			}
		});

		exthttpEdit.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable arg0)
			{
				exthttpEdit.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
			}
		});

		exthttpEdit.setOnFocusChangeListener(new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View v, boolean hasFocus)
			{
				if (!hasFocus)
				{
					if (extHttpChecked())
					{
						portCheck(exthttp);
					}
				}
			}
		});
	}

	private void initPage()
	{
		idEdit = (EditText) findViewById(R.id.addCameraId_edit);
		nameEdit = (EditText) findViewById(R.id.addCameraName_edit);
		snapshotEdit = (EditText) findViewById(R.id.addCameraJpg_edit);
		usernameEdit = (EditText) findViewById(R.id.addUsername_edit);
		passwordEdit = (EditText) findViewById(R.id.addPassword_edit);
		exthttpEdit = (EditText) findViewById(R.id.addExtHttp_edit);
		modelEdit = (EditText) findViewById(R.id.addModel_value);
		vendorEdit = (EditText) findViewById(R.id.addVendor_value);
		macEdit = (EditText) findViewById(R.id.addMac_value);
		publicRadioBtn = (RadioButton) findViewById(R.id.publicRadio);
		addBtn = (Button) findViewById(R.id.button_creatCamera);
	}

	private void fillPage()
	{
		if (camera.getExthttp() > 0)
		{
			portCheck(camera.getExthttp());
			exthttpEdit.setText(String.valueOf(camera.getExthttp()));
		}
		if (camera.hasJpgURL())
		{
			snapshotEdit.setText(camera.getJpg());
		}
		usernameEdit.setText(camera.getUsername());
		passwordEdit.setText(camera.getPassword());
		macEdit.setText(camera.getMAC().toLowerCase(Locale.UK));
		macEdit.setEnabled(false);
		macEdit.setTextColor(Color.parseColor("#808080"));
		if (camera.hasModel())
		{
			if (camera.getModel().startsWith(camera.getVendor()))
			{
				camera.setModel(camera.getModel().substring(camera.getVendor().length() + 1).trim());
			}
			modelEdit.setText(camera.getModel().toLowerCase());
		}
		if (camera.hasVendor())
		{
			vendorEdit.setText(camera.getVendor().toLowerCase());
			vendorEdit.setEnabled(false);
			vendorEdit.setTextColor(Color.parseColor("#808080"));
		}
		idEdit.setText(SharedPrefsManager.getEvercamUsername(sharedPrefs) + random());
		nameEdit.setText(R.string.myCamera);
	}

	private void showShortToast(int id)
	{
		Toast.makeText(this, id, Toast.LENGTH_SHORT).show();
	}

	private void showShortToast(String msg)
	{
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	private boolean detailsChecked()
	{
		String idStr = idEdit.getText().toString();
		String nameStr = nameEdit.getText().toString();
		String snapshotStr = snapshotEdit.getText().toString();
		String exthttpStr = exthttpEdit.getText().toString();
		String usernameStr = usernameEdit.getText().toString();
		String passwordStr = passwordEdit.getText().toString();

		if (extHttpChecked())
		{
			if (idStr.length() == 0)
			{
				showShortToast(R.string.idEmpty);
				idEdit.requestFocus();
			}
			else if (nameStr.length() == 0)
			{
				showShortToast(R.string.nameEmpty);
				nameEdit.requestFocus();
			}
			else if (snapshotStr.length() == 0)
			{
				showShortToast(R.string.snapshotEmpty);
				snapshotEdit.requestFocus();
			}
			else if (!snapshotStr.startsWith("/"))
			{
				showShortToast(R.string.snapshotInvalid);
			}
			else if (exthttpStr.length() == 0)
			{
				showShortToast(R.string.exthttpEmpty);
				exthttpEdit.requestFocus();
			}
			else if (usernameStr.length() == 0)
			{
				showShortToast(R.string.usernameEmpty);
				usernameEdit.requestFocus();
			}
			else if (passwordStr.length() == 0)
			{
				showShortToast(R.string.passwordEmpty);
				passwordEdit.requestFocus();
			}
			else
			{
				cameraId = idStr;
				cameraName = nameStr;
				snapshotPath = snapshotStr;
				isPublic = publicRadioBtn.isChecked();
				cameraUsername = usernameStr;
				cameraPassword = passwordStr;
				cameraModel = modelEdit.getText().toString();
				cameraVendor = vendorEdit.getText().toString();
				cameraMac = macEdit.getText().toString();
				return true;
			}
		}
		return false;
	}

	private boolean extHttpChecked()
	{
		String exthttpStr = exthttpEdit.getText().toString();
		try
		{
			exthttp = Integer.parseInt(exthttpStr);
			if (!(exthttp > 0 && exthttp <= 65535))
			{
				showShortToast(R.string.portRangeMsg);
				return false;
			}
			else
			{
				return true;
			}
		}
		catch (NumberFormatException e)
		{
			showShortToast(R.string.portRangeMsg);
			return false;
		}
	}

	private void showTick()
	{
		Drawable tick = getResources().getDrawable(R.drawable.tick);
		exthttpEdit.setCompoundDrawablesWithIntrinsicBounds(null, null, tick, null);
	}

	private void showCross()
	{
		Drawable cross = getResources().getDrawable(R.drawable.cross);
		exthttpEdit.setCompoundDrawablesWithIntrinsicBounds(null, null, cross, null);
	}

	private boolean isPassed()
	{
		Drawable[] drawables = exthttpEdit.getCompoundDrawables();
		for (Drawable drawable : drawables)
		{
			if (drawable != null)
			{
				if (drawable.equals(getResources().getDrawable(R.drawable.tick)))
				;
				return true;
			}
		}
		return false;
	}

	private void portCheck(final int port)
	{
		Handler handler = new Handler();

		handler.postDelayed(new Runnable(){
			@Override
			public void run()
			{
				if (externalIp != null)
				{
					if (PortScan.isPortReachable(externalIp, port))
					{
						showTick();
					}
					else
					{
						showCross();
					}
				}
				else
				{
					externalIp = NetInfo.getExternalIP();
					portCheck(port);
				}
			}
		}, 1000);
	}

	private class CreateCameraTask extends AsyncTask<Void, Void, Boolean>
	{
		CameraDetail cameraDetail;
		String errorMsg = "Error, please try again later.";

		@Override
		protected void onPreExecute()
		{
			progressDialog = ProgressDialog.show(AddToEvercamActivity.this, "",
					"Creating camera...", true);
		}

		@Override
		protected void onPostExecute(Boolean success)
		{
			if (progressDialog.isShowing())
			{
				progressDialog.dismiss();
			}
			if (success)
			{
				showShortToast(R.string.Success);
				CameraOperation cameraOperation = new CameraOperation(AddToEvercamActivity.this);
				cameraOperation.updateAttributeInt(camera.getIP(), camera.getSsid(), "evercam", 1);
				AddToEvercamActivity.this.finish();
			}
			else
			{
				showShortToast(errorMsg);
			}
		}

		@Override
		protected Boolean doInBackground(Void... arg0)
		{
			initialDetailObject();
			try
			{
				API.setAuth(SharedPrefsManager.getEvercamUsername(sharedPrefs),
						SharedPrefsManager.getEvercamPassword(sharedPrefs));
				io.evercam.Camera camera = io.evercam.Camera.create(cameraDetail);
				if (camera.getId().equals(cameraId))
				{
					return true;
				}
				return false;
			}
			catch (EvercamException e)
			{
				errorMsg = e.getMessage();
				return false;
			}
		}

		private void initialDetailObject()
		{
			while (externalIp == null)
			{
				externalIp = NetInfo.getExternalIP();
			}

			try
			{
				Model model = Vendor.getById(cameraVendor).getModel(cameraModel);
				cameraModel = model.getName();
			}
			catch (EvercamException e1)
			{
				errorMsg = e1.getMessage();
			}

			CameraBuilder cameraBuilder;
			try
			{
				cameraBuilder = new CameraBuilder(cameraId, cameraName, isPublic,
						new String[] { "http://" + externalIp + ":" + exthttp })
						.setTimeZone(TimeZone.getDefault().getID())
						.setBasicAuth(cameraUsername, cameraPassword).setSnapshotJPG(snapshotPath);

				if (cameraVendor != null)
				{
					if (!cameraVendor.equals("Unknown Vendor") && cameraVendor.length() != 0)
					{
						cameraBuilder.setVendor(cameraVendor);
					}
				}
				if (cameraModel != null)
				{
					if (cameraModel.length() != 0)
					{
						cameraBuilder.setModel(cameraModel);
					}
				}
				if (cameraMac != null)
				{
					if (cameraMac.length() != 0)
					{
						cameraBuilder.setMacAddress(cameraMac);
					}
				}
				cameraDetail = cameraBuilder.build();
			}
			catch (EvercamException e)
			{
				e.printStackTrace();
			}
		}
	}

	public int random()
	{
		Random rand = new Random();
		return rand.nextInt(100001);
	}
}

package io.evercam.connect;

import java.util.TimeZone;

import io.evercam.CameraDetail;
import io.evercam.EvercamException;
import io.evercam.connect.db.Camera;
import io.evercam.connect.db.SharedPrefsManager;
import io.evercam.connect.net.NetInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;

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
	private ProgressDialog progressDialog;

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
				if(detailsChecked())
				{
					if(createCameraTask != null)
					{
						createCameraTask = null;
					}
					createCameraTask = new CreateCameraTask();
					createCameraTask.execute();
				}
			}			
		});
	}
	
	private void  initPage()
	{
		idEdit = (EditText)findViewById(R.id.addCameraId_edit);
		nameEdit = (EditText)findViewById(R.id.addCameraName_edit);
		snapshotEdit = (EditText)findViewById(R.id.addCameraJpg_edit);
		usernameEdit = (EditText)findViewById(R.id.addUsername_edit);
		passwordEdit = (EditText)findViewById(R.id.addPassword_edit);
		exthttpEdit = (EditText)findViewById(R.id.addExtHttp_edit);
		modelEdit = (EditText)findViewById(R.id.addModel_value);
		vendorEdit = (EditText)findViewById(R.id.addVendor_value);
		publicRadioBtn = (RadioButton)findViewById(R.id.publicRadio);
		addBtn = (Button)findViewById(R.id.button_creatCamera);
	}
	
	private void fillPage()
	{
		exthttpEdit.setText(String.valueOf(camera.getExthttp()));
		snapshotEdit.setText(camera.getSnapshotJpgUrl());
		usernameEdit.setText(camera.getUsername());
		passwordEdit.setText(camera.getPassword());
		if(camera.hasModel())
		{
			if(camera.getModel().startsWith(camera.getVendor()))
			{
				camera.setModel(camera.getModel().substring(camera.getVendor().length()+1).trim());
			}
		modelEdit.setText(camera.getModel().toLowerCase());
		}
		if(camera.hasVendor())
		{
		vendorEdit.setText(camera.getVendor().toLowerCase());
		}
		String[] evercamAccount = SharedPrefsManager.getEvercam(sharedPrefs);
		idEdit.setText(evercamAccount[0] + camera.getMAC());
		nameEdit.setText(R.string.myCamera);
	}
	
	private void showErrorToast(int id)
	{
		Toast.makeText(this, id, Toast.LENGTH_SHORT).show();
	}
	
	private boolean detailsChecked()
	{
		String idStr = idEdit.getText().toString();
		String nameStr = nameEdit.getText().toString();
		String snapshotStr = snapshotEdit.getText().toString();
		String exthttpStr = exthttpEdit.getText().toString();
		String usernameStr = usernameEdit.getText().toString();
		String passwordStr = passwordEdit.getText().toString();
		
		try
		{
			exthttp = Integer.parseInt(exthttpStr);
			if (exthttp >= 0 && exthttp <= 65535)
			{
				return true;
			}
			else
			{
				showErrorToast(R.string.portRangeMsg);
				return false;
			}
		}
		catch (NumberFormatException e)
		{
			showErrorToast(R.string.portRangeMsg);
		}
		
		if(idStr.length()== 0)
		{
			showErrorToast(R.string.idEmpty);
			idEdit.requestFocus();
		}
		else if(nameStr.length()== 0)
		{
			showErrorToast(R.string.nameEmpty);
			nameEdit.requestFocus();
		}
		else if(snapshotStr.length()== 0)
		{
			showErrorToast(R.string.snapshotEmpty);
			snapshotEdit.requestFocus();
		}
		else if (!snapshotStr.startsWith("/"))
		{
			showErrorToast(R.string.snapshotInvalid);
		}
		else if(exthttpStr.length()== 0)
		{
			showErrorToast(R.string.exthttpEmpty);
			exthttpEdit.requestFocus();
		}
		else if(usernameStr.length()== 0)
		{
			showErrorToast(R.string.usernameEmpty);
			usernameEdit.requestFocus();
		}
		else if(passwordStr.length()== 0)
		{
			showErrorToast(R.string.passwordEmpty);
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
			cameraModel= modelEdit.getText().toString();
			cameraVendor = vendorEdit.getText().toString();			
			return true;
		}
		return false;
	}
	
	private class CreateCameraTask extends AsyncTask<Void,Void,Void>
	{
		CameraDetail cameraDetail = new CameraDetail();
	
		@Override
		protected void onPreExecute()
		{
			progressDialog = ProgressDialog.show(AddToEvercamActivity.this,
					"", "Creating camera...", true);
		}

		@Override
		protected void onPostExecute(Void result)
		{
			if (progressDialog.isShowing())
			{
				progressDialog.dismiss();
			}
		}


		@Override
		protected Void doInBackground(Void... arg0)
		{
//			initialDetailObject();
//			try
//			{
//				io.evercam.Camera.create(cameraDetail);
//				Log.v("evercamconnect", "success");
//			}
//			catch (EvercamException e)
//			{
//				e.printStackTrace();
//				Log.v("evercamconnect", "failure");
//			}
			try
			{
				Thread.sleep(5000);
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		private void initialDetailObject()
		{
			String externalIp = NetInfo.getExternalIP();
			String timezoneID = TimeZone.getDefault().getID();
			Log.v("evercamconnect", timezoneID);
			cameraDetail.setId(cameraId);
			cameraDetail.setName(cameraName);
			cameraDetail.setBasicAuth(cameraUsername, cameraPassword);
			cameraDetail.setSnapshotJPG(snapshotPath);
			cameraDetail.setPublic(isPublic);
			cameraDetail.setEndpoints(new String[]{"http://" + externalIp});
			cameraDetail.setTimezone(timezoneID);
			if(!cameraVendor.equals("Unknown Vendor") && cameraVendor.length()!=0)
			{
				cameraDetail.setVendor(cameraVendor);
			}
			if(cameraModel.length()!=0)
			{
				cameraDetail.setModel(cameraModel);
			}
		}
	}
}

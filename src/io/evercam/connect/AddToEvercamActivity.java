package io.evercam.connect;

import io.evercam.connect.db.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.app.Activity;

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
//	private RadioGroup publicRadioGroup;
	private RadioButton publicRadioBtn;
//	private RadioButton privateRadioBtn;
	private Button addBtn;
	private AddToEvercamTask addEvercamTask;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_to_evercam);
		
		addEvercamTask = new AddToEvercamTask();
		
		camera = (Camera) getIntent().getSerializableExtra("camera");
		initPage();
		fillPage();
		
		addBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v)
			{
				checkDetails();
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
//		publicRadioGroup = (RadioGroup)findViewById(R.id.radioGroup_isPublic);
		publicRadioBtn = (RadioButton)findViewById(R.id.publicRadio);
//		privateRadioBtn = (RadioButton)findViewById(R.id.privateRadio);
		addBtn = (Button)findViewById(R.id.button_creatCamera);
	}
	
	private void fillPage()
	{
		exthttpEdit.setText(String.valueOf(camera.getExthttp()));
		snapshotEdit.setText(camera.getSnapshotJpgUrl());
		usernameEdit.setText(camera.getUsername());
		passwordEdit.setText(camera.getPassword());
		modelEdit.setText(camera.getModel());
		vendorEdit.setText(camera.getVendor());
	}
	
	private class AddToEvercamTask extends AsyncTask<Void,Void,Void>
	{
	
		@Override
		protected void onPreExecute()
		{
			
		}

		@Override
		protected Void doInBackground(Void... arg0)
		{
			return null;
		}
		
	}
	
	private void showErrorToast(int id)
	{
		Toast.makeText(this, id, Toast.LENGTH_SHORT).show();
	}
	
	private void checkDetails()
	{
		String idStr = idEdit.getText().toString();
		String nameStr = nameEdit.getText().toString();
		String snapshotStr = snapshotEdit.getText().toString();
		String exthttpStr = exthttpEdit.getText().toString();
		String usernameStr = usernameEdit.getText().toString();
		String passwordStr = passwordEdit.getText().toString();
		String modelStr = modelEdit.getText().toString();
		String vendorStr = vendorEdit.getText().toString();
		Boolean isPublic = publicRadioBtn.isChecked();
		
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
	}
}

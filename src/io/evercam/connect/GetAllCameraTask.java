package io.evercam.connect;

import io.evercam.API;
import io.evercam.Camera;
import io.evercam.EvercamException;
import io.evercam.User;
import io.evercam.connect.db.CameraOperation;
import io.evercam.connect.helper.SharedPrefsManager;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

public class GetAllCameraTask extends AsyncTask<Void, HashMap<String, Object>, ArrayList<Camera>>
{
	ArrayList<Camera> cameras = null;
	DiscoverMainActivity discoverMainActivity;
	Context ctxt;
	CameraOperation cameraOperation;
	String username;
	String password;

	public GetAllCameraTask(DiscoverMainActivity discoverMainActivity)
	{
		this.discoverMainActivity = discoverMainActivity;
		this.ctxt = discoverMainActivity.getApplicationContext();
		readAuth();
	}

	@Override
	protected ArrayList<Camera> doInBackground(Void... params)
	{
		try
		{
			API.setAuth(username, password);
			cameras = User.getCameras(username);
			discoverMainActivity.evercamCameraList = cameras;
		}
		catch (EvercamException e)
		{
			e.printStackTrace();
		}
		return cameras;
	}

	private void readAuth()
	{
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctxt);
		username = SharedPrefsManager.getEvercamUsername(sharedPrefs);
		password = SharedPrefsManager.getEvercamPassword(sharedPrefs);
	}
}
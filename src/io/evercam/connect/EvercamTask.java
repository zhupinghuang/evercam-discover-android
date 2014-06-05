package io.evercam.connect;

import java.util.Locale;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import io.evercam.Auth;
import io.evercam.EvercamException;
import io.evercam.Model;
import io.evercam.Vendor;
import io.evercam.connect.db.Camera;
import io.evercam.connect.db.CameraOperation;

public class EvercamTask extends AsyncTask<Void, Void, Void>
{
	public String username;
	public String password;
	private String jpgURL;
	private String streamURL;
	private Camera camera;
	CameraOperation cameraOperation;

	public EvercamTask(Camera camera, Context ctxt)
	{
		this.camera = camera;
		cameraOperation = new CameraOperation(ctxt);
	}

	@Override
	protected Void doInBackground(Void... arg0)
	{
		Vendor vendor = null;
		try
		{
			vendor = Vendor.getById(camera.getVendor().toLowerCase(Locale.UK));
		}
		catch (EvercamException e)
		{
			Log.e("evercamconnect", e.getMessage());
		}
		if (vendor != null)
		{
			if (!camera.hasUsername())
			{
				fillDefaultAuth(vendor);
			}

			fillDefaultURL(vendor);
		}

		return null;
	}

	private void fillDefaultAuth(Vendor vendor)
	{
		try
		{
			username = vendor.getModel("default").getDefaults().getAuth(Auth.TYPE_BASIC)
					.getUsername();
			password = vendor.getModel("default").getDefaults().getAuth(Auth.TYPE_BASIC)
					.getPassword();
			cameraOperation.updateAttributeString(camera.getIP(), camera.getSsid(), "username",
					username);
			cameraOperation.updateAttributeString(camera.getIP(), camera.getSsid(), "password",
					password);
		}
		catch (EvercamException e)
		{
			Log.e("evercamconnect", e.getMessage());
		}
	}

	private void fillDefaultURL(Vendor vendor)
	{
		String vendorId = "";
		try
		{
			vendorId = vendor.getId();
			jpgURL = vendor.getModel(Model.DEFAULT_MODEL).getDefaults().getJpgURL();
			streamURL = vendor.getModel(Model.DEFAULT_MODEL).getDefaults().getH264URL();
			if (!jpgURL.startsWith("/"))
			{
				jpgURL = "/" + jpgURL;
			}
			if (!streamURL.startsWith("/"))
			{
				streamURL = "/" + streamURL;
			}
			cameraOperation.updateAttributeString(camera.getIP(), camera.getSsid(), "jpg", jpgURL);
			cameraOperation.updateAttributeString(camera.getIP(), camera.getSsid(), "h264",
					streamURL);
		}
		catch (EvercamException e)
		{
			Log.e("evercamconnect", "Exception with get default url " + vendorId + e.getMessage());
		}
	}

	public static String getUsername(String vendorId)
	{
		try
		{
			Vendor vendor = Vendor.getById(vendorId.toLowerCase(Locale.UK));
			if (vendor != null)
			{
				return vendor.getModel(Model.DEFAULT_MODEL).getDefaults().getAuth(Auth.TYPE_BASIC)
						.getUsername();
			}
		}
		catch (EvercamException e)
		{
			Log.e("evercamconnect",
					"Exception with get default username " + vendorId + e.getMessage());
		}
		return "";
	}

	public static String getPassword(String vendorId)
	{
		try
		{
			Vendor vendor = Vendor.getById(vendorId.toLowerCase(Locale.UK));
			if (vendor != null)
			{
				return vendor.getModel(Model.DEFAULT_MODEL).getDefaults().getAuth(Auth.TYPE_BASIC)
						.getPassword();
			}
		}
		catch (EvercamException e)
		{
			Log.e("evercamconnect",
					"Exception with get default password" + vendorId + e.getMessage());
		}
		return "";
	}

}

package io.evercam.connect;

import java.util.Locale;

import android.content.Context;
import android.os.AsyncTask;

import io.evercam.Auth;
import io.evercam.EvercamException;
import io.evercam.Vendor;
import io.evercam.connect.db.Camera;
import io.evercam.connect.db.CameraOperation;

public class EvercamTask extends AsyncTask<Void, Void, Void>
{
	public String username;
	public String password;
	private Camera camera;
	private Context ctxt;

	public EvercamTask(Camera camera, Context ctxt)
	{
		this.camera = camera;
		this.ctxt = ctxt;
	}

	@Override
	protected Void doInBackground(Void... arg0)
	{
		CameraOperation cameraOperation = new CameraOperation(ctxt);
		if(!camera.hasUsername())
		{
		username = getUsername(camera.getVendor());
		password = getPassword(camera.getVendor());
		cameraOperation.updateAttributeString(camera.getIP(), camera.getSsid(), "username",
				username);
		cameraOperation.updateAttributeString(camera.getIP(), camera.getSsid(), "password",
				password);
		}
		return null;
	}

	public static String getUsername(String vendorId)
	{
		try
		{
			Vendor vendor = Vendor.getById(vendorId.toLowerCase(Locale.UK));
			return vendor.getModel("*").getDefaults().getAuth(Auth.TYPE_BASIC).getUsername();
		}
		catch (EvercamException e)
		{
			return "";
		}
	}

	public static String getPassword(String vendorId)
	{
		try
		{
			Vendor vendor = Vendor.getById(vendorId.toLowerCase(Locale.UK));
			return vendor.getModel("*").getDefaults().getAuth(Auth.TYPE_BASIC).getPassword();
		}
		catch (EvercamException e)
		{
			return null;
		}
	}

}

package io.evercam.connect;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.Locale;

import io.evercam.Auth;
import io.evercam.EvercamException;
import io.evercam.Model;
import io.evercam.Vendor;
import io.evercam.connect.db.Camera;
import io.evercam.connect.db.CameraOperation;
import io.evercam.network.query.EvercamQuery;

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
			Log.e("evercamdiscover", e.getMessage());
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
			username = EvercamQuery.getDefaultUsernameByVendor(vendor);
			password = EvercamQuery.getDefaultPasswordByVendor(vendor);
			cameraOperation.updateAttributeString(camera.getIP(), camera.getSsid(), "username",
					username);
			cameraOperation.updateAttributeString(camera.getIP(), camera.getSsid(), "password",
					password);
		}
		catch (EvercamException e)
		{
			Log.e("evercamdiscover", e.getMessage());
		}
	}

	private void fillDefaultURL(Vendor vendor)
	{
		String vendorId = "";
		try
		{
			jpgURL = EvercamQuery.getDefaultJpgUrlByVendor(vendor);
			streamURL = EvercamQuery.getDefaultH264UrlByVendor(vendor);
			if (!jpgURL.isEmpty())
			{
				if (!jpgURL.startsWith("/"))
				{
					jpgURL = "/" + jpgURL;
					cameraOperation.updateAttributeString(camera.getIP(), camera.getSsid(), "jpg",
							jpgURL);
				}
			}
			if (!streamURL.isEmpty())
			{
				if (!streamURL.startsWith("/"))
				{
					streamURL = "/" + streamURL;
				}
				cameraOperation.updateAttributeString(camera.getIP(), camera.getSsid(), "h264",
						streamURL);
			}
		}
		catch (EvercamException e)
		{
			Log.e("evercamdiscover", "Exception with get default url " + vendorId + e.getMessage());
		}
	}

	public static String getUsername(String vendorId)
	{
		try
		{
			return Model.getDefaultModelByVendorId(vendorId.toLowerCase(Locale.UK)).getDefaults()
					.getAuth(Auth.TYPE_BASIC).getUsername();
		}
		catch (EvercamException e)
		{
			Log.e("evercamdiscover",
					"Exception with get default username " + vendorId + e.getMessage());
		}
		return "";
	}

	public static String getPassword(String vendorId)
	{
		try
		{
			return Model.getDefaultModelByVendorId(vendorId.toLowerCase(Locale.UK)).getDefaults()
					.getAuth(Auth.TYPE_BASIC).getPassword();
		}
		catch (EvercamException e)
		{
			Log.e("evercamdiscover",
					"Exception with get default password" + vendorId + e.getMessage());
		}
		return "";
	}

	/**
	 * Only used in camera detail page, if camera credentials are empty.
	 */
	public static void runAuthTaskOnly(CameraDetailActivity detailActivity)
	{
		new FillAuthOnlyTask(detailActivity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private static class FillAuthOnlyTask extends AsyncTask<Void, Void, Void>
	{
		CameraDetailActivity detailActivity;

		FillAuthOnlyTask(CameraDetailActivity detailActivity)
		{
			this.detailActivity = detailActivity;
		}

		@Override
		protected Void doInBackground(Void... params)
		{
			Camera camera = detailActivity.camera;
			CameraOperation cameraOperation = detailActivity.cameraOperation;
			String username = getUsername(camera.getVendor());
			String password = getPassword(camera.getVendor());
			cameraOperation.updateAttributeString(camera.getIP(), camera.getSsid(), "username",
					username);
			cameraOperation.updateAttributeString(camera.getIP(), camera.getSsid(), "password",
					password);
			camera.setUsername(username);
			camera.setPassword(password);
			return null;
		}
	}
}

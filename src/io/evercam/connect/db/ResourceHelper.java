package io.evercam.connect.db;

import android.content.Context;

public class ResourceHelper
{
	private Context ctxt;

	public ResourceHelper(Context ctxt)
	{
		this.ctxt = ctxt;
	}

	public int getImageId(String name)
	{
		return ctxt.getResources().getIdentifier(name, "drawable",
				ctxt.getPackageName());
	}

	public int getCameraImageId(Camera camera)
	{
		int idFromVendor = getImageId(camera.getVendor().toLowerCase());
		if (camera.hasModel())
		{
			// To be improved: make images match models without hardcoding
			if (camera.getModel().contains("M1054")
					|| camera.getModel().contains("1033"))
			{
				return getImageId("axis_m1033");
			}
			else if (camera.getModel().contains("1011"))
			{
				return getImageId("axis_m1011w");
			}

			else if (camera.getModel().contains("DS-2CD8133"))
			{
				return getImageId("ds_2cd8133f_e");
			}
			else if (camera.getModel().contains("DS-2CD2032"))
			{
				return getImageId("ds_2cd2032_i");
			}
			else
			{
				return idFromVendor;
			}
		}
		else
		{
			return idFromVendor;
		}
	}

	public static String getInternalHttpURL(Camera camera)
	{
		return "http://" + camera.getUsername() + ":" + camera.getPassword()
				+ "@" + camera.getIP() + ":" + camera.getHttp() + "/";
	}

	public static String getExternalHttpURL(Camera camera)
	{
		return "http://" + camera.getUsername() + ":" + camera.getPassword()
				+ "@" + camera.getIP() + ":" + camera.getExthttp() + "/";
	}

}

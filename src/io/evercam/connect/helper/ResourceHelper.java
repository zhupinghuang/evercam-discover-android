package io.evercam.connect.helper;

import io.evercam.connect.db.Camera;

public class ResourceHelper
{
	public static String getExternalHttpURL(Camera camera)
	{
		return Constants.PREFIX_HTTP + camera.getUsername() + ":" + camera.getPassword() + "@"
				+ camera.getIP() + ":" + camera.getExthttp();
	}

	public static String getInternalHttpURL(Camera camera)
	{
		return Constants.PREFIX_HTTP + camera.getUsername() + ":" + camera.getPassword() + "@"
				+ camera.getIP() + ":" + camera.getHttp();
	}

	public static String getExternalFullRtspURL(Camera camera)
	{
		return Constants.PREFIX_RTSP + camera.getUsername() + ":" + camera.getPassword() + "@"
				+ camera.getIP() + ":" + camera.getExtrtsp() + camera.getH264();
	}

	public static String getInternalFullRtspURL(Camera camera)
	{
		return Constants.PREFIX_RTSP + camera.getUsername() + ":" + camera.getPassword() + "@"
				+ camera.getIP() + ":" + camera.getRtsp() + camera.getH264();
	}
}

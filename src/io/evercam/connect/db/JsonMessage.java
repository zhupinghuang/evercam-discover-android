package io.evercam.connect.db;

import io.evercam.connect.net.NetInfo;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * JsonMessage
 * 
 * Convert local camera objects to JSON for data collection.
 */

public class JsonMessage
{

	public String getAllDataJsonMsg(ArrayList<Camera> cameraList, String name,
			String email, NetInfo netInfo)
	{
		JSONObject userDataObject = new JSONObject();
		JSONArray cameraArray = getJsonArrayFromCameras(cameraList);
		JSONObject userInfoObject = new JSONObject();
		JSONObject netInfoObject = new JSONObject();
		try
		{
			userInfoObject.put("name", name);
			userInfoObject.put("email", email);

			netInfoObject.put("external ip", NetInfo.getExternalIP());
			netInfoObject.put("internal ip", netInfo.getLocalIp());
			netInfoObject.put("network interface", netInfo.getInterfaceName());
			netInfoObject.put("mac address", netInfo.getMacAddress());
			netInfoObject.put("subnet mask", netInfo.getNetmaskIp());
			netInfoObject.put("ssid", netInfo.getSsid());

			userDataObject.put("user", userInfoObject);
			userDataObject.put("device", cameraArray);
			userDataObject.put("network", netInfoObject);

		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		try
		{
			return userDataObject.toString(1);
		}
		catch (JSONException e)
		{
			return userDataObject.toString();
		}
	}

	private JSONArray getJsonArrayFromCameras(ArrayList<Camera> cameraList)
	{
		JSONArray cameraArray = new JSONArray();
		Iterator<Camera> iterator = cameraList.iterator();
		while (iterator.hasNext())
		{
			Camera camera = iterator.next();
			JSONObject cameraObject = getJsonObjectFromCamera(camera);
			cameraArray.put(cameraObject);
		}
		return cameraArray;
	}

	private JSONObject getJsonObjectFromCamera(Camera camera)
	{
		JSONObject cameraJsonObject = new JSONObject();
		try
		{
			cameraJsonObject.put("ip", camera.getIP());
			cameraJsonObject.put("mac", camera.getMAC());
			cameraJsonObject.put("vendor", camera.getVendor());
			cameraJsonObject.put("model", camera.getModel());
			cameraJsonObject.put("upnp", camera.getUpnp());
			cameraJsonObject.put("onvif", camera.getOnvif());
			cameraJsonObject.put("bonjour", camera.getBonjour());
			cameraJsonObject.put("http", camera.getHttp());
			cameraJsonObject.put("https", camera.getHttps());
			cameraJsonObject.put("rtsp", camera.getRtsp());
			cameraJsonObject.put("ftp", camera.getFtp());
			cameraJsonObject.put("ssh", camera.getSsh());
			cameraJsonObject.put("portforwarded", camera.getPortForwarded());
			cameraJsonObject.put("evercam", camera.getEvercamConnected());
			cameraJsonObject.put("exthttp", camera.getExthttp());
			cameraJsonObject.put("exthttps", camera.getExthttps());
			cameraJsonObject.put("extrtsp", camera.getExtrtsp());
			cameraJsonObject.put("extftp", camera.getExtftp());
			cameraJsonObject.put("extssh", camera.getExtssh());
			cameraJsonObject.put("flag", camera.getFlag());
			cameraJsonObject.put("firstseen", camera.getFirstSeen());
			cameraJsonObject.put("lastseen", camera.getLastSeen());
			cameraJsonObject.put("username", camera.getUsername());
			cameraJsonObject.put("password", camera.getPassword());
			cameraJsonObject.put("ssid", camera.getSsid());
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}

		return cameraJsonObject;
	}

}

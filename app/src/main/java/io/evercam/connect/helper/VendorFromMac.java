package io.evercam.connect.helper;

import android.util.Log;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import io.evercam.EvercamException;
import io.evercam.Vendor;
import io.evercam.network.query.EvercamQuery;
import io.evercam.relocation.JSONObject;

public class VendorFromMac
{
	private static final String TAG = "evercamdiscover-VendorFromMac";
	private final String URL = "http://www.macvendorlookup.com/api/v2/";
	private final String KEY_COMPANY = "company";
	private final int CODE_OK = 200;
	private final int CODE_NO_CONTENT = 204;
	private JSONObject vendorJsonObject = null;

	public VendorFromMac(String macAddress)
	{
		try
		{
			HttpResponse<JsonNode> response = Unirest.get(URL + macAddress)
					.header("accept", "application/json").asJson();
			if (response.getCode() == CODE_OK)
			{
				vendorJsonObject = response.getBody().getArray().getJSONObject(0);
			}
		}
		catch (UnirestException e)
		{
			Log.e(TAG, e.getMessage());
		}
	}

	public String getCompany()
	{
		if (vendorJsonObject != null)
		{
            return vendorJsonObject.getString(KEY_COMPANY);
		}
		return "";
	}

	/**
	 * Query Evercam API to get camera manufacturer's name by MAC address.
	 * 
	 * @param macAddress
	 *            Full MAC address read from device.
	 * @return Short camera manufacturer's name, return empty string if camera
	 *         vendor not exists.
	 */
	public static String getCameraVendor(String macAddress)
	{
		Vendor cameraVendor = EvercamQuery.getCameraVendorByMac(macAddress);
		if(cameraVendor!= null)
		{
			try 
			{
				return cameraVendor.getId();
			} 
			catch (EvercamException e) 
			{
				Log.e(TAG, e.toString());
			}
		}
		return "";
	}
}

package io.evercam.connect.helper;

import java.util.ArrayList;
import java.util.Locale;

import io.evercam.EvercamException;
import io.evercam.Vendor;

import org.json.JSONException;
import org.json.JSONObject;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import android.util.Log;

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
		catch (JSONException e)
		{
			Log.e(TAG, e.getMessage());
		}
	}

	public String getCompany()
	{
		if (vendorJsonObject != null)
		{
			try
			{
				return vendorJsonObject.getString(KEY_COMPANY);
			}
			catch (JSONException e)
			{
				Log.e(TAG, e.getMessage());
			}
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
		String submac = macAddress.substring(0, 8).toLowerCase(Locale.UK);
		try
		{
			ArrayList<Vendor> vendorList = Vendor.getByMac(submac);
			if (vendorList.size() > 0)
			{
				Vendor vendor = vendorList.get(0);
				return vendor.getId();
			}
		}
		catch (EvercamException e)
		{
			Log.e(TAG, e.toString());
		}
		return "";
	}
}

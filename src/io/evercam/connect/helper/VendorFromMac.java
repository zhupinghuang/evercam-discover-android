package io.evercam.connect.helper;

import org.json.JSONException;
import org.json.JSONObject;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import android.util.Log;

public class VendorFromMac
{
	private final String TAG = "evercamdiscover-VendorFromMac";
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
		if(vendorJsonObject != null)
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
}

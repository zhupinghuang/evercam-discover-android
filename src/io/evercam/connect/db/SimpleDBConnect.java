package io.evercam.connect.db;

import io.evercam.connect.Constants;
import io.evercam.connect.PropertyReader;

import java.util.Locale;

import android.content.Context;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.NoSuchDomainException;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.amazonaws.services.simpledb.model.SelectResult;

public class SimpleDBConnect
{
	private AWSCredentials credentials;
	private static AmazonSimpleDBClient sdbClient;
	public String username = null;
	public String password = null;
	private Context ctxt;

	public SimpleDBConnect(Context ctxt)
	{
		this.ctxt = ctxt;
		ConnectSimpleDB();
	}

	private void ConnectSimpleDB()
	{
		String accessKey = new PropertyReader(ctxt)
				.getPropertyStr(Constants.PROPERTY_KEY_ACCESS_KEY);
		String secretKey = new PropertyReader(ctxt)
				.getPropertyStr(Constants.PROPERTY_KEY_SECRET_KEY);
		credentials = new BasicAWSCredentials(accessKey, secretKey);
		sdbClient = new AmazonSimpleDBClient(credentials);
		sdbClient.setEndpoint("sdb.eu-west-1.amazonaws.com");

	}

	public void queryDefaultPassword(String vendor)
	{

		SelectRequest selectRequest = new SelectRequest(
				"select username,password from username_password where vendor='"
						+ vendor + "'");
		selectRequest.setConsistentRead(true);
		SelectResult response = sdbClient.select(selectRequest);
		if (!response.getItems().isEmpty())
		{
			username = response.getItems().get(0).getAttributes().get(0)
					.getValue();
			password = response.getItems().get(0).getAttributes().get(1)
					.getValue();
		}

	}

	public String getVendorFromMac(String mac)
	{

		String vendor = "Unknown Vendor";
		String submac = mac.substring(0, 8).toUpperCase(Locale.UK);
		try
		{
			SelectRequest selectRequest = new SelectRequest(
					"select vendor,alias from mac_vendor where mac='" + submac
							+ "'");
			selectRequest.setConsistentRead(true);
			SelectResult response = sdbClient.select(selectRequest);
			if (!response.getItems().isEmpty())
			{
				int size = response.getItems().get(0).getAttributes().size();
				// no alias
				if (size == 1)
				{
					vendor = response.getItems().get(0).getAttributes().get(0)
							.getValue();
				}
				// has alias
				else if (size == 2)
				{
					vendor = response.getItems().get(0).getAttributes().get(1)
							.getValue();
				}
			}

		}
		catch (NoSuchDomainException e)
		{
			e.printStackTrace();
		}
		catch (AmazonClientException e)
		{
			e.printStackTrace();
		}

		return vendor;
	}

	public boolean isCameraVendor(String mac)
	{
		String submac = mac.substring(0, 8).toUpperCase(Locale.UK);

		try
		{
			SelectRequest selectRequest = new SelectRequest(
					"select vendor,alias from mac_vendor where mac='" + submac
							+ "'");
			selectRequest.setConsistentRead(true);
			SelectResult response = sdbClient.select(selectRequest);

			if (!response.getItems().isEmpty())
			{
				int size = response.getItems().get(0).getAttributes().size();
				// No alias
				if (size == 1)
				{
					return false;
				}
				// Has alias
				else if (size == 2)
				{
					return true;
				}
			}
		}
		catch (NoSuchDomainException e)
		{
			e.printStackTrace();
		}
		return false;

	}
}
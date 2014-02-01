package io.evercam.connect;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

public class PropertyReader
{
	private Context context;
	private Properties properties;
	private final String LOCAL_PROPERTY_FILE = "local.properties";

	public PropertyReader(Context context)
	{
		this.context = context;
		properties = new Properties();
		properties = getProperties(LOCAL_PROPERTY_FILE);
	}

	private Properties getProperties(String fileName)
	{
		try
		{
			AssetManager assetManager = context.getAssets();
			InputStream inputStream = assetManager.open(fileName);
			properties.load(inputStream);
		}
		catch (IOException e)
		{
			Log.e("Error", e.toString());
		}
		return properties;

	}

	public String getPropertyStr(String propertyName)
	{
		return properties.getProperty(propertyName).toString();
	}

	public boolean isPropertyExist(String key)
	{
		if (properties.containsKey(key))
		{
			return true;
		}
		return false;
	}

}
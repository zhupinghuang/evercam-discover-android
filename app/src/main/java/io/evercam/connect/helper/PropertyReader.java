package io.evercam.connect.helper;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyReader
{
    public final static String KEY_API_KEY = "ApiKey";
    public final static String KEY_API_ID = "ApiId";
    public static final String KEY_BUG_SENSE = "BugSenseCode";
    public static final String KEY_ACCESS_KEY = "AwsAccessKeyId";
    public static final String KEY_SECRET_KEY = "AwsSecretKey";
    public static final String KEY_SAMPLE_IP = "SampleCameraIp";
    public static final String KEY_SAMPLE_MAC = "SampleCameraMac";
    public static final String KEY_SAMPLE_VENDOR = "SampleCameraVendor";
    public static final String KEY_SAMPLE_MODEL = "SampleCameraModel";
    public static final String KEY_SAMPLE_USERNAME = "SampleCameraUsername";
    public static final String KEY_SAMPLE_PASSWORD = "SampleCameraPassword";
    public static final String KEY_DATA_COLLECTION = "EnableDataCollection";

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
        catch(IOException e)
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
        if(properties.containsKey(key))
        {
            return true;
        }
        return false;
    }

}
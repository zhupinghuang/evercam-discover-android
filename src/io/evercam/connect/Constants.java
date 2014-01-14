package io.evercam.connect;

public final class Constants
{
	// collect user data
	public static final boolean IsDataCollectionEnabled = true;

	// Fixed camera vendors
	public static final String VENDOR_HIKVISION = "HIKVISION";
	public static final String VENDOR_AXIS = "AXIS";
	public static final String VENDOR_UBIQUITI = "UBIQUITI";
	public static final String VENDOR_YCAM = "YCAM";
	public static final String VENDOR_TPLINK = "TPLINK";
	public static final String VENDOR_PANASONIC = "PANASONIC";

	public static final String URL_GET_EXTERNAL_ADDR = "http://api.externalip.net/ip";

	// device types
	public static final int TYPE_CAMERA = 1;
	public static final int TYPE_ROUTER = 2;
	public static final int TYPE_OTHERS = 3;

	// default UPnP port mapping description
	public static final String UPNP_HTTP_DESCRIPTION = "EverCam Connect HTTP";
	public static final String UPNP_RTSP_DESCRIPTION = "EverCam Connect RTSP";

	public static final String PROTOCOL_TCP = "TCP";
	public static final String PROTOCOL_UDP = "UDP";

	// preference
	public static final String KEY_NETWORK_INTERFACE = "listInterface_preference";
	public static final String KEY_NETWORK_INFO = "networkpage_preference";
	public static final String KEY_VERSION = "version_preference";
	public static final String KEY_CONTACT = "contact_preference";
	public static final String KEY_SHOW_CAMERA_ONLY = "checkbox_showcameraonly";
	public static final String KEY_USER_DATA = "checkbox_userdata_preference";
	public static final String KEY_LAST_SSID = "LastSSID";
	public static final String KEY_USER_EMAIL = "UserEmail";
	public static final String KEY_USER_FIRST_NAME = "UserFirstName";
	public static final String KEY_USER_LAST_NAME = "UserLastName";

	// Property keys
	public static final String PROPERTY_KEY_BUG_SENSE = "BugSenseCode";
	public static final String PROPERTY_KEY_ACCESS_KEY = "AwsAccessKeyId";
	public static final String PROPERTY_KEY_SECRET_KEY = "AwsSecretKey";
	public static final String PROPERTY_KEY_SAMPLE_IP = "SampleCameraIp";
	public static final String PROPERTY_KEY_SAMPLE_MAC = "SampleCameraMac";
	public static final String PROPERTY_KEY_SAMPLE_VENDOR = "SampleCameraVendor";
	public static final String PROPERTY_KEY_SAMPLE_MODEL = "SampleCameraModel";
	public static final String PROPERTY_KEY_DATA_COLLECTION = "EnableDataCollection";

	public static final int TYPE_SHOW_ALL = 1;
	public static final int TYPE_SHOW_CAMERA = 0;

	// mail
	public static final String TITLE_SETCAMERA = "EvercamConnectReport: Set as a CAMERA";
	public static final String TITLE_SETDEVICE = "EvercamConnectReport: Set as a DEVICE";

}

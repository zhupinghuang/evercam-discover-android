package io.evercam.connect.helper;

public final class Constants
{
	// collect user data
	public static final boolean IsDataCollectionEnabled = true;

	public static final String URL_GET_EXTERNAL_ADDR = "http://ipinfo.io/ip";

	// device types
	public static final int TYPE_CAMERA = 1;
	public static final int TYPE_ROUTER = 2;
	public static final int TYPE_OTHERS = 3;

	// default UPnP port mapping description
	public static final String UPNP_HTTP_DESCRIPTION = "Evercam Connect HTTP";
	public static final String UPNP_RTSP_DESCRIPTION = "Evercam Connect RTSP";

	public static final String PROTOCOL_TCP = "TCP";
	public static final String PROTOCOL_UDP = "UDP";

	// preference
	public static final String KEY_NETWORK_INTERFACE = "listInterface_preference";
	public static final String KEY_ACCOUNT = "accountname_preference";
	public static final String KEY_NETWORK_INFO = "networkpage_preference";
	public static final String KEY_VERSION = "version_preference";
	public static final String KEY_CONTACT = "contact_preference";
	public static final String KEY_SHOW_CAMERA_ONLY = "checkbox_showcameraonly";
	public static final String KEY_USER_DATA = "checkbox_userdata_preference";
	public static final String KEY_LAST_SSID = "LastSSID";
	public static final String KEY_USER_EMAIL = "UserEmail";
	public static final String KEY_USER_FIRST_NAME = "UserFirstName";
	public static final String KEY_USER_LAST_NAME = "UserLastName";

	public static final String EVERCAM_USERNAME = "EvercamUserName";
	public static final String EVERCAM_PASSWORD = "EvercamPassword";
	public static final String EVERCAM_FORENAME = "EvercamForename";
	public static final String EVERCAM_LASTNAME = "EvercamLastname";
	public static final String EVERCAM_COUNTRY = "EvercamCountry";
	public static final String EVERCAM_EMAIL = "EvercamEmail";

	public static final int TYPE_SHOW_ALL = 1;
	public static final int TYPE_SHOW_CAMERA = 0;

	// mail
	public static final String TITLE_SETCAMERA = "EvercamConnectFeedback: This is a CAMERA";
	public static final String TITLE_SETDEVICE = "EvercamConnectFeedback: This is NOT a camera";

	// Prefixes
	public static final String PREFIX_HTTP = "http://";
	public static final String PREFIX_RTSP = "rtsp://";
	
	// Bundle keys
	public static final String BUNDLE_KEY_IP = "IP";
	public static final String BUNDLE_KEY_SSID = "SSID";
	public static final String SAMPLE = "sample";

}

package io.evercam.connect.helper;

import android.content.SharedPreferences;

public class SharedPrefsManager
{
	public static void clearGoogleUserInfo(SharedPreferences sharedPrefs)
	{
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putString(Constants.KEY_USER_EMAIL, null);
		editor.putString(Constants.KEY_USER_FIRST_NAME, null);
		editor.putString(Constants.KEY_USER_LAST_NAME, null);
		editor.commit();
	}
	
	public static void clearEvercamUserInfo(SharedPreferences sharedPrefs)
	{
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putString(Constants.EVERCAM_USERNAME, null);
		editor.putString(Constants.EVERCAM_PASSWORD, null);
		editor.commit();
	}
	
	public static void clearAllUserInfo(SharedPreferences sharedPrefs)
	{
		clearGoogleUserInfo(sharedPrefs);
		clearEvercamUserInfo(sharedPrefs);
	}

	public static void saveEvercamCredential(SharedPreferences sharedPrefs,
			String username, String password)
	{
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putString(Constants.EVERCAM_USERNAME, username);
		editor.putString(Constants.EVERCAM_PASSWORD, password);
		editor.commit();
	}

	public static boolean isSignedWithGoogle(SharedPreferences sharedPrefs)
	{
		if (sharedPrefs.getString(Constants.KEY_USER_EMAIL, null) != null)
		{
			return true;
		}
		return false;
	}

	public static boolean isSignedWithEvercam(SharedPreferences sharedPrefs)
	{
		if (sharedPrefs.getString(Constants.EVERCAM_USERNAME, null) != null)
		{
			return true;
		}
		return false;
	}

	public static boolean isSigned(SharedPreferences sharedPrefs)
	{
		if (isSignedWithGoogle(sharedPrefs) || isSignedWithEvercam(sharedPrefs))
		{
			return true;
		}
		return false;
	}
	
	public static String[] getGoogle(SharedPreferences sharedPrefs)
	{
		return new String[] {sharedPrefs.getString(Constants.KEY_USER_EMAIL, null),sharedPrefs.getString(Constants.KEY_USER_FIRST_NAME, null),sharedPrefs.getString(Constants.KEY_USER_LAST_NAME, null)};
	}
	
	public static String[] getEvercam(SharedPreferences sharedPrefs)
	{
		return new String[] {sharedPrefs.getString(Constants.EVERCAM_USERNAME, null),sharedPrefs.getString(Constants.EVERCAM_PASSWORD, null)};
	}
}

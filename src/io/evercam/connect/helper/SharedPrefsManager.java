package io.evercam.connect.helper;

import io.evercam.EvercamException;
import io.evercam.User;
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

	public static void saveEvercamCredential(SharedPreferences sharedPrefs,User user,String password) throws EvercamException
	{
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putString(Constants.EVERCAM_USERNAME, user.getId());
		editor.putString(Constants.EVERCAM_PASSWORD, password);
		editor.putString(Constants.EVERCAM_COUNTRY, user.getCountry());
		editor.putString(Constants.EVERCAM_EMAIL, user.getEmail());
		editor.putString(Constants.EVERCAM_FORENAME, user.getForename());
		editor.putString(Constants.EVERCAM_LASTNAME, user.getLastname());
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
	
	public static String getEvercamUsername(SharedPreferences sharedPrefs)
	{
		return sharedPrefs.getString(Constants.EVERCAM_USERNAME, null);
	}
	
	public static String getEvercamPassword(SharedPreferences sharedPrefs)
	{
		return sharedPrefs.getString(Constants.EVERCAM_PASSWORD, null);
	}
	
}

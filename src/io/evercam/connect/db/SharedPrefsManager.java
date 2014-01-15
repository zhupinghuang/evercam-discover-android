package io.evercam.connect.db;

import io.evercam.connect.Constants;
import android.content.SharedPreferences;

public class SharedPrefsManager
{
	public static void clearUserInfo(SharedPreferences sharedPrefs)
	{
		SharedPreferences.Editor editor = sharedPrefs
				.edit();
		editor.putString(Constants.KEY_USER_EMAIL,
				null);
		editor.putString(
				Constants.KEY_USER_FIRST_NAME, null);
		editor.putString(
				Constants.KEY_USER_LAST_NAME, null);
		editor.commit();
	}

}

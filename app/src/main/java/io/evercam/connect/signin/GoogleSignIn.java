package io.evercam.connect.signin;

import io.evercam.connect.helper.Constants;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.plus.PlusClient;

public class GoogleSignIn implements ConnectionCallbacks,OnConnectionFailedListener
{

	protected static final int REQUEST_CODE_RESOLVE_ERR = 9000;
	LoginActivity loginActivity;

	GoogleSignIn(LoginActivity loginActivity)
	{
		this.loginActivity = loginActivity;
		loginActivity.mPlusClient = new PlusClient.Builder(loginActivity, this, this).setActions(
				"http://schemas.google.com/AddActivity", "http://schemas.google.com/BuyActivity")
				.build();

		loginActivity.mConnectionProgressDialog = new ProgressDialog(loginActivity);
		loginActivity.mConnectionProgressDialog.setMessage("Signing in...");

		loginActivity.mPlusClient.connect();

	}

	@Override
	public void onConnected(Bundle connectionHint)
	{

		loginActivity.mConnectionProgressDialog.dismiss();
		Toast.makeText(loginActivity,
				"Hi, " + loginActivity.mPlusClient.getCurrentPerson().getDisplayName() + " :)",
				Toast.LENGTH_LONG).show();

		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(loginActivity);
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putString(Constants.KEY_USER_EMAIL, loginActivity.mPlusClient.getAccountName());
		editor.putString(Constants.KEY_USER_FIRST_NAME, loginActivity.mPlusClient
				.getCurrentPerson().getDisplayName().split(" ")[0]);
		editor.putString(Constants.KEY_USER_LAST_NAME, loginActivity.mPlusClient.getCurrentPerson()
				.getDisplayName().split(" ")[1]);
		editor.commit();

		loginActivity.launchConfirmPage();
	}

	@Override
	public void onDisconnected()
	{

	}

	@Override
	public void onConnectionFailed(ConnectionResult result)
	{

		// The user clicked the sign-in button already. Start to resolve
		// connection errors. Wait until onConnected() to dismiss the
		// connection dialog.
		if (result.hasResolution())
		{
			try
			{
				result.startResolutionForResult(loginActivity, REQUEST_CODE_RESOLVE_ERR);
			}
			catch (SendIntentException e)
			{
				loginActivity.mPlusClient.disconnect();
				loginActivity.mPlusClient.connect();
			}
		}
		// Save the result and resolve the connection failure upon a user
		// click.
		loginActivity.mConnectionResult = result;
		Log.v("Error", "Connection failed!");
	}

}

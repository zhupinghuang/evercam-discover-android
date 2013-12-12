package io.evercam.connect;

import io.evercam.connect.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.plus.PlusClient;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.IntentSender.SendIntentException;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity
{

	private static final int REQUEST_CODE_RESOLVE_ERR = 9000;

	private ProgressDialog mConnectionProgressDialog;
	private PlusClient mPlusClient;
	private ConnectionResult mConnectionResult;
	SignInButton signInButton;
	TextView alreadySigned;
	LinearLayout loginLayout;
	LinearLayout confirmLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayHomeAsUpEnabled(true);

		loginLayout = (LinearLayout) findViewById(R.id.login_layout);
		confirmLayout = (LinearLayout) findViewById(R.id.confirm_layout);

		TextView signUpLink = (TextView) findViewById(R.id.signupLink);
		SpannableString spanString = new SpannableString(this.getResources()
				.getString(R.string.signUpForEvercam));
		spanString.setSpan(new UnderlineSpan(), 0, spanString.length(), 0);
		signUpLink.setText(spanString);

		signUpLink.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v)
			{
				Toast.makeText(LoginActivity.this, "Coming Soon...",
						Toast.LENGTH_SHORT).show();
			}

		});

		signInButton = (SignInButton) findViewById(R.id.sign_in_button);
		signInButton.setStyle(SignInButton.SIZE_WIDE, SignInButton.COLOR_LIGHT);

		/*
		 * check google plus application available or not in device
		 */
		int errorCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(getApplicationContext());
		if (errorCode != ConnectionResult.SUCCESS)
		{
			GooglePlayServicesUtil.getErrorDialog(errorCode, this, 0).show();
			Toast.makeText(this, "Google+ is not installed!", Toast.LENGTH_LONG)
					.show();
		}
		else
		{

			signInButton.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v)
				{

					new GoogleSignIn(LoginActivity.this);
					if (!mPlusClient.isConnected())
					{
						if (mConnectionResult == null)
						{
							mConnectionProgressDialog.show();

						}
						else
						{
							try
							{
								mConnectionResult.startResolutionForResult(
										LoginActivity.this,
										REQUEST_CODE_RESOLVE_ERR);
							}
							catch (SendIntentException e)
							{
								// Try connecting again.
								mConnectionResult = null;
								mPlusClient.connect();

							}
						}

					}

				}

			});
		}
	}

	protected void launchConfirmPage()
	{

		loginLayout.setVisibility(View.GONE);
		confirmLayout.setVisibility(View.VISIBLE);

		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(LoginActivity.this);
		String email = sharedPrefs.getString(Constants.KEY_USER_EMAIL, null);
		String firstName = sharedPrefs.getString(Constants.KEY_USER_FIRST_NAME, null);
		String lastName = sharedPrefs.getString(Constants.KEY_USER_LAST_NAME, null);

		EditText firstNameEditTxt = (EditText) findViewById(R.id.signUpFirstnamevalue_detail);
		EditText lastNameEditTxt = (EditText) findViewById(R.id.signUpLastnamevalue_detail);
		EditText emailEditTxt = (EditText) findViewById(R.id.signUpEmailvalue_detail);
		EditText countryEditTxt = (EditText) findViewById(R.id.signUpCountryvalue_detail);
		Button nextBtn = (Button) findViewById(R.id.button_next);

		firstNameEditTxt.setText(firstName);
		lastNameEditTxt.setText(lastName);
		emailEditTxt.setText(email);

		nextBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v)
			{
				LoginActivity.this.finish();
			}
		});
	}

	@Override
	protected void onStart()
	{
		super.onStart();

		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(LoginActivity.this);
		if (sharedPrefs.contains("Logged"))
		{

		}
		else
		{

			new GoogleSignIn(LoginActivity.this);
			if (!mPlusClient.isConnected())
			{
				if (mConnectionResult == null)
				{

				}
				else
				{
					try
					{
						mConnectionResult.startResolutionForResult(
								LoginActivity.this, REQUEST_CODE_RESOLVE_ERR);
					}
					catch (SendIntentException e)
					{
						// Try connecting again.
						mConnectionResult = null;
						mPlusClient.connect();

					}
				}
			}

			SharedPreferences.Editor editor = sharedPrefs.edit();

			editor.putInt("Logged", 0);
			editor.commit();
		}
	}

	@Override
	public Intent getParentActivityIntent()
	{
		this.finish();
		return super.getParentActivityIntent();

	}

	private class GoogleSignIn implements ConnectionCallbacks,
			OnConnectionFailedListener
	{
		LoginActivity loginActivity;

		GoogleSignIn(LoginActivity loginActivity)
		{
			this.loginActivity = loginActivity;
			mPlusClient = new PlusClient.Builder(getApplicationContext(), this,
					this).setActions("http://schemas.google.com/AddActivity",
					"http://schemas.google.com/BuyActivity").build();

			mConnectionProgressDialog = new ProgressDialog(loginActivity);
			mConnectionProgressDialog.setMessage("Signing in...");

			mPlusClient.connect();

		}

		@Override
		public void onConnected(Bundle connectionHint)
		{

			mConnectionProgressDialog.dismiss();
			Toast.makeText(
					loginActivity,
					"Hi, " + mPlusClient.getCurrentPerson().getDisplayName()
							+ " :)", Toast.LENGTH_LONG).show();

			SharedPreferences sharedPrefs = PreferenceManager
					.getDefaultSharedPreferences(LoginActivity.this);
			SharedPreferences.Editor editor = sharedPrefs.edit();
			editor.putString(Constants.KEY_USER_EMAIL,
					mPlusClient.getAccountName());
			editor.putString(Constants.KEY_USER_FIRST_NAME, mPlusClient
					.getCurrentPerson().getDisplayName().split(" ")[0]);
			editor.putString(Constants.KEY_USER_LAST_NAME, mPlusClient
					.getCurrentPerson().getDisplayName().split(" ")[1]);
			editor.commit();

			launchConfirmPage();
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
					result.startResolutionForResult(loginActivity,
							REQUEST_CODE_RESOLVE_ERR);
				}
				catch (SendIntentException e)
				{
					mPlusClient.disconnect();
					mPlusClient.connect();
				}
			}
			// Save the result and resolve the connection failure upon a user
			// click.
			mConnectionResult = result;
			Log.v("Error","Connection failed!");
		}
	}

}

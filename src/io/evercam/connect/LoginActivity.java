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

	protected ProgressDialog mConnectionProgressDialog;
	protected PlusClient mPlusClient;
	protected ConnectionResult mConnectionResult;
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
										GoogleSignIn.REQUEST_CODE_RESOLVE_ERR);
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
								LoginActivity.this, GoogleSignIn.REQUEST_CODE_RESOLVE_ERR);
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
}

package io.evercam.connect.signin;

import io.evercam.API;
import io.evercam.EvercamException;
import io.evercam.User;
import io.evercam.connect.R;
import io.evercam.connect.helper.Constants;
import io.evercam.connect.helper.PropertyReader;
import io.evercam.connect.helper.SharedPrefsManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.plus.PlusClient;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.IntentSender.SendIntentException;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
import android.view.Gravity;
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
	private View loginFormView;
	private View loginStatusView;
	private EditText usernameEdit;
	private EditText passwordEdit;
	private String username;
	private String password;
	private Button btnEvercamSignIn;
	SignInButton signInButton;
	TextView alreadySigned;
	LinearLayout loginLayout;
	LinearLayout confirmLayout;
	private UserLoginTask loginTask = null;
	private SharedPreferences sharedPrefs;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
		SharedPrefsManager.clearAllUserInfo(sharedPrefs);

		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayHomeAsUpEnabled(true);

		loginLayout = (LinearLayout) findViewById(R.id.login_layout);
		confirmLayout = (LinearLayout) findViewById(R.id.confirm_layout);

		loginFormView = findViewById(R.id.login_form);
		loginStatusView = findViewById(R.id.login_status);

		usernameEdit = (EditText) findViewById(R.id.loginUsername);
		passwordEdit = (EditText) findViewById(R.id.loginPassword);

		btnEvercamSignIn = (Button) findViewById(R.id.signInEvercamBtn);
		TextView signUpLink = (TextView) findViewById(R.id.signupLink);
		SpannableString spanString = new SpannableString(this.getResources().getString(
				R.string.create_account));
		spanString.setSpan(new UnderlineSpan(), 0, spanString.length(), 0);
		signUpLink.setText(spanString);

		signUpLink.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v)
			{
				Intent signUpIntent = new Intent();
				signUpIntent.setClass(LoginActivity.this, SignUpActivity.class);
				startActivity(signUpIntent);
			}
		});

		btnEvercamSignIn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v)
			{
				attemptLogin();
			}
		});

		signInButton = (SignInButton) findViewById(R.id.sign_in_button);
		signInButton.setStyle(SignInButton.SIZE_WIDE, SignInButton.COLOR_LIGHT);

		signInButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v)
			{
				int errorCode = GooglePlayServicesUtil
						.isGooglePlayServicesAvailable(getApplicationContext());
				if (errorCode != ConnectionResult.SUCCESS)
				{
					GooglePlayServicesUtil.getErrorDialog(errorCode, LoginActivity.this, 0).show();
					Toast.makeText(LoginActivity.this, "Google+ is not installed!",
							Toast.LENGTH_LONG).show();
				}
				else
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
								mConnectionResult.startResolutionForResult(LoginActivity.this,
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
			}
		});
	}

	public void attemptLogin()
	{
		if (loginTask != null)
		{
			return;
		}

		usernameEdit.setError(null);
		passwordEdit.setError(null);

		username = usernameEdit.getText().toString();
		password = passwordEdit.getText().toString();

		boolean cancel = false;
		View focusView = null;

		if (TextUtils.isEmpty(password))
		{
			passwordEdit.setError("Password Required");
			focusView = passwordEdit;
			cancel = true;
		}

		if (TextUtils.isEmpty(username))
		{
			usernameEdit.setError("Username Required");
			focusView = usernameEdit;
			cancel = true;
		}
		else if (username.contains("@"))
		{
			usernameEdit.setError("Please sign in with your Evercam USERNAME, NOT Email address.");
			focusView = usernameEdit;
			cancel = true;
		}

		if (cancel)
		{
			focusView.requestFocus();
		}
		else
		{
			showProgress(true);
			loginTask = new UserLoginTask();
			loginTask.execute((Void) null);
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show)
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2)
		{
			int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

			loginStatusView.setVisibility(View.VISIBLE);
			loginStatusView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter(){
						@Override
						public void onAnimationEnd(Animator animation)
						{
							loginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
						}
					});

			loginFormView.setVisibility(View.VISIBLE);
			loginFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter(){
						@Override
						public void onAnimationEnd(Animator animation)
						{
							loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
						}
					});
		}
		else
		{
			loginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
	public Intent getParentActivityIntent()
	{
		this.finish();
		return super.getParentActivityIntent();

	}

	public class UserLoginTask extends AsyncTask<Void, Void, Boolean>
	{
		@Override
		protected Boolean doInBackground(Void... params)
		{
			try
			{
				PropertyReader propertyReader = new PropertyReader(getApplicationContext());
				String apiKey = propertyReader.getPropertyStr(PropertyReader.KEY_API_KEY);
				String apiID = propertyReader.getPropertyStr(PropertyReader.KEY_API_ID);
				HttpResponse<JsonNode> response = Unirest.get(API.URL + "users/" + username  + "?app_id=" + apiID + "&app_key="
						+ apiKey)
						.header("accept", "application/json").basicAuth(username, password)
						.asJson();
				if (response.getCode() == 401)
				{
					return false;
				}
				else if (response.getCode() == 200)
				{
					return true;
				}
			}
			catch (UnirestException e)
			{
				e.printStackTrace();
			}
			return false;
		}

		@Override
		protected void onPostExecute(final Boolean success)
		{
			loginTask = null;
			showProgress(false);

			if (success)
			{
				API.setAuth(username, password);

				try
				{
					User user = new User(username);
					SharedPrefsManager.saveEvercamCredential(sharedPrefs, user, password);
				}
				catch (EvercamException e)
				{
					e.printStackTrace();
				}
				Toast toast = Toast
						.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT);
				toast.show();
				finish();
			}
			else
			{
				Toast toast = Toast.makeText(getApplicationContext(), "Invalid username/password!",
						Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
				passwordEdit.setText(null);
			}
		}

		@Override
		protected void onCancelled()
		{
			loginTask = null;
			showProgress(false);
		}
	}
}

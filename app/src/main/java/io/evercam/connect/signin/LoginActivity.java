package io.evercam.connect.signin;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import io.evercam.API;
import io.evercam.ApiKeyPair;
import io.evercam.EvercamException;
import io.evercam.User;
import io.evercam.connect.EvercamDiscover;
import io.evercam.connect.R;
import io.evercam.connect.helper.Constants;
import io.evercam.connect.helper.SharedPrefsManager;

public class LoginActivity extends Activity
{
	private final String TAG = "LoginActivity";
	private View loginFormView;
	private View loginStatusView;
	private EditText usernameEdit;
	private EditText passwordEdit;
	private String username;
	private String password;
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

		EvercamDiscover.sendScreenAnalytics(this, getString(R.string.screen_login));

		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayHomeAsUpEnabled(true);

		loginLayout = (LinearLayout) findViewById(R.id.login_layout);
		confirmLayout = (LinearLayout) findViewById(R.id.confirm_layout);

		loginFormView = findViewById(R.id.login_form_relativelayout);
		loginStatusView = findViewById(R.id.login_status);

		usernameEdit = (EditText) findViewById(R.id.loginUsername);
		passwordEdit = (EditText) findViewById(R.id.loginPassword);

		Button btnEvercamSignIn = (Button) findViewById(R.id.signInEvercamBtn);
		TextView signUpLink = (TextView) findViewById(R.id.signupLink);
		SpannableString spanString = new SpannableString(this.getResources().getString(
				R.string.create_account));
		spanString.setSpan(new UnderlineSpan(), 0, spanString.length(), 0);
		signUpLink.setText(spanString);

		signUpLink.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				Intent signUpIntent = new Intent(LoginActivity.this, SignUpActivity.class);
				startActivityForResult(signUpIntent, Constants.REQUEST_CODE_SIGN_UP);
			}
		});

		btnEvercamSignIn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v)
			{
				attemptLogin();
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(requestCode == Constants.REQUEST_CODE_SIGN_UP)
		{
			if(resultCode == Constants.RESULT_TRUE)
			{
				setResult(Constants.RESULT_TRUE);
				finish();
			}
		}
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
			passwordEdit.setError(getString(R.string.error_field_required));
			focusView = passwordEdit;
			cancel = true;
		}
		else if (password.contains(" "))
		{
			passwordEdit.setError(getString(R.string.error_invalid_password));
			focusView = passwordEdit;
			cancel = true;
		}

		if (TextUtils.isEmpty(username))
		{
			usernameEdit.setError(getString(R.string.error_field_required));
			focusView = usernameEdit;
			cancel = true;
		}
		else if (username.contains(" "))
		{
			usernameEdit.setError(getString(R.string.error_invalid_username));
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

	@Override
	public Intent getParentActivityIntent()
	{
		this.finish();
		return super.getParentActivityIntent();

	}

	public class UserLoginTask extends AsyncTask<Void, Void, Boolean>
	{
		String errorMessage = "Error";

		@Override
		protected Boolean doInBackground(Void... params)
		{
			try
			{
				ApiKeyPair userKeyPair = API.requestUserKeyPairFromEvercam(username, password);
				String userApiKey = userKeyPair.getApiKey();
				String userApiId = userKeyPair.getApiId();
				SharedPrefsManager.saveEvercamUserKeyPair(sharedPrefs, userApiKey, userApiId);
				API.setUserKeyPair(userApiKey, userApiId);
				User evercamUser = new User(username);
				SharedPrefsManager.saveEvercamCredential(sharedPrefs, evercamUser, password);
				return true;
			}
			catch (EvercamException e)
			{
				errorMessage = e.getMessage();
			}
			return false;
		}

		@Override
		protected void onPostExecute(final Boolean success)
		{
			loginTask = null;

			if (success)
			{
				setResult(Constants.RESULT_TRUE);
				finish();
			}
			else
			{
				showProgress(false);
				Toast toast = Toast.makeText(getApplicationContext(), errorMessage,
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

	public static boolean isUserLogged(SharedPreferences sharedPrefs)
	{
		String savedUsername = sharedPrefs.getString(Constants.EVERCAM_USERNAME, null);
		if (savedUsername != null)
		{
			return true;
		}
		return false;
	}
}

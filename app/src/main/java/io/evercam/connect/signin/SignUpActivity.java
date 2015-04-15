package io.evercam.connect.signin;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Locale;
import java.util.Set;
import java.util.TreeMap;

import io.evercam.API;
import io.evercam.ApiKeyPair;
import io.evercam.EvercamException;
import io.evercam.User;
import io.evercam.UserDetail;
import io.evercam.connect.EvercamDiscover;
import io.evercam.connect.R;
import io.evercam.connect.helper.Constants;
import io.evercam.connect.helper.SharedPrefsManager;
import io.evercam.connect.helper.account.AccountUtils;
import io.evercam.connect.helper.account.UserProfile;

public class SignUpActivity extends Activity
{
	private final String TAG = "SignUpActivity";

	// Auto filled profiles
	private String filledFirstname = "";
	private String filledLastname = "";
	private String filledEmail = "";

	private EditText firstnameEdit;
	private EditText lastnameEdit;
	private EditText usernameEdit;
	private EditText emailEdit;
	private EditText passwordEdit;
	private EditText repasswordEdit;
	private Button signupBtn;
	private Spinner countrySpinner;
	private TreeMap<String, String> countryMap;
	private View signUpFormView;
	private View signUpStatusView;
	private CreateUserTask createUserTask;
	private View focusView = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_user);
		readFromAccount();
		initialPage();
		EvercamDiscover.sendScreenAnalytics(this, getString(R.string.screen_sign_up));
	}

	private void initialPage()
	{
		signUpFormView = findViewById(R.id.signup_form);
		signUpStatusView = findViewById(R.id.signup_status);
		firstnameEdit = (EditText) findViewById(R.id.firstname_edit);
		lastnameEdit = (EditText) findViewById(R.id.lastname_edit);
		usernameEdit = (EditText) findViewById(R.id.username_edit);
		emailEdit = (EditText) findViewById(R.id.email_edit);
		passwordEdit = (EditText) findViewById(R.id.password_edit);
		repasswordEdit = (EditText) findViewById(R.id.repassword_edit);
		signupBtn = (Button) findViewById(R.id.sign_up_button);
		countrySpinner = (Spinner) findViewById(R.id.country_spinner);

		fillDefaultProfile();

		setSpinnerAdapter();
		signupBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v)
			{
				UserDetail userDetail = checkDetails();
				if (userDetail != null)
				{
					if (createUserTask != null)
					{
						createUserTask = null;
					}
					createUserTask = new CreateUserTask(userDetail);
					createUserTask.execute();
				}
				else
				{
					if (focusView != null)
					{
						focusView.requestFocus();
					}
				}
			}
		});
	}

	private UserDetail checkDetails()
	{
		UserDetail user = new UserDetail();
		String firstname = firstnameEdit.getText().toString();
		String lastname = lastnameEdit.getText().toString();
		String username = usernameEdit.getText().toString();
		String email = emailEdit.getText().toString();
		String countryname = countrySpinner.getSelectedItem().toString();
		String password = passwordEdit.getText().toString();
		String repassword = repasswordEdit.getText().toString();

		firstnameEdit.setError(null);
		lastnameEdit.setError(null);
		usernameEdit.setError(null);
		emailEdit.setError(null);
		passwordEdit.setError(null);
		repasswordEdit.setError(null);

		if (TextUtils.isEmpty(firstname))
		{
			firstnameEdit.setError(getString(R.string.error_field_required));
			focusView = firstnameEdit;
			return null;
		}
		else
		{
			user.setFirstname(firstname);
		}

		if (TextUtils.isEmpty(lastname))
		{
			lastnameEdit.setError(getString(R.string.error_field_required));
			focusView = lastnameEdit;
			return null;
		}
		else
		{
			user.setLastname(lastname);
		}

		if (countryname.equals(getResources().getString(R.string.spinnerFistItem)))
		{
			makeShortToast(R.string.countryNotSelected);
			return null;
		}
		else
		{
			String countrycode = countryMap.get(countryname).toLowerCase(Locale.UK);
			user.setCountrycode(countrycode);
		}

		if (TextUtils.isEmpty(username))
		{
			usernameEdit.setError(getString(R.string.error_field_required));
			focusView = usernameEdit;
			return null;
		}
		else if (username.contains(" "))
		{
			usernameEdit.setError(getString(R.string.error_invalid_username));
			focusView = usernameEdit;
			return null;
		}
		else
		{
			user.setUsername(username);
		}

		if (TextUtils.isEmpty(email))
		{
			emailEdit.setError(getString(R.string.error_field_required));
			focusView = emailEdit;
			return null;
		}
		else if (!email.contains("@"))
		{
			makeShortToast(R.string.invalidEmail);
			focusView = emailEdit;
			return null;
		}
		else
		{
			user.setEmail(email);
		}

		if (TextUtils.isEmpty(password))
		{
			passwordEdit.setError(getString(R.string.error_field_required));
			focusView = passwordEdit;
			return null;
		}

		if (TextUtils.isEmpty(repassword))
		{
			repasswordEdit.setError(getString(R.string.error_field_required));
			focusView = passwordEdit;
			return null;
		}
		else if (!password.equals(repassword))
		{
			makeShortToast(R.string.passwordNotMatch);
			return null;
		}
		else
		{
			user.setPassword(password);
		}
		return user;
	}

	private void initCountryMap()
	{
		countryMap = new TreeMap<String, String>();

		for (String countryCode : Locale.getISOCountries())
		{
			Locale locale = new Locale("", countryCode);
			countryMap.put(locale.getDisplayName(), countryCode);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private String[] join(String[]... arrays)
	{
		int size = 0;
		for (String[] array : arrays)
		{
			size += array.length;
		}
		java.util.List list = new java.util.ArrayList(size);
		for (String[] array : arrays)
		{
			list.addAll(java.util.Arrays.asList(array));
		}
		return (String[]) list.toArray(new String[size]);
	}

	private void makeShortToast(int message)
	{
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

	private void makeShortToast(String message)
	{
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

	private void setSpinnerAdapter()
	{
		initCountryMap();
		Set<String> set = countryMap.keySet();
		String[] countryArray = join(
				new String[] { getResources().getString(R.string.spinnerFistItem) },
				set.toArray(new String[0]));
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(SignUpActivity.this,
				R.layout.country_spinner, countryArray);
		spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		countrySpinner.setAdapter(spinnerArrayAdapter);
	}

	public class CreateUserTask extends AsyncTask<Void, Void, String>
	{
		UserDetail userDetail;

		public CreateUserTask(UserDetail userDetail)
		{
			this.userDetail = userDetail;
		}

		@Override
		protected void onPostExecute(String message)
		{
			if (message == null)
			{
				makeShortToast(R.string.confirmSignUp);
				setResult(Constants.RESULT_TRUE);
				finish();
			}
			else
			{
				showProgress(false);
				makeShortToast(message);
			}
		}

		@Override
		protected void onPreExecute()
		{
			showProgress(true);
		}

		@Override
		protected String doInBackground(Void... args)
		{
			try
			{
				User.create(userDetail);
				ApiKeyPair userKeyPair = API.requestUserKeyPairFromEvercam(
						userDetail.getUsername(), userDetail.getPassword());
				String userApiKey = userKeyPair.getApiKey();
				String userApiId = userKeyPair.getApiId();
				SharedPreferences sharedPrefs = PreferenceManager
						.getDefaultSharedPreferences(SignUpActivity.this);
				SharedPrefsManager.saveEvercamUserKeyPair(sharedPrefs, userApiKey, userApiId);
				API.setUserKeyPair(userApiKey, userApiId);
				User evercamUser = new User(userDetail.getUsername());
				SharedPrefsManager.saveEvercamCredential(sharedPrefs, evercamUser,
						userDetail.getPassword());
				return null;
			}
			catch (EvercamException e)
			{
				return e.getMessage();
			}
		}
	}

	private void readFromAccount()
	{
		try
		{
			UserProfile profile = AccountUtils.getUserProfile(this);
			if (profile.primaryEmail() != null)
			{
				filledEmail = profile.primaryEmail();
			}
			else if (profile.possibleEmails().size() > 0)
			{
				filledEmail = profile.possibleEmails().get(0);
			}

			if (profile.possibleNames().size() > 0)
			{
				String name = profile.possibleNames().get(0);
				String[] nameArray = name.split("\\s+");
				if (nameArray.length >= 2)
				{
					filledFirstname = nameArray[0];
					filledLastname = nameArray[1];
				}
			}
			Log.d(TAG, "emails" + profile.possibleEmails().size() + "\nnames"
					+ profile.possibleNames().size() + profile.primaryEmail());
		}
		catch (Exception e)
		{
			// If exceptions happen here, will not influence app functionality.
			// Just catch it to avoid crashing.
			Log.e(TAG, e.toString());
		}
	}

	private void fillDefaultProfile()
	{
		firstnameEdit.setText(filledFirstname);
		lastnameEdit.setText(filledLastname);
		emailEdit.setText(filledEmail);
	}

	private void showProgress(boolean show)
	{
		signUpStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
		signUpFormView.setVisibility(show ? View.GONE : View.VISIBLE);
	}
}

package io.evercam.connect.signin;

import io.evercam.EvercamException;
import io.evercam.User;
import io.evercam.UserDetail;
import io.evercam.connect.R;
import java.util.Locale;
import java.util.Set;
import java.util.TreeMap;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class SignUpActivity extends Activity
{
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

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_user);

		initialPage();
	}

	private void initialPage()
	{
		signUpFormView = findViewById(R.id.signup_form);
		signUpStatusView = findViewById(R.id.signup_status);
		firstnameEdit = (EditText) findViewById(R.id.forename_edit);
		lastnameEdit = (EditText) findViewById(R.id.lastname_edit);
		usernameEdit = (EditText) findViewById(R.id.username_edit);
		emailEdit = (EditText) findViewById(R.id.email_edit);
		// passwordEdit = (EditText) findViewById(R.id.password_edit);
		// repasswordEdit = (EditText) findViewById(R.id.repassword_edit);
		signupBtn = (Button) findViewById(R.id.sign_up_button);
		countrySpinner = (Spinner) findViewById(R.id.country_spinner);

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

		firstnameEdit.setError(null);
		lastnameEdit.setError(null);
		usernameEdit.setError(null);
		emailEdit.setError(null);

		if (TextUtils.isEmpty(firstname))
		{
			firstnameEdit.setError(getString(R.string.error_field_required));
			return null;
		}
		else
		{
			user.setFirstname(firstname);
		}

		if (TextUtils.isEmpty(lastname))
		{
			lastnameEdit.setError(getString(R.string.error_field_required));
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
			return null;
		}
		else
		{
			user.setUsername(username);
		}

		if (TextUtils.isEmpty(email))
		{
			emailEdit.setError(getString(R.string.error_field_required));
			return null;
		}
		else if (!email.matches("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]"
				+ ")*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"))
		{
			makeShortToast(R.string.invalidEmail);
			return null;
		}
		else
		{
			user.setEmail(email);
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
				android.R.layout.simple_spinner_item, countryArray);
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
				showConfirmDialog();
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
				return null;
			}
			catch (EvercamException e)
			{
				return e.getMessage();
			}
		}
	}

	private void showProgress(boolean show)
	{
		signUpStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
		signUpFormView.setVisibility(show ? View.GONE : View.VISIBLE);
	}

	private void showConfirmDialog()
	{
		AlertDialog.Builder dialog = new AlertDialog.Builder(SignUpActivity.this);
		dialog.setMessage(getString(R.string.confirmSignUp));
		dialog.setCancelable(false);
		dialog.setNegativeButton(R.string.ok, new AlertDialog.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				showProgress(false);
				finish();
			}
		});
		dialog.show();
	}
}
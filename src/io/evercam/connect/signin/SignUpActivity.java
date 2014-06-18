package io.evercam.connect.signin;

import io.evercam.API;
import io.evercam.ApiKeyPair;
import io.evercam.EvercamException;
import io.evercam.User;
import io.evercam.UserDetail;
import io.evercam.connect.R;
import io.evercam.connect.helper.SharedPrefsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeMap;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;

public class SignUpActivity extends Activity
{
	private final String TAG = "evercamdiscover-SignUpActivity";
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
				showConfirmSignUp();
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

	private String readFromAccount()
	{
		Log.d(TAG, "emails" + getUserProfile(this).possibleEmails().size() + "\nnames"
				+ getUserProfile(this).possibleNames().get(0));
		return "";
	}

	/**
	 * Retrieves the user profile information.
	 * 
	 * @param context
	 *            the context from which to retrieve the user profile
	 * @return the user profile
	 */
	public static UserProfile getUserProfile(Context context)
	{
		return getUserProfileOnIcsDevice(context);

	}

	/**
	 * Interface for interacting with the result of
	 * {@link AccountUtils#getUserProfile}.
	 */
	public static class UserProfile
	{

		/**
		 * Adds an email address to the list of possible email addresses for the
		 * user
		 * 
		 * @param email
		 *            the possible email address
		 */
		public void addPossibleEmail(String email)
		{
			addPossibleEmail(email, false);
		}

		/**
		 * Adds an email address to the list of possible email addresses for the
		 * user. Retains information about whether this email address is the
		 * primary email address of the user.
		 * 
		 * @param email
		 *            the possible email address
		 * @param is_primary
		 *            whether the email address is the primary email address
		 */
		public void addPossibleEmail(String email, boolean is_primary)
		{
			if (email == null) return;
			if (is_primary)
			{
				_primary_email = email;
				_possible_emails.add(email);
			}
			else _possible_emails.add(email);
		}

		/**
		 * Adds a name to the list of possible names for the user.
		 * 
		 * @param name
		 *            the possible name
		 */
		public void addPossibleName(String name)
		{
			if (name != null) _possible_names.add(name);
		}

		/**
		 * Adds a phone number to the list of possible phone numbers for the
		 * user.
		 * 
		 * @param phone_number
		 *            the possible phone number
		 */
		public void addPossiblePhoneNumber(String phone_number)
		{
			if (phone_number != null) _possible_phone_numbers.add(phone_number);
		}

		/**
		 * Adds a phone number to the list of possible phone numbers for the
		 * user. Retains information about whether this phone number is the
		 * primary phone number of the user.
		 * 
		 * @param phone_number
		 *            the possible phone number
		 * @param is_primary
		 *            whether the phone number is teh primary phone number
		 */
		public void addPossiblePhoneNumber(String phone_number, boolean is_primary)
		{
			if (phone_number == null) return;
			if (is_primary)
			{
				_primary_phone_number = phone_number;
				_possible_phone_numbers.add(phone_number);
			}
			else _possible_phone_numbers.add(phone_number);
		}

		/**
		 * Sets the possible photo for the user.
		 * 
		 * @param photo
		 *            the possible photo
		 */
		public void addPossiblePhoto(Uri photo)
		{
			if (photo != null) _possible_photo = photo;
		}

		/**
		 * Retrieves the list of possible email addresses.
		 * 
		 * @return the list of possible email addresses
		 */
		public List<String> possibleEmails()
		{
			return _possible_emails;
		}

		/**
		 * Retrieves the list of possible names.
		 * 
		 * @return the list of possible names
		 */
		public List<String> possibleNames()
		{
			return _possible_names;
		}

		/**
		 * Retrieves the list of possible phone numbers
		 * 
		 * @return the list of possible phone numbers
		 */
		public List<String> possiblePhoneNumbers()
		{
			return _possible_phone_numbers;
		}

		/**
		 * Retrieves the possible photo.
		 * 
		 * @return the possible photo
		 */
		public Uri possiblePhoto()
		{
			return _possible_photo;
		}

		/**
		 * Retrieves the primary email address.
		 * 
		 * @return the primary email address
		 */
		public String primaryEmail()
		{
			return _primary_email;
		}

		/**
		 * Retrieves the primary phone number
		 * 
		 * @return the primary phone number
		 */
		public String primaryPhoneNumber()
		{
			return _primary_phone_number;
		}

		/** The primary email address */
		private String _primary_email;
		/** The primary name */
		private String _primary_name;
		/** The primary phone number */
		private String _primary_phone_number;
		/** A list of possible email addresses for the user */
		private List<String> _possible_emails = new ArrayList<String>();
		/** A list of possible names for the user */
		private List<String> _possible_names = new ArrayList<String>();
		/** A list of possible phone numbers for the user */
		private List<String> _possible_phone_numbers = new ArrayList<String>();
		/** A possible photo for the user */
		private Uri _possible_photo;
	}

	/**
	 * Retrieves the user profile information in a manner supported by Ice Cream
	 * Sandwich devices.
	 * 
	 * @param context
	 *            the context from which to retrieve the user's email address
	 *            and name
	 * @return a list of the possible user's email address and name
	 */
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private static UserProfile getUserProfileOnIcsDevice(Context context)
	{
		final ContentResolver content = context.getContentResolver();
		final Cursor cursor = content.query(
		// Retrieves data rows for the device user's 'profile' contact
				Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
						ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

				// Selects only email addresses or names
				ContactsContract.Contacts.Data.MIMETYPE + "=? OR "
						+ ContactsContract.Contacts.Data.MIMETYPE + "=? OR "
						+ ContactsContract.Contacts.Data.MIMETYPE + "=? OR "
						+ ContactsContract.Contacts.Data.MIMETYPE + "=?", new String[] {
						ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
						ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE,
						ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
						ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE },

				// Show primary rows first. Note that there won't be a primary
				// email address if the
				// user hasn't specified one.
				ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");

		final UserProfile user_profile = new UserProfile();
		String mime_type;
		while (cursor.moveToNext())
		{
			mime_type = cursor.getString(ProfileQuery.MIME_TYPE);
			if (mime_type.equals(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)) user_profile
					.addPossibleEmail(cursor.getString(ProfileQuery.EMAIL),
							cursor.getInt(ProfileQuery.IS_PRIMARY_EMAIL) > 0);
			else if (mime_type
					.equals(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)) user_profile
					.addPossibleName(cursor.getString(ProfileQuery.GIVEN_NAME) + " "
							+ cursor.getString(ProfileQuery.FAMILY_NAME));
			else if (mime_type.equals(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) user_profile
					.addPossiblePhoneNumber(cursor.getString(ProfileQuery.PHONE_NUMBER),
							cursor.getInt(ProfileQuery.IS_PRIMARY_PHONE_NUMBER) > 0);
			else if (mime_type.equals(ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)) user_profile
					.addPossiblePhoto(Uri.parse(cursor.getString(ProfileQuery.PHOTO)));
		}

		cursor.close();

		return user_profile;
	}

	/**
	 * Contacts user profile query interface.
	 */
	private interface ProfileQuery
	{
		/** The set of columns to extract from the profile query results */
		String[] PROJECTION = { ContactsContract.CommonDataKinds.Email.ADDRESS,
				ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
				ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
				ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
				ContactsContract.CommonDataKinds.Phone.NUMBER,
				ContactsContract.CommonDataKinds.Phone.IS_PRIMARY,
				ContactsContract.CommonDataKinds.Photo.PHOTO_URI,
				ContactsContract.Contacts.Data.MIMETYPE };

		/** Column index for the email address in the profile query results */
		int EMAIL = 0;
		/**
		 * Column index for the primary email address indicator in the profile
		 * query results
		 */
		int IS_PRIMARY_EMAIL = 1;
		/** Column index for the family name in the profile query results */
		int FAMILY_NAME = 2;
		/** Column index for the given name in the profile query results */
		int GIVEN_NAME = 3;
		/** Column index for the phone number in the profile query results */
		int PHONE_NUMBER = 4;
		/**
		 * Column index for the primary phone number in the profile query
		 * results
		 */
		int IS_PRIMARY_PHONE_NUMBER = 5;
		/** Column index for the photo in the profile query results */
		int PHOTO = 6;
		/** Column index for the MIME type in the profile query results */
		int MIME_TYPE = 7;
	}

	private void showProgress(boolean show)
	{
		signUpStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
		signUpFormView.setVisibility(show ? View.GONE : View.VISIBLE);
	}

	private void showConfirmSignUp()
	{
		makeShortToast(R.string.confirmSignUp);
		finish();
	}
}

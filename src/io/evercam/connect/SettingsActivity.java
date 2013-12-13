package io.evercam.connect;

import io.evercam.connect.net.NetInfo;
import io.evercam.connect.R;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.provider.Settings;

public class SettingsActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new PrefsFragement()).commit();

		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public Intent getParentActivityIntent()
	{
		this.finish();
		return super.getParentActivityIntent();

	}

	public static class PrefsFragement extends PreferenceFragment
	{

		NetInfo netInfo;
		ListPreference interfaceList;
		Preference netInfoPrefs;
		SharedPreferences sharedPrefs;

		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preference_xml);

			netInfo = new NetInfo(getActivity().getApplicationContext());
			sharedPrefs = PreferenceManager
					.getDefaultSharedPreferences(getActivity());

			setUpNetworkInterfacePrefs();
			setNetInfoPrefs();
			setVersionPrefs();

			interfaceList
					.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){
						@Override
						public boolean onPreferenceChange(Preference prefs,
								Object newValue)
						{
							interfaceList.setSummary(newValue.toString());
							return true;
						}
					});

			netInfoPrefs
					.setOnPreferenceClickListener(new OnPreferenceClickListener(){

						@Override
						public boolean onPreferenceClick(Preference preference)
						{
							if (netInfo.hasActiveNetwork())
							{
								Intent intent = new Intent();
								intent.setClass(getActivity(),
										RouterActivity.class);
								startActivity(intent);
							}
							else
							{
								showWifiNotConnectDialog();
							}
							return true;
						}
					});
		}

		private void setUpNetworkInterfacePrefs()
		{
			CharSequence[] charInterfaceNames = netInfo
					.getNetworkInterfaceNames();
			interfaceList = (ListPreference) getPreferenceManager()
					.findPreference(Constants.KEY_NETWORK_INTERFACE);
			interfaceList.setEntries(charInterfaceNames);
			interfaceList.setEntryValues(charInterfaceNames);

			if (charInterfaceNames.length != 0)
			{
				interfaceList.setValue(netInfo.getInterfaceName());

				interfaceList.setSummary(interfaceList.getEntry());
				interfaceList.setEnabled(true);
			}
			else
			{
				interfaceList.setEnabled(false);
				interfaceList.setSummary(R.string.noNetworkInterface);
			}
		}

		private void setNetInfoPrefs()
		{
			netInfoPrefs = getPreferenceManager().findPreference(
					Constants.KEY_NETWORK_INFO);
		}

		private void setVersionPrefs()
		{
			Preference netInfoPrefs = getPreferenceManager().findPreference(
					Constants.KEY_VERSION);
			netInfoPrefs.setSummary(getVersion());
		}
		
		private void showWifiNotConnectDialog()
		{
			AlertDialog.Builder connectDialogBuilder = new AlertDialog.Builder(
					getActivity());
			connectDialogBuilder.setMessage(R.string.dialogMsgMustConnect);

			connectDialogBuilder.setPositiveButton(R.string.wifiSettings,
					new DialogInterface.OnClickListener(){
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							startActivity(new Intent(
									Settings.ACTION_WIFI_SETTINGS));
						}
					});
			connectDialogBuilder.setNegativeButton(R.string.notNow,
					new DialogInterface.OnClickListener(){
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							return;
						}
					});
			connectDialogBuilder.setTitle(R.string.notConnected);
			connectDialogBuilder.setCancelable(false);
			connectDialogBuilder.show();
		}

		private String getVersion()
		{
			try
			{
				PackageManager manager = getActivity().getPackageManager();
				PackageInfo info = manager.getPackageInfo(getActivity()
						.getPackageName(), 0);
				String version = info.versionName;
				return version;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return getResources().getString(R.string.notAvaliable);
			}
		}

	}
}
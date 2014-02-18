package io.evercam.connect;

import io.evercam.connect.R;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

/**
 * MainTabActivity
 * 
 * Tab page, switch between manual and UPnP port forwarding page.
 */

public class MainTabActivity extends FragmentActivity
{
	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_tab);

		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.addTab(actionBar
				.newTab()
				.setText(R.string.titleManualForward)
				.setTabListener(
						new TabListener<ManuallyForwardingTab>(this, "tabtwo",
								ManuallyForwardingTab.class)));
		actionBar
				.addTab(actionBar
						.newTab()
						.setText(R.string.titleUpnpForward)
						.setTabListener(
								new TabListener<UpnpForwardingTab>(this, "tabone",
										UpnpForwardingTab.class)));

	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState)
	{
		if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM))
		{
			getActionBar().setSelectedNavigationItem(
					savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar().getSelectedNavigationIndex());
	}

	public class TabListener<T extends Fragment> implements ActionBar.TabListener
	{
		private Fragment mFragment;
		private final Activity mActivity;
		private final Class<T> mClass;
		String mTag;

		public TabListener(Activity activity, String tag, Class<T> clz)
		{
			mActivity = activity;
			mTag = tag;
			mClass = clz;
		}

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft)
		{
			mFragment = Fragment.instantiate(mActivity, mClass.getName());
			getSupportFragmentManager().beginTransaction().replace(R.id.containter, mFragment)
					.commit();
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft)
		{
		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft)
		{
		}
	}
}
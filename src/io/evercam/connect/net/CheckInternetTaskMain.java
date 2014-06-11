package io.evercam.connect.net;

import io.evercam.connect.DiscoverMainActivity;
import io.evercam.connect.R;

public class CheckInternetTaskMain extends CheckInternetTask
{
	DiscoverMainActivity mainActivity;

	public CheckInternetTaskMain(DiscoverMainActivity mainActivity)
	{
		super(mainActivity);
		this.mainActivity = mainActivity;
	}

	@Override
	protected void onPostExecute(Boolean hasNetwork)
	{
		if (hasNetwork)
		{
			mainActivity.startDiscovery();
		}
		else
		{
			mainActivity.makeToast(mainActivity.getResources().getString(
					R.string.checkInternetConnection));
		}
	}
}
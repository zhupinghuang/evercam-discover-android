package io.evercam.connect;

import android.app.Activity;
import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.HashMap;

public class EvercamDiscover extends Application
{
	private static final String PROPERTY_ID = "UA-52483995-2";

	private static final String TAG = "evercamdiscover-EvercamDiscover";

	public enum TrackerName
	{
		APP_TRACKER, // Tracker used only in this app.
		GLOBAL_TRACKER, // Tracker used by all the apps from a company.
	}

	HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

	public EvercamDiscover()
	{
		super();
	}

//	@Override
//	public void onCreate()
//	{
//        super.onCreate();
//        // Redirect URL, just for temporary testing
//        API.URL = "http://proxy.evr.cm:9292/v1/";
//	}

	synchronized Tracker getTracker(TrackerName trackerId)
	{
		if (!mTrackers.containsKey(trackerId))
		{
			GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
			Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics
					.newTracker(R.xml.app_tracker) : analytics.newTracker(PROPERTY_ID);
			mTrackers.put(trackerId, t);
		}
		return mTrackers.get(trackerId);
	}

	private static Tracker getAppTracker(Activity activity)
	{
		return ((EvercamDiscover) activity.getApplication()).getTracker(TrackerName.APP_TRACKER);
	}

	/**
	 * Send screen view to Google Analytics from activity with screen name.
	 * 
	 * @param activity
	 * @param screenName
	 *            The screen name that shows in Google dashboard.
	 */
	public static void sendScreenAnalytics(Activity activity, String screenName)
	{
		Tracker tracker = getAppTracker(activity);
		tracker.setScreenName(screenName);
		tracker.send(new HitBuilders.AppViewBuilder().build());
	}

	/**
	 * Send an event with fixed details string id.
	 */
	public static void sendEventAnalytics(Activity activity, int cateory, int action, int label)
	{
		Tracker tracker = getAppTracker(activity);
		tracker.send(new HitBuilders.EventBuilder().setCategory(activity.getString(cateory))
				.setAction(activity.getString(action)).setLabel(activity.getString(label)).build());
	}

	/**
	 * Send an event with a customed label.
	 */
	public static void sendEventAnalytics(Activity activity, int cateory, int action, String label)
	{
		Tracker tracker = getAppTracker(activity);
		tracker.send(new HitBuilders.EventBuilder().setCategory(activity.getString(cateory))
				.setAction(activity.getString(action)).setLabel(label).build());
	}
}
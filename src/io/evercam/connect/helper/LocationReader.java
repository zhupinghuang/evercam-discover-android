package io.evercam.connect.helper;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class LocationReader
{
	private final String TAG = "evercamdiscover-LocationReader";
	private LocationManager locationManager;
	private LocationListener listener;
	private Location currentLocation;

	public LocationReader(Context context)
	{
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		String provider = locationManager.getBestProvider(criteria, false);

		Location location = locationManager.getLastKnownLocation(provider);

		if (location != null)
		{
			currentLocation = location;
			// Log.d(TAG, "Provider " + provider + " has been selected.");
			// Log.d(TAG, location.getLongitude() + "   " +
			// location.getLatitude());
		}
		else
		{
			Log.d(TAG, "Previous location data not available, launch location listener");
			launchListener();
		}
	}

	public Location getLocation()
	{
		return currentLocation;
	}

	private void launchListener()
	{
		listener = new LocationListener(){
			@Override
			public void onLocationChanged(Location location)
			{
				if (currentLocation == null)
				{
					currentLocation = location;
				}
				else
				{
					locationManager.removeUpdates(this);
				}
				Log.d(TAG, location.getLongitude() + "   " + location.getLatitude());
			}

			@Override
			public void onStatusChanged(String provider, int status, Bundle extras)
			{
			}

			@Override
			public void onProviderEnabled(String provider)
			{
			}

			@Override
			public void onProviderDisabled(String provider)
			{
			}
		};
		// locationManager.requestSingleUpdate(provider, listener, null);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);
	}
}

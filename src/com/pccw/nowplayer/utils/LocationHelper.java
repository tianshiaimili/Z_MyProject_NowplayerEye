package com.pccw.nowplayer.utils;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

/**
 * A helper class of location.
 * <pre>
 * You may need permission:
 * &lt;!-- permission for GPS Location Provider and Network(cell tower and Wi-Fi based location) Location Provider. -->
 * &lt;uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
 * &lt;!-- permission for Network(cell tower and Wi-Fi based location) Location Provider. -->
 * &lt;uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
 * &lt;uses-permission android:name="android.permission.INTERNET" />
 * </pre>
 * 
 * @author AlfredZhong
 * @version 1.0, 2011-10-20
 * @version 1.1, 2012-05-04
 */
public abstract class LocationHelper {
	
	private static final String TAG = LocationHelper.class.getSimpleName();
	private Context mContext;
	private LocationManager locationManager;
	private SimpleLocationListener gpsListener;
	private SimpleLocationListener networkListener;
	
	/**
	 * GPS(Global Positioning System), Cell-ID, and Wi-Fi can each provide a clue to users location.
	 * NOTE: be aware of user movement and varying accuracy.
	 * @param context
	 */
	public LocationHelper(Context context) {
		mContext = context;
		locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
	}
	
	/**
	 * To enable GPS location provider, you should enable "Settings -- Location and security -- Use GPS satellites".
	 * @param minTime
	 * @param minDistance
	 */
	public void requestGPSLocationUpdates(long minTime, float minDistance) {
		try {
			if(gpsListener == null) {
				gpsListener = new SimpleLocationListener();
			}
			// GPS_PROVIDER, accuracy is high(5-50m), but slow to get the first location(usually 10-60s).
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, gpsListener);
			Log.w(TAG, "Set up gps provider listener.");
		} catch (Exception e) {
			Log.e(TAG, "Error while request gps location : " + e);
		}
	}
	
	/**
	 * To enable network location provider, you should enable "Settings -- Location and security -- Use wireless networks".
	 * @param minTime
	 * @param minDistance
	 */
	public void requestNetworkLocationUpdates(long minTime, float minDistance) {
		try {
			if(networkListener == null) {
				networkListener = new SimpleLocationListener();
			}
			// NETWORK_PROVIDER, accuracy is low(500-1000m), but locating fast.
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, networkListener);
			Log.w(TAG, "Set up network provider listener.");
		} catch (Exception e) {
			Log.e(TAG, "Error while request network location : " + e);
		}
	}
	
	public void removeGPSLocationUpdates() {
		if(gpsListener != null) {
			locationManager.removeUpdates(gpsListener);
			Log.w(TAG, "Remove gps provider listener.");
		}
	}
	
	public void removeNetworkLocationUpdates() {
		if(networkListener != null) {
			locationManager.removeUpdates(networkListener);
			Log.w(TAG, "Remove network provider listener.");
		}
	}
	
	/**
	 * Returns the current enabled/disabled status of the given provider. 
	 * If the user has enabled this provider in the Settings menu, true is returned otherwise false is returned
	 * 
	 * @param provider the name of the provider
	 * @return true if the provider is enabled
	 * 
	 * @throws SecurityException if no suitable permission is present for the provider.
     * @throws IllegalArgumentException if provider is null or doesn't exist
	 */
	public boolean isProviderEnabled(String provider) {
		return locationManager.isProviderEnabled(provider);
	}
	
	public abstract void onLocationChanged(Location location);
	
	protected void onStatusChanged(String provider, int status, Bundle extras) {
		// implemented by sub class.
	}
	
	protected void onProviderEnabled(String provider) {
		// implemented by sub class.
	}
	
	protected void onProviderDisabled(String provider) {
		// implemented by sub class.
	}
	
	/**
	 * @version 2012-05-04
	 */
	private class SimpleLocationListener implements LocationListener {
		
		@Override
		public void onLocationChanged(Location location) {
			Log.d(TAG, location.getProvider() + " onLocationChanged()");
			LocationHelper.this.onLocationChanged(location);
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			Log.d(TAG, "onStatusChanged " + provider);
			switch(status) {
			case LocationProvider.AVAILABLE:
				Log.w(TAG,  provider + " AVAILABLE");
				break;
			case LocationProvider.OUT_OF_SERVICE:
				Log.w(TAG, provider + " OUT_OF_SERVICE");
				break;
			case LocationProvider.TEMPORARILY_UNAVAILABLE:
				Log.w(TAG, provider + " TEMPORARILY_UNAVAILABLE");
				break;
			}
			LocationHelper.this.onStatusChanged(provider, status, extras);
		}

		@Override
		public void onProviderEnabled(String provider) {
			// Called when the provider is enabled by the user.
			Log.d(TAG, "onProviderEnabled " + provider);
			LocationHelper.this.onProviderEnabled(provider);
		}

		@Override
		public void onProviderDisabled(String provider) {
			/*
			 * Called when the provider is disabled by the user. 
			 * If requestLocationUpdates is called on an already disabled provider,
			 * this method is called immediately.
			 */
			Log.d(TAG, "onProviderDisabled " + provider);
			LocationHelper.this.onProviderDisabled(provider);
		}
		
	} // end of inner class.
	
	/**
	 * Get last known location.
	 * @return null or location from GPS_PROVIDER or NETWORK_PROVIDER.
	 */
	public Location getLastKnownLocation() {
		return getLastKnownLocation(mContext);
	}
	
	/**
	 * Get last known location.
	 * @return null or location from GPS_PROVIDER or NETWORK_PROVIDER.
	 */
	public static Location getLastKnownLocation(Context context) {
		// last location refers to its app, in other words, every app has its location(may be null if never use Location service).
		LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		Location gpsLocation = null;
		Location networkLocation = null;
		try{
			// If the provider is currently disabled, null is returned.
			gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		} catch(Exception e) {
			Log.w(TAG, "Can not get last known location from GPS_PROVIDER");
		}
		try{
			networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		} catch(Exception e) {
			Log.w(TAG, "Can not get last known location from NETWORK_PROVIDER");
		}
		if (gpsLocation == null) {
			Log.d(TAG, "GPS last location is null. Return network last location");
			return networkLocation;
		} else if(networkLocation == null) {
			Log.d(TAG, "Network last location is null. Return GPS last location");
			return gpsLocation;
		} else {
			// two last location is not null, choose a better one.
			// NOTE: here we don't care about which location is new, which is current.
			Log.d(TAG, "Both GPS and network last location are not null.");
			if(isNewLoactionBetter(gpsLocation, networkLocation)) {
				Log.d(TAG, "GPS last location is a better one.");
				return gpsLocation;
			} else {
				Log.d(TAG, "Network last location is a better one.");
				return networkLocation;
			}
		}
	}

	/** 
	 * Determines whether one Location reading is better than the current Location fix.
	 * 
	 * @param location  The new Location that you want to evaluate.
	 * @param currentBestLocation  The current Location fix, to which you want to compare the new one. 
	 */
	public static boolean isNewLoactionBetter(Location newLocation, Location currentBestLocation) {
		final int TWO_MINUTES = 1000 * 60 * 2; // 2 minutes.
		if (currentBestLocation == null) {
			// A new location is always better than no location
			return true;
		}
		// Check whether the new location fix is newer or older
		long timeDelta = newLocation.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
		boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
		boolean isNewer = timeDelta > 0;
		Log.d(TAG, "isNewLoactionBetter timeDelta is " + timeDelta);
		// If it's been more than two minutes since the current location, use the new location, because the user has likely moved
		if (isSignificantlyNewer) {
			// If the new location is more than two minutes older, it must be worse
			return true; 
		} else if (isSignificantlyOlder) {
			return false;
		}
		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (newLocation.getAccuracy() - currentBestLocation.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;
		Log.d(TAG, "isNewLoactionBetter accuracyDelta is " + accuracyDelta);
		// Check if the old and new location are from the same provider
		boolean isFromSameProvider;
		if(newLocation.getProvider() == null) {
			isFromSameProvider = (currentBestLocation.getProvider() == null);
		} else {
			isFromSameProvider = (newLocation.getProvider().equals(currentBestLocation.getProvider()));
		}
		Log.d(TAG, "isNewLoactionBetter isFromSameProvider is " + isFromSameProvider);
		// Determine location quality using a combination of timeliness and accuracy
		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
			return true;
		}
		return false;
	}
	
	/**
	 * Whether the given location is in the timeDelta to now in the UTC time.
	 * @param location location which to check.
	 * @param timeDelta milliseconds of the tiemDelta.
	 *         For example, if you think location fix in 2 minutes is new enough, set timeDelta 120000.
	 * @return true if the location is new enough.
	 */
	public static boolean isLocationNewEnough(Location location, int timeDelta) {
		long delta = System.currentTimeMillis() - location.getTime();
		if(Math.abs(delta) <= timeDelta) {
			return true;
		}
		return false;
	}
	
	// added on 2013-06-08
	public static interface LocatingOnceCallback {
		
		public void onLocated(boolean success, Location location);
		
	}
	
	// added on 2013-06-08
	private static class LocatingOnceTimeoutTask implements Runnable {

		private LocationHelper locationHelper;
		
		public void hookLocationHelper(LocationHelper helper) {
			locationHelper = helper;
		}
		
		public void removeUpdates() {
			if(locationHelper != null) {
				locationHelper.removeGPSLocationUpdates();
				locationHelper.removeNetworkLocationUpdates();
			}
		}
		
		@Override
		public void run() {
			
		}
		
	}
	
	/**
	 * Locating once to get the current position asynchronously.
	 * 
	 * @param context
	 * @param gps whether use GPS provider
	 * @param network whether use network provider
	 * @param maxTimeDeltaForLastKnownLocation the max time delta in milliseconds from last known location
	 * @param handler
	 * @param timeout the timeout in milliseconds of locating once task
	 * @param callback the callback to handle locating once task result
	 */
	public static void getCurrentLocationOnce(Context context, final boolean gps, final boolean network, 
			int maxTimeDeltaForLastKnownLocation, final Handler handler, int timeout, final LocatingOnceCallback callback) {
		// added on 2013-06-08
		final LocatingOnceTimeoutTask timeoutRunnable = new LocatingOnceTimeoutTask() {
			@Override
			public void run() {
				// failed, timeout
				removeUpdates();
				callback.onLocated(false, null);
			}
		};
		final LocationHelper helper = new LocationHelper(context) {
			@Override
			public void onLocationChanged(Location location) {
				// success
				handler.removeCallbacks(timeoutRunnable);
				removeGPSLocationUpdates();
				removeNetworkLocationUpdates();
				callback.onLocated(true, location);
			}
		};
		timeoutRunnable.hookLocationHelper(helper);
		Location location = helper.getLastKnownLocation();
		if(location != null && LocationHelper.isLocationNewEnough(location, maxTimeDeltaForLastKnownLocation)) {
			// get last location, no running location listener, no need to call remove updates.
			callback.onLocated(true, location);
		} else {
			// request latest location, will call LocationHelper.onLocationChanged() callback.
			if(gps) {
				// minDistance can be 0, but minTime should be larger than 60000ms if used to update location every frequency in background service.
				helper.requestGPSLocationUpdates(0, 0);
			}
			if(network) {
				// minDistance can be 0, but minTime should be larger than 60000ms if used to update location every frequency in background service.
				helper.requestNetworkLocationUpdates(0, 0);
			}
			handler.postDelayed(timeoutRunnable, timeout);
		}
	}
	
}
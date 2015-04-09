package com.ngstudio.friendstep.components;

import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.ngstudio.friendstep.WhereAreYouApplication;
import com.ngstudio.friendstep.model.connectivity.BaseResponseCallback;
import com.ngstudio.friendstep.model.connectivity.HttpServer;
import com.ngstudio.friendstep.model.connectivity.requests.InsertGeoCordsRequest;
import com.ngstudio.friendstep.utils.CommonUtils;
import com.ngstudio.friendstep.utils.SingletonHelper;
import com.ngstudio.friendstep.utils.WhereAreYouAppConstants;

import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class CustomLocationManager {

	private static final String TAG = CustomLocationManager.class.getSimpleName();

    public static final double LAT_LON_DEFAULT = 500;


	public static final String[] LOCATION_PROVIDERS_DESIRED = {
		LocationManager.PASSIVE_PROVIDER,
		LocationManager.NETWORK_PROVIDER,
		LocationManager.GPS_PROVIDER
	};


	private final Context context;

	private CustomLocationManager(Context context) {
		if (!(context instanceof Application)) {
			throw new Error(TAG + " should be created in Application context only!");
		}
		this.context = context;
	}


	private final Collection<String> providersAvailable
		= Collections.synchronizedCollection(new ArrayList<String>());

	@NotNull
	private static Collection<String> getProvidersAccessible(Context context) {
		String[] providersArray = new String[0];
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
			String providersAccessible = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

			if (!(providersAccessible == null || TextUtils.isEmpty(providersAccessible)))
				providersArray = providersAccessible.split(",");

			Collection<String> providers = new ArrayList<>(Arrays.asList(providersArray));
			if (providers.contains(LocationManager.NETWORK_PROVIDER)
					&& !providers.contains(LocationManager.PASSIVE_PROVIDER)) {
				if (providers.add(LocationManager.PASSIVE_PROVIDER))
					providersArray = providers.toArray(new String[providers.size()]);
			}
		} else {
			try {
				int locationModeAccessible = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

				switch (locationModeAccessible) {
					case Settings.Secure.LOCATION_MODE_OFF:
						break;

					case Settings.Secure.LOCATION_MODE_SENSORS_ONLY:
						providersArray = new String[] {
							android.location.LocationManager.GPS_PROVIDER
						};
						break;

					case Settings.Secure.LOCATION_MODE_BATTERY_SAVING:
						providersArray =  new String[] {
							android.location.LocationManager.GPS_PROVIDER,
							android.location.LocationManager.PASSIVE_PROVIDER
						};
						break;

					case Settings.Secure.LOCATION_MODE_HIGH_ACCURACY:
						providersArray = new String[] {
							android.location.LocationManager.GPS_PROVIDER,
							android.location.LocationManager.NETWORK_PROVIDER,
							android.location.LocationManager.PASSIVE_PROVIDER
						};
						break;

					default:
						throw new Error("This branch is better not to be reached.");
				}

			} catch (Settings.SettingNotFoundException e) {
				e.printStackTrace();
			}
		}
		return Arrays.asList(providersArray);
	}


	private LocationManager getLocationManager() {
		return (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
	}


	public boolean hasLocationProvidersAvailable() {
		return !providersAvailable.isEmpty();
	}

	private boolean checkDesiredLocationProvidersAreAvailable(String[] providersDesired) {
		final LocationManager locationManager = getLocationManager();

		final Collection<String> providersAccessible = getProvidersAccessible(context);

		synchronized (providersAvailable) {
			providersAvailable.clear();
			for (String p : providersDesired) {
				try {
					if (locationManager.isProviderEnabled(p) && providersAccessible.contains(p)) {
						providersAvailable.add(p);
					}
				} catch (Exception e) {
					/* Nothing to do */
				}
			}
			return hasLocationProvidersAvailable();
		}
	}

	public boolean checkDesiredLocationProvidersAreAvailable() {
		return checkDesiredLocationProvidersAreAvailable(LOCATION_PROVIDERS_DESIRED);
	}


	@NotNull
	public Collection<String> getLocationProvidersAvailable() {
//        return Arrays.asList(LocationManager.GPS_PROVIDER);
		return new ArrayList<>(providersAvailable);
	}


	/**
	 * Determines whether one Location reading is better than the current Location fix
	 *
	 *  @param newLocation         The new Location that you want to evaluate
	 *  @param currentBestLocation The current Location fix, to which you want to compare the new one
	 */
	private static boolean isBetterLocation(Location newLocation, Location currentBestLocation) {
		if (newLocation == null) {
			return false;
		}

		if (currentBestLocation == null) {
			// A new location is always better than no location
			return true;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = newLocation.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > GeoService.MIN_TIME;
		boolean isSignificantlyOlder = timeDelta < - GeoService.MIN_TIME;
		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location, use the new location
		// because the user has likely moved
		if (isSignificantlyNewer) {
			return true;
			// If the new location is more than two minutes older, it must be worse
		} else if (isSignificantlyOlder) {
			return false;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (newLocation.getAccuracy() - currentBestLocation.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = CommonUtils.areEqual(newLocation.getProvider(), currentBestLocation.getProvider());

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


	private volatile Location lastLocation;

	public Location getCurrentLocation() {
		return lastLocation;
	}

	protected void updateLocation(Location newLocation) {
        if(newLocation == null)
            return;
        NotificationManager.notifyClients(WhereAreYouAppConstants.NOTIFICATION_INSERT_GEO_CORDS_SUCCESS,"update location");
		synchronized (this) {
			if (isBetterLocation(newLocation, lastLocation)) {
				lastLocation = newLocation;

                final InsertGeoCordsRequest request = new InsertGeoCordsRequest(WhereAreYouApplication.getInstance().getUuid(),lastLocation);
                HttpServer.submitToServer(request,new BaseResponseCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        NotificationManager.notifyClients(WhereAreYouAppConstants.NOTIFICATION_INSERT_GEO_CORDS_SUCCESS,result);
                    }

                    @Override
                    public void onError(Exception error) {
                        NotificationManager.notifyClients(WhereAreYouAppConstants.NOTIFICATION_INSERT_GEO_CORDS_ERROR,error);
                    }
                });
				Log.d("Location", "Location update! " + newLocation.toString());
			}
		}
	}


	private class MyLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location location) {
			updateLocation(location);
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			/* Nothing to do */
		}

		@Override
		public void onProviderEnabled(String provider) {
			/* Nothing to do */
		}

		@Override
		public void onProviderDisabled(String provider) {
			/* Nothing to do */
		}
	}


	private final Map<String, MyLocationListener> providerListenersMap = new HashMap<>();

	protected final LocationListener getLocationListener(@MagicConstant(stringValues = {
		LocationManager.PASSIVE_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER
	}) @NotNull String provider, boolean dontCreate) {

		synchronized (providerListenersMap) {
			MyLocationListener listener = providerListenersMap.get(provider);

			if (!dontCreate && listener == null) {
				providerListenersMap.put(provider,
					listener = new MyLocationListener());
			}
			return listener;
		}
	}

	private LocationListener getLocationListener(String provider) {
		return getLocationListener(provider, false);
	}


	private void unregisterListener(LocationManager lm, String provider) {
		LocationListener listener = getLocationListener(provider, true);

		if (listener != null)
			lm.removeUpdates(listener);
	}


	public void beginTrackLocation(long minTime, float minDistance, boolean passively) {
		final LocationManager lm = getLocationManager();

		if (checkDesiredLocationProvidersAreAvailable()) {
			Collection<String> providersAvailable = getLocationProvidersAvailable();

			if (lastLocation == null) {
				for (String provider : providersAvailable) {
                    Location location;
                    if(isBetterLocation(location = lm.getLastKnownLocation(provider),lastLocation)){
                        lastLocation = location;
                    }
				}
                if(lastLocation == null) {
                    lastLocation = new Location(LocationManager.PASSIVE_PROVIDER);
                    lastLocation.setLatitude(LAT_LON_DEFAULT);
                    lastLocation.setLongitude(LAT_LON_DEFAULT);
                }
			}

			if (passively && providersAvailable.contains(LocationManager.PASSIVE_PROVIDER)) {
				lm.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER,
					minTime, minDistance, getLocationListener(LocationManager.PASSIVE_PROVIDER));
			} else {
				unregisterListener(lm, LocationManager.PASSIVE_PROVIDER);

				for (String provider : providersAvailable) {
					if (LocationManager.PASSIVE_PROVIDER.equals(provider))
						continue;

					lm.requestLocationUpdates(provider, minTime, 0, getLocationListener(provider));
				}
			}
		}
	}

	public void stopTrackLocation() {
		final LocationManager lm = getLocationManager();

		for (String provider : getLocationProvidersAvailable()) {
			if (LocationManager.PASSIVE_PROVIDER.equals(provider))
				continue;

			unregisterListener(lm, provider);
		}
	}


	private static final SingletonHelper<CustomLocationManager> singleton
		= new SingletonHelper<>(CustomLocationManager.class, Context.class);

	public static void init(@NotNull Context context) {
		singleton.initialize(TAG + " has been initialized already!", context);
	}

	public static CustomLocationManager getInstance() {
		return singleton.obtain(TAG + " hasn't been initialized yet!");
	}

}


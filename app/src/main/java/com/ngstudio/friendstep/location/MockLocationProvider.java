package com.ngstudio.friendstep.location;

import android.annotation.TargetApi;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;

import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;

public class MockLocationProvider {

	private static final String TAG = MockLocationProvider.class.getSimpleName();


	String providerName;
	Context context;

	public MockLocationProvider(@NotNull Context context, @MagicConstant(stringValues = {
		LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER
	}) @NotNull String name) {

		this.providerName = name;
		this.context = context;

		LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		lm.addTestProvider(providerName, false, false, false, false, false,	true, true, 0, 5);
		lm.setTestProviderEnabled(providerName, true);
	}


	private static Location buildLocation(String providerName, double lat, double lon, double alt, float accuracy, long timeMillis) {
		Location mockLocation = new Location(providerName);
		mockLocation.setLatitude(lat);
		mockLocation.setLongitude(lon);
		mockLocation.setAltitude(alt);
		mockLocation.setAccuracy(accuracy);
		mockLocation.setTime(timeMillis);

		return mockLocation;
	}

	public void pushLocation(double lat, double lon, double alt, float accuracy, long timeMillis) {
		LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		lm.setTestProviderLocation(providerName, buildLocation(providerName, lat, lon, alt, accuracy, timeMillis));
	}

	@TargetApi(value = Build.VERSION_CODES.JELLY_BEAN_MR1)
	public void pushLocation(double lat, double lon, double alt, float accuracy, long timeMillis, long elapsedTimeNanos) {
		LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

		Location mockLocation = buildLocation(providerName, lat, lon, alt, accuracy, timeMillis);
		mockLocation.setElapsedRealtimeNanos(elapsedTimeNanos);

		lm.setTestProviderLocation(providerName, mockLocation);
	}

	public void pushLocation(double lat, double lon, double alt) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
			pushLocation(lat, lon, alt, 1.f, Calendar.getInstance().getTimeInMillis());
		} else {
			pushLocation(lat, lon, alt, 1.f, Calendar.getInstance().getTimeInMillis(), System.nanoTime());
		}
	}

	public void pushLocation(double lat, double lon) {
		pushLocation(lat, lon, 0);
	}


	public void shutdown() {
		LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		lm.removeTestProvider(providerName);
	}
}

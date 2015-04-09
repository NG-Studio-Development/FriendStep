package com.ngstudio.friendstep.location;


import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toRadians;

public class LocationUtils {

	protected LocationUtils() {
		/* Prevent instantiating */
	}

	public static final double EARTH_RADIUS = 6371000; // meters

	public static double shortestDistanceBetween(Location la, Location lb) {
		if (la == null || lb == null)
			return -1.;

		double[] lla = { la.getLatitude(), la.getLongitude() };
		double[] llb = { lb.getLatitude(), lb.getLongitude() };

		double a;
		{
			double dLatSin = sin(toRadians(llb[0] - lla[0]) / 2);
			double dLonSin = sin(toRadians(llb[1] - lla[1]) / 2);

			a = dLatSin * dLatSin + cos(toRadians(lla[0]))
				* cos(toRadians(llb[0])) * dLonSin * dLonSin;
		}

		return 2 * atan2(sqrt(a), sqrt(1 - a)) * EARTH_RADIUS;
	}

       /**
     * Checks whether two providers are the same
     */
    private static boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    /**
     * Checks whether any of GPS or Network providers is available
     */
    public static boolean isAnyGPSProviderEnabled(Context context) {
        final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return (manager.isProviderEnabled(LocationManager.GPS_PROVIDER) || manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
    }

}

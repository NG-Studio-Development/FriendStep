package com.ngstudio.friendstep.ui.fragments;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.Marker;
import com.ngstudio.friendstep.R;
import com.ngstudio.friendstep.ui.activities.BaseActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public abstract class BaseMapFragment<ActivityClass extends BaseActivity> extends BaseFragment<ActivityClass> implements GoogleMap.OnMapLoadedCallback {

    private MapView mapView;
    private GoogleMap map;

    private double minLat = 0;
    private double maxLat = 0;
    private double minLong = 0;
    private double maxLong = 0;
    private int countMarker = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mapView = (MapView) view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        MapsInitializer.initialize(getActivity());

        map = mapView.getMap();
        if (map == null)
            throw new Error("Map is null");
        map.getUiSettings().setZoomControlsEnabled(false);
        return view;

    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onMapLoaded() { /* Options */ }

    public GoogleMap getMap() { return map; }

    protected Marker updateLocations(LatLng latLng, String title, String text) {
        minLat = countMarker>0?Math.min(latLng.latitude,minLat) : latLng.latitude;
        minLong = countMarker>0?Math.min(latLng.longitude,minLong) : latLng.longitude;
        maxLat = countMarker>0?Math.max(latLng.latitude, maxLat) : latLng.latitude;
        maxLong = countMarker>0?Math.max(latLng.longitude, maxLong) : latLng.longitude;
        countMarker++;

        return map.addMarker(new MarkerOptions()
                .position(latLng)
                .title(title)
                .snippet(text));
    }

    protected void changingCameraPosition(LatLng latLng) {
        map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    protected void zoomIn(LatLng latLng,float zoom) {
        map.animateCamera(CameraUpdateFactory.zoomTo(zoom));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    protected LatLng getOptimalCameraPosition() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(new LatLng(minLat,minLong));
        builder.include(new LatLng(maxLat,maxLong));
        LatLngBounds tmpBounds = builder.build();

        return tmpBounds.getCenter();
    }

    protected void calibrateCamera() {
        /*(LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(new LatLng(minLat,minLong));
        builder.include(new LatLng(maxLat,maxLong));
        LatLngBounds tmpBounds = builder.build();*/
        map.moveCamera(CameraUpdateFactory.newLatLng(getOptimalCameraPosition()));
    }

    /*protected Location getLocation() {
        return CustomLocationManager.getInstance().getCurrentLocation();
    }*/

    protected Location getLastKnownLocation() {
        LocationManager mLocationManager = (LocationManager) getHostActivity().getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = mLocationManager.getLastKnownLocation(provider);

            if (l == null) {
                continue;
            }
            if (bestLocation == null
                    || l.getAccuracy() < bestLocation.getAccuracy()) {
                bestLocation = l;
            }
        }
        return bestLocation;
    }
}

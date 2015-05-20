package com.ngstudio.friendstep.ui.fragments;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.View;

import com.ngstudio.friendstep.R;
import com.ngstudio.friendstep.ui.activities.MapForPushActivity;
import com.ngstudio.friendstep.ui.activities.ProfileActivity;


import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;

import org.jetbrains.annotations.NotNull;

public class MapProfileFragment  extends BaseMapFragment<ProfileActivity> implements LocationListener {

    private MapView mapView;
    private GoogleMap map;
    //private ImageButton ibButtonSendLocation;

    @Override
    public int getLayoutResID() {
        return R.layout.fragment_map;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        LatLng latLng = new LatLng(getArguments().getDouble(MapForPushActivity.CONTACT_LOCATION_LATITUDE),
                                   getArguments().getDouble(MapForPushActivity.CONTACT_LOCATION_LONGITUDE));
        updateLocations(latLng,
                        getArguments().getString(MapForPushActivity.CONTACT_USER_NAME),
                        getArguments().getString(MapForPushActivity.CONTACT_MOBILE_NUMBER));
        changingCameraPosition(latLng);
    }

    public void findChildViews(@NotNull View view) {
        view.findViewById(R.id.ibButtonSendLocation).setVisibility(View.INVISIBLE);
        //ib.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onLocationChanged(Location location) {}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}

}


package com.ngstudio.friendstep.ui.fragments;

import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ngstudio.friendstep.R;
import com.ngstudio.friendstep.components.CustomLocationManager;
import com.ngstudio.friendstep.ui.activities.MapForPushActivity;
import com.ngstudio.friendstep.ui.dialogs.AlertDialogBase;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class IncomingRequestFragment extends BaseFragment implements LocationListener {

    private MapView mapView;
    private GoogleMap map;

    private String alert;
    private String sound;
    private String category;
    private String type;
    private String name;
    private String mobilenumber;
    private double latitude;
    private double longitude;

    @Override
    public int getLayoutResID() {
        return R.layout.fragment_map;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, container, false);

        mapView = (MapView) view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        MapsInitializer.initialize(getActivity());

        map = mapView.getMap();
        if(map == null) {
            throw new Error("Map is null");
        }

        map.getUiSettings().setZoomControlsEnabled(false);

        if (getArguments() != null) {
            alert = getArguments().getString(MapForPushActivity.KEY_ALERT);
            sound = getArguments().getString(MapForPushActivity.KEY_SOUND);
            category = getArguments().getString(MapForPushActivity.KEY_CATEGORY);
            type = getArguments().getString(MapForPushActivity.KEY_TYPE);
            name = getArguments().getString(MapForPushActivity.KEY_NAME);
            mobilenumber = getArguments().getString(MapForPushActivity.KEY_MOBILENUMBER);
            latitude = getArguments().getDouble(MapForPushActivity.KEY_LATITUDE);
            longitude = getArguments().getDouble(MapForPushActivity.KEY_LONGITUDE);

            Marker marker = map.addMarker(new MarkerOptions()
                    .position(new LatLng(latitude, longitude))
                    .title(name)
                    .snippet(mobilenumber));
            marker.showInfoWindow();

            if (type.equalsIgnoreCase("LocationRequest")) {
                TextView textView = new TextView(getActivity());
                int padding = getResources().getDimensionPixelSize(R.dimen.margin_widget_default_small);
                textView.setPadding(padding,padding,padding,padding);
                textView.setText(name + ", " + mobilenumber + " has asked Where Are You, allow access?");
                AlertDialogBase dialog = new AlertDialogBase(getActivity());
                dialog.setTitle(alert);
                dialog.setCustomView(textView);
                dialog.setCancelable(false);
                dialog.addPositiveButton(R.string.text_button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                dialog.addNegativeButton(R.string.text_button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                dialog.show();
            }


        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        Location location = CustomLocationManager.getInstance().getCurrentLocation();
        updateWithNewLocation(location);
    }

    private void updateWithNewLocation(Location location) {
        if (location != null) {
            map.addMarker(new MarkerOptions()
                    .position(new LatLng(location.getLatitude(), location.getLongitude()))
                    .title(getString(R.string.title_map_position))
                    .snippet(getString(R.string.text_map_position)));
        }
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onLocationChanged(Location location) {}
}

package com.ngstudio.friendstep.ui.fragments;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.ngstudio.friendstep.R;
import com.ngstudio.friendstep.WhereAreYouApplication;
import com.ngstudio.friendstep.components.NotificationManager;
import com.ngstudio.friendstep.model.connectivity.BaseResponseCallback;
import com.ngstudio.friendstep.model.connectivity.HttpServer;
import com.ngstudio.friendstep.model.connectivity.requests.BaseContactRequest;
import com.ngstudio.friendstep.model.entity.Contact;
import com.ngstudio.friendstep.testc.MapWrapperLayout;
import com.ngstudio.friendstep.testc.OnInfoWindowElemTouchListener;
import com.ngstudio.friendstep.ui.activities.MainActivity;
import com.ngstudio.friendstep.ui.activities.MapForPushActivity;
import com.ngstudio.friendstep.utils.WhereAreYouAppConstants;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import org.jetbrains.annotations.NotNull;

public class MapRequestContact extends BaseMapFragment<MainActivity> implements LocationListener, NotificationManager.Client {
    private final String TEMP_MESSAGE = "Debug Hard Message";

    private MapView mapView;
    private GoogleMap map;
    private ImageButton ibButtonSendLocation;
    private MarkerOptions marker;
    private Button buttonYes;
    private Button buttonNo;
    private OnInfoWindowElemTouchListener infoButtonListener;
    private OnInfoWindowElemTouchListener infoButtonNoListener;
    private LatLng latLngContact;
    ViewGroup infoWindow;


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
        latLngContact = new LatLng(getArguments().getDouble(MapForPushActivity.CONTACT_LOCATION_LATITUDE),
                getArguments().getDouble(MapForPushActivity.CONTACT_LOCATION_LONGITUDE));
        //LatLng latLng = new LatLng(0.0,0.0);
        updateLocations(latLngContact,
                getArguments().getString(MapForPushActivity.CONTACT_USER_NAME),
                getArguments().getString(MapForPushActivity.CONTACT_MOBILE_NUMBER));
        //updateLocations(latLng,"","");
        //changingCameraPosition(latLng);

        marker = new MarkerOptions().position(latLngContact);
        getMap().addMarker(marker);
    }

    public void findChildViews(@NotNull View view) {
        NotificationManager.registerClient(this);
        final MapWrapperLayout mapWrapperLayout = (MapWrapperLayout)view.findViewById(R.id.map_relative_layout);
        final GoogleMap map = getMap();
        mapWrapperLayout.init(map, getPixelsFromDp(getActivity(), 39 + 20));
        this.infoWindow = (ViewGroup) getActivity().getLayoutInflater().inflate(R.layout.bubble_request_contact, null);
        this.buttonYes = (Button) infoWindow.findViewById(R.id.buttonYes);
        this.buttonNo = (Button) infoWindow.findViewById(R.id.buttonNo);

        this.infoButtonListener = new OnInfoWindowElemTouchListener(buttonYes) {
            @Override
            protected void onClickConfirmed(View v, Marker marker) {
                requestReply(latLngContact,getArguments().getString(MapForPushActivity.CONTACT_MOBILE_NUMBER),TEMP_MESSAGE);
                getActivity().onBackPressed();
            }
        };

        infoButtonNoListener = new OnInfoWindowElemTouchListener(buttonNo) {
            @Override
            protected void onClickConfirmed(View v, Marker marker) {
                Log.d("CLICK_M","no");

                getActivity().onBackPressed();
            }
        };

        this.buttonYes.setOnTouchListener(infoButtonListener);
        this.buttonNo.setOnTouchListener(infoButtonNoListener);


        map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                infoButtonNoListener.setMarker(marker);
                infoButtonListener.setMarker(marker);
                mapWrapperLayout.setMarkerWithInfoWindow(marker, infoWindow);
                return infoWindow;
            }
        });
    }

    private void requestReply(LatLng latLng,String mobile, String message) {
        BaseContactRequest replyRequest = BaseContactRequest.createLocationContactReplyRequest(WhereAreYouApplication.getInstance().getUuid(),
                                                                                                latLng.latitude,
                                                                                                latLng.longitude,
                                                                                                mobile,
                                                                                                message,
                                                                                                Contact.Status.approve.name());
        HttpServer.submitToServer(replyRequest, new BaseResponseCallback<String>() {
            @Override
            public void onSuccess(String result) {
                //getActivity().onBackPressed();
                NotificationManager.notifyClients(WhereAreYouAppConstants.NOTIFICATION_REQUEST_REPLY, result );
            }

            @Override
            public void onError(Exception error) {
                Toast.makeText(getActivity(),R.string.toast_unknown_error,Toast.LENGTH_SHORT);
            }
        });
    }

    @Override
    public void handleNotificationMessage(int what, int arg1, int arg2, Object obj) {
        if(obj == null)
            return;

        if (what == WhereAreYouAppConstants.NOTIFICATION_REQUEST_REPLY) {
            String result = (String) obj;
            getActivity().onBackPressed();

        }
    }

    @Override
    public void onDestroyView() {
        NotificationManager.unregisterClient(this);
        super.onDestroyView();
    }

    public static int getPixelsFromDp(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dp * scale + 0.5f);
    }

    @Override
    public void onLocationChanged(Location location) {}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}

    private class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private View view;

        public CustomInfoWindowAdapter() {
            view = getActivity().getLayoutInflater().inflate(R.layout.bubble_request_contact, null);
        }


        @Override
        public View getInfoContents(Marker marker) { return null; }

        @Override
        public View getInfoWindow(final Marker marker) {
            String url = null;
            return view;
        }
    }
}

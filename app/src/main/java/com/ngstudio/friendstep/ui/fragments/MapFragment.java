package com.ngstudio.friendstep.ui.fragments;

import android.content.Context;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alexutils.helpers.BitmapUtils;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.ngstudio.friendstep.FragmentPool;
import com.ngstudio.friendstep.R;
import com.ngstudio.friendstep.WhereAreYouApplication;
import com.ngstudio.friendstep.components.NotificationManager;
import com.ngstudio.friendstep.components.cache.AvatarBase64ImageDownloader;
import com.ngstudio.friendstep.model.Callback;
import com.ngstudio.friendstep.model.connectivity.BaseResponseCallback;
import com.ngstudio.friendstep.model.connectivity.HttpServer;
import com.ngstudio.friendstep.model.connectivity.requests.stepserver.ContactRequestStepServer;
import com.ngstudio.friendstep.model.connectivity.requests.stepserver.InsertGeoCordsRequestStepServer;
import com.ngstudio.friendstep.model.entity.step.ContactStep;
import com.ngstudio.friendstep.ui.activities.BaseActivity;
import com.ngstudio.friendstep.ui.activities.ChatActivity;
import com.ngstudio.friendstep.ui.activities.MainActivity;
import com.ngstudio.friendstep.ui.dialogs.AlertDialogBase;
import com.ngstudio.friendstep.utils.ContactsHelper;
import com.ngstudio.friendstep.utils.ReverseGeoLocation;
import com.ngstudio.friendstep.utils.SettingsHelper;
import com.ngstudio.friendstep.utils.WhereAreYouAppConstants;


import org.apache.http.HttpException;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapFragment extends BaseMapFragment<MainActivity> implements LocationListener,NotificationManager.Client {

    private static final float START_ZOOM = 14.0f;
    private final String SEND_LOCATION_RESULT_OK = "Location Sent";


    //private ImageButton ibButtonSendLocation;
    private FragmentPool fragmentPool = FragmentPool.getInstance();
    private Map<Marker, ContactStep> markerContact;


    @Override
    public int getLayoutResID() {
        return R.layout.fragment_map;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        markerContact = new HashMap<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentPool.popFragment(MapFragment.class);
        return super.onCreateView(inflater,container,savedInstanceState);
    }

    public void findChildViews(@NotNull View view) {
        NotificationManager.registerClient(this);

        /*ibButtonSendLocation = (ImageButton) view.findViewById(R.id.ibButtonSendLocation);
        ibButtonSendLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = LayoutInflater.from(getActivity());
                if (CommonUtils.isConnected(getActivity()))
                    showDialogSendLocation(inflater.inflate(R.layout.view_content_dialog_location, null),CustomLocationManager.getInstance().getCurrentLocation());
                else
                    Toast.makeText(getActivity(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
            }
        }); */

        getMap().setOnMapLoadedCallback(this);

        getMap().setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                ContactStep contactStep = markerContact.get(marker);
                if (contactStep != null)
                    ChatActivity.startChatActivity(getHostActivity(), contactStep);
            }
        });

        getMap().setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                View markerContentView = getHostActivity().getLayoutInflater().inflate(R.layout.item_marker_contact, null);
                ImageView ivAvatar = (ImageView) markerContentView.findViewById(R.id.ivAvatar);
                TextView tvName = (TextView) markerContentView.findViewById(R.id.tvContactsName);

                ContactStep contact = markerContact.get(marker);

                tvName.setText(contact != null ? "Friend: "+contact.getName() : "It's you");

                String uri = AvatarBase64ImageDownloader.getImageUriFor(contact != null ? contact.getName() : WhereAreYouApplication.getInstance().getUserName());
                WhereAreYouApplication.getInstance().getAvatarCache().displayImage(uri, ivAvatar);

                return markerContentView;
            }
        });

        getHostActivity().getSupportActionBar().setTitle(R.string.title_screen_map);

        if (!changeUserLocation())
            Toast.makeText(getActivity(), R.string.toast_impossible_to_obtain_location, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onMapLoaded() {
        zoomIn(getOptimalCameraPosition(), START_ZOOM);
    }


    private boolean changeUserLocation() {
        Location location = getLastKnownLocation();
        if (location == null)
            return false;

        setUserMarker(location);

        if (nearbyList != null)
            setListMarker(nearbyList);
        else
            queryNearbyContacts(getHostActivity());

        sendToServerUserLocation(location);
        return true;
    }

    private void setUserMarker(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        changingCameraPosition(latLng);
        updateLocations(latLng, getString(R.string.title_map_position), getString(R.string.text_map_position));
    }

    private void sendToServerUserLocation(Location location) {
        /*
         * Send to server user latitude and longitude
         */

        final InsertGeoCordsRequestStepServer request  = InsertGeoCordsRequestStepServer
                .requestInsertGeoCoordinates( WhereAreYouApplication.getInstance().getUserId(),
                                                                        location.getLatitude(),
                                                                        location.getLongitude(),
                                                                        SettingsHelper.getInstance().getStateSendLocation());

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpServer.sendRequestToServer(request);
                } catch (HttpException ex) {ex.printStackTrace();}
            }
        }).start();
    }

    @Override
    public void onDestroyView() {
        NotificationManager.unregisterClient(this);
        fragmentPool.free(MapFragment.class,this);
        super.onDestroyView();
    }

    public void showDialogSendLocation(View view, Location location) {

        final EditText etPhone = (EditText) view.findViewById(R.id.etMobile);
        Button buttonSend = (Button) view.findViewById(R.id.buttonSend);
        Button buttonCancel = (Button) view.findViewById(R.id.buttonCancel);
        final TextView textLocation = (TextView) view.findViewById(R.id.tvTextLocation);

        final AlertDialogBase dialogSendLocation = new AlertDialogBase(getActivity());
        dialogSendLocation.setTitle(getString(R.string.title_send_location));
        dialogSendLocation.setCustomView(view);

        dialogSendLocation.getWindow().setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialogSendLocation.getWindow().getAttributes());
        lp.width = BitmapUtils.getDisplayWidth(getActivity()) - (int) (2* getResources().getDimension(R.dimen.margin_widget_default_tiny));
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.y = (int) getResources().getDimension(R.dimen.margin_widget_default_tiny);
        dialogSendLocation.getWindow().setAttributes(lp);

        if ( location == null ) {
            Toast.makeText(getActivity(), R.string.toast_location_is_not_available, Toast.LENGTH_SHORT).show();
            return;
        }

        ReverseGeoLocation.getAddressesFromLocationBackground(location.getLatitude(), location.getLongitude(), new Callback<Collection<Address>, Exception>() {
            @Override
            public void onComplete(Collection<Address> result) {
                LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                try {
                    if ( result != null && result.iterator().hasNext() ) {
                        Address address = result.iterator().next();
                        StringBuilder builder = new StringBuilder();
                        for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                            if(i != 0)
                                builder.append(", ");
                            builder.append(address.getAddressLine(i));
                        }
                        textLocation.setText(builder.toString());
                        dialogSendLocation.show();
                    } else {
                        Toast.makeText(getActivity(),"No address", Toast.LENGTH_SHORT).show();
                    }
                } catch(Exception e) {
                    Toast.makeText(getActivity(), R.string.toast_location_is_not_available, Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Exception error) {}

            @Override
            public void anyway() {}
        });

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( etPhone.getText().toString().isEmpty() ) {
                    Toast.makeText(getActivity(),R.string.toast_wrong_phone,Toast.LENGTH_SHORT).show();
                } else {
                    PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
                    Phonenumber.PhoneNumber phoneNumber;
                    try {
                        phoneNumber = phoneNumberUtil.parse("+" + etPhone.getText().toString(),null);
                        if(!phoneNumberUtil.isPossibleNumber(phoneNumber))
                            throw new NumberParseException(NumberParseException.ErrorType.NOT_A_NUMBER,"impossible number");
                    } catch (NumberParseException e) {
                        Toast.makeText(getActivity(),R.string.toast_wrong_phone,Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                        return;
                    }
                    dialogSendLocation.dismiss();
                    queryCreateSendLocation(etPhone.getText().toString());
                }
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogSendLocation.dismiss();
            }
        });
    }

    public boolean hasConnection(final Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiInfo != null && wifiInfo.isConnected()) {
            return true;
        }
        wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifiInfo != null && wifiInfo.isConnected()) {
            return true;
        }
        wifiInfo = cm.getActiveNetworkInfo();
        if (wifiInfo != null && wifiInfo.isConnected()) {
            return true;
        }
        return false;
    }

    public void queryCreateSendLocation(String number) {
        Location location = getLastKnownLocation();
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        getHostActivity().showProgressDialog();
        ContactsHelper.getInstance().queryCreateSendLocation(latLng,WhereAreYouApplication.getInstance().getUserName(),new BaseResponseCallback<String>() {
            @Override
            public void onSuccess(String result) {
                if (result.contains(SEND_LOCATION_RESULT_OK))
                    Toast.makeText(getActivity(),R.string.toast_location_successfully_sent,Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(),R.string.toast_unknown_error,Toast.LENGTH_SHORT).show();
                getHostActivity().hideProgressDialog();
            }

            @Override
            public void onError(Exception error) {
                getHostActivity().hideProgressDialog();
                Toast.makeText(getActivity(),R.string.toast_unknown_error,Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onLocationChanged(Location location) {}

    public void queryNearbyContacts(final BaseActivity activity) {

        if (!hasConnection(activity)) {
            Toast.makeText(activity, getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
            return;
        }

        removeAllContactMarker(getLastKnownLocation());

        activity.showProgressDialog();

        ContactRequestStepServer getContacts
                = ContactRequestStepServer
                .requestGetNearbyContacts(getLastKnownLocation(), SettingsHelper
                        .getInstance()
                        .getDistance());

        HttpServer.submitToServer(getContacts, new BaseResponseCallback<String>() {
            @Override
            public void onSuccess(String result) {

                if (result != null)
                    NotificationManager.notifyClients(WhereAreYouAppConstants.NOTIFICATION_CONTACTS_NEARBY, result);
                else
                    Toast.makeText(activity, R.string.toast_location_is_not_available, Toast.LENGTH_SHORT).show();


                WhereAreYouApplication.getInstance().setFriendLoadedInMap(true);
                activity.hideProgressDialog();

            }

            @Override
            public void onError(Exception error) {
                WhereAreYouApplication.getInstance().setFriendLoadedInMap(true);
                activity.hideProgressDialog();
                Toast.makeText(activity, R.string.toast_location_is_not_available, Toast.LENGTH_SHORT).show();
            }
        });
    }




    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        menu.findItem(R.id.actionRefresh).setVisible(true);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionRefresh:
                queryNearbyContacts(getHostActivity());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setListMarker(List<ContactStep> nearbyList) {
        MapFragment.nearbyList = nearbyList;

        for (ContactStep nearbyContact : nearbyList) {
            Marker marker = updateLocations(new LatLng(nearbyContact.getLatitude(), nearbyContact.getLongitude()),null,null);
            markerContact.put(marker, nearbyContact);
        }
    }

    private void removeAllContactMarker(Location location) {
        getMap().clear();
        markerContact.clear();
        setUserMarker(location);
    }

    private static List<ContactStep> nearbyList;

    @Override
    public void handleNotificationMessage(int what, int arg1, int arg2, Object obj) {
        if(obj == null)
            return;

        if (what == WhereAreYouAppConstants.NOTIFICATION_CONTACTS_NEARBY) {
            String result = (String) obj;

            Gson gson = new Gson();
            try {
                List<ContactStep> nearbyList = gson.fromJson(result, new TypeToken<List<ContactStep>>() {
                }.getType());

                setListMarker(nearbyList);

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), R.string.toast_unknown_error, Toast.LENGTH_SHORT).show();
            }
        }
    }
}

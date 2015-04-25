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
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.alexutils.helpers.BitmapUtils;
import com.ngstudio.friendstep.FragmentPool;
import com.ngstudio.friendstep.R;
import com.ngstudio.friendstep.WhereAreYouApplication;
import com.ngstudio.friendstep.components.CustomLocationManager;
import com.ngstudio.friendstep.components.NotificationManager;
import com.ngstudio.friendstep.model.Callback;
import com.ngstudio.friendstep.model.connectivity.BaseResponseCallback;
import com.ngstudio.friendstep.model.connectivity.HttpServer;
import com.ngstudio.friendstep.model.connectivity.requests.BaseContactRequest;
import com.ngstudio.friendstep.model.entity.NearbyContact;
import com.ngstudio.friendstep.ui.activities.MainActivity;
import com.ngstudio.friendstep.ui.dialogs.AlertDialogBase;
import com.ngstudio.friendstep.utils.CommonUtils;
import com.ngstudio.friendstep.utils.ContactsHelper;
import com.ngstudio.friendstep.utils.ReverseGeoLocation;
import com.ngstudio.friendstep.utils.WhereAreYouAppConstants;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import org.jetbrains.annotations.NotNull;
import java.util.Collection;
import java.util.List;

public class MapFragment extends BaseMapFragment<MainActivity> implements LocationListener,NotificationManager.Client {

    private final float START_ZOOM = 16.0f;
    private final String SEND_LOCATION_RESULT_OK = "Location Sent";
    private final String TEMPLATE_RESULT_REQUEST = "[{\"name\":";

    private ImageButton ibButtonSendLocation;
    private FragmentPool fragmentPool = FragmentPool.getInstance();


    @Override
    public int getLayoutResID() {
        return R.layout.fragment_map;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        /* getHostActivity().getActionBarHolder().setMenuItemClickListener(R.id.ivRefresh, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( !hasConnection(getActivity()) )
                    Toast.makeText(getActivity(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                else
                    queryNearbyContacts();
            }
        }); */
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentPool.popFragment(MapFragment.class);
        return super.onCreateView(inflater,container,savedInstanceState);
    }

    public void findChildViews(@NotNull View view) {
        NotificationManager.registerClient(this);
        ibButtonSendLocation = (ImageButton) view.findViewById(R.id.ibButtonSendLocation);
        ibButtonSendLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = LayoutInflater.from(getActivity());
                if (CommonUtils.isConnected(getActivity()))
                    showDialogSendLocation(inflater.inflate(R.layout.view_content_dialog_location, null),CustomLocationManager.getInstance().getCurrentLocation());
                else
                    Toast.makeText(getActivity(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!changeUserLocation())
            Toast.makeText(getActivity(), R.string.toast_impossible_to_obtain_location, Toast.LENGTH_SHORT).show();
    }

    private boolean changeUserLocation() {
        Location location = getLastKnownLocation();
        if (location == null)
            return false;

        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        changingCameraPosition(latLng);
        updateLocations(latLng,getString(R.string.title_map_position),getString(R.string.text_map_position));
        zoomIn(latLng,START_ZOOM);
        return true;
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
        ContactsHelper.getInstance().queryCreateSendLocation(latLng,WhereAreYouApplication.getInstance().getCurrentMobile(),new BaseResponseCallback<String>() {
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

    public void queryNearbyContacts() {
        //ProgressDialogBase.getInstance(getActivity()).setContent("Title","Fish fish fish").show();
        getHostActivity().showProgressDialog();

        Location location = CustomLocationManager.getInstance().getCurrentLocation();
        BaseContactRequest getContacts = BaseContactRequest.createGetNearbyContactsRequest(WhereAreYouApplication.getInstance().getUuid(), location.getLatitude(), location.getLongitude());
        HttpServer.submitToServer(getContacts, new BaseResponseCallback<String>() {
            @Override
            public void onSuccess(String result) {

                //Test json array
                //result = "[{\"name\":\"testp-hone5\",\"latitude\":\"53.4839550\",\"longitude\":\"-2.2567090\",\"dist\":\"0\",\"loc_time\":\"1395752394\",\"mobilenumber\":\"447582178798\"}]";
                if (result != null && result.contains(TEMPLATE_RESULT_REQUEST)) {
                    NotificationManager.notifyClients(WhereAreYouAppConstants.NOTIFICATION_CONTACTS_NEARBY, result );
                } else {
                    Toast.makeText(getActivity(), R.string.toast_location_is_not_available, Toast.LENGTH_SHORT).show();
                }
                getHostActivity().hideProgressDialog();
            }
            @Override
            public void onError(Exception error) {
                //ProgressDialogBase.getInstance(getActivity()).cancel();
                getHostActivity().hideProgressDialog();
                Toast.makeText(getActivity(), R.string.toast_location_is_not_available, Toast.LENGTH_SHORT).show();
            }
        });
    }



    @Override
    public void handleNotificationMessage(int what, int arg1, int arg2, Object obj) {
        if(obj == null)
            return;

        if (what == WhereAreYouAppConstants.NOTIFICATION_CONTACTS_NEARBY) {
            String result = (String) obj;

            Gson gson = new Gson();
            try {
                List<NearbyContact> nearbyList = gson.fromJson(result, new TypeToken<List<NearbyContact>>() {
                }.getType());

                for (NearbyContact nearbyContact : nearbyList) {
                    updateLocations(new LatLng(nearbyContact.getLatitude(), nearbyContact.getLongitude()),
                            nearbyContact.getName(),
                            nearbyContact.getMobilenumber());
                }
                calibrateCamera();

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), R.string.toast_unknown_error, Toast.LENGTH_SHORT).show();
            }
        }
    }
}

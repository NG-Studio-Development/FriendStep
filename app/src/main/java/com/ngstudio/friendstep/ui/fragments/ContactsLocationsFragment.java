package com.ngstudio.friendstep.ui.fragments;


import android.os.Bundle;
import android.widget.Toast;

import com.ngstudio.friendstep.R;
import com.ngstudio.friendstep.WhereAreYouApplication;
import com.ngstudio.friendstep.components.NotificationManager;
import com.ngstudio.friendstep.model.connectivity.BaseResponseCallback;
import com.ngstudio.friendstep.model.connectivity.HttpServer;
import com.ngstudio.friendstep.model.connectivity.requests.BaseContactRequest;
import com.ngstudio.friendstep.model.entity.ContactLocation;
import com.ngstudio.friendstep.utils.WhereAreYouAppConstants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class ContactsLocationsFragment extends BaseFragment implements NotificationManager.Client {

    List<ContactLocation> contactLocations = new ArrayList<>();

    @Override
    public int getLayoutResID() {
        return R.layout.fragment_contacts_locations;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NotificationManager.registerClient(this);
        setHasOptionsMenu(true);

        BaseContactRequest getLocationsRequest = BaseContactRequest.createGetContactsLocationsRequest(WhereAreYouApplication.getInstance().getUuid());
        HttpServer.submitToServer(getLocationsRequest, new BaseResponseCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Gson gson = new Gson();
                try {
                    List<ContactLocation> contactList = gson.fromJson(result, new TypeToken<List<ContactLocation>>() {
                    }.getType());
                    NotificationManager.notifyClients(WhereAreYouAppConstants.NOTIFICATION_CONTACTS_LOADED, contactList);
                } catch (Exception e) {
                    Toast.makeText(getActivity(), R.string.toast_unknown_error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Exception error) {
                Toast.makeText(getActivity(), R.string.toast_unknown_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroy() {
        NotificationManager.unregisterClient(this);
        super.onDestroy();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handleNotificationMessage(int what, int arg1, int arg2, Object obj) {
        if(what == WhereAreYouAppConstants.NOTIFICATION_CONTACTS_LOCATIONS_LOADED) {
            List<ContactLocation> locations = (List<ContactLocation>) obj;
            contactLocations.addAll(locations);
        }
    }
}

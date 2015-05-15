package com.ngstudio.friendstep.ui.fragments;

import android.location.Location;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ngstudio.friendstep.R;
import com.ngstudio.friendstep.components.CustomLocationManager;
import com.ngstudio.friendstep.components.NotificationManager;
import com.ngstudio.friendstep.model.connectivity.BaseResponseCallback;
import com.ngstudio.friendstep.model.connectivity.HttpServer;
import com.ngstudio.friendstep.model.connectivity.requests.stepserver.ContactRequestStepServer;
import com.ngstudio.friendstep.model.entity.NearbyContact;
import com.ngstudio.friendstep.model.entity.step.ContactStep;
import com.ngstudio.friendstep.ui.activities.ChatActivity;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ContactsFragment extends BaseContactsFragment implements NotificationManager.Client {

    @Override
    public void findChildViews(@NotNull View view) {
        super.findChildViews(view);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //ProfileActivity.startProfileActivity(getActivity(),(ContactStep) parent.getItemAtPosition(position));
                ChatActivity.startChatActivity(getHostActivity(), adapter.getItem(position));
            }
        });

        buttonPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queryNearbyContacts();
            }
        });
    }


    public void queryNearbyContacts() {
        Location currentLocation = CustomLocationManager.getInstance().getCurrentLocation();
        //BaseContactRequest getContacts = BaseContactRequest.createGetNearbyContactsRequest(WhereAreYouApplication.getInstance().getUuid(), currentLocation.getLatitude(), currentLocation.getLongitude());
        ContactRequestStepServer getContacts = ContactRequestStepServer.requestGetNearbyContacts();
        HttpServer.submitToServer(getContacts, new BaseResponseCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Gson gson = new Gson();
                try {
                    List<NearbyContact> nearbyList = gson.fromJson(result, new TypeToken<List<NearbyContact>>() {
                    }.getType());
                    adapter.setCoincidenceList(nearbyList);
                    adapter.getFilter().filter(null);
                } catch (Exception e) {
                    e.printStackTrace();
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
    protected String getStatusContactFilter() {
        return ContactStep.Status.approve.name();
    }

}


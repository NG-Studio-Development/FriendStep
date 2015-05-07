package com.ngstudio.friendstep.ui.fragments;

import android.view.View;
import android.widget.AdapterView;

import com.ngstudio.friendstep.components.NotificationManager;
import com.ngstudio.friendstep.model.entity.step.ContactStep;
import com.ngstudio.friendstep.ui.activities.ChatActivity;

import org.jetbrains.annotations.NotNull;

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
    }





    @Override
    protected String getStatusContactFilter() {
        return ContactStep.Status.approve.name();
    }

}


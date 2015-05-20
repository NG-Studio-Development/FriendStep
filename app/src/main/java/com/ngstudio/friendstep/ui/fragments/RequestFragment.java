package com.ngstudio.friendstep.ui.fragments;

import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.ngstudio.friendstep.R;
import com.ngstudio.friendstep.WhereAreYouApplication;
import com.ngstudio.friendstep.components.NotificationManager;
import com.ngstudio.friendstep.model.connectivity.BaseResponseCallback;
import com.ngstudio.friendstep.model.connectivity.HttpServer;
import com.ngstudio.friendstep.model.connectivity.requests.stepserver.ContactRequestStepServer;
import com.ngstudio.friendstep.model.entity.step.ContactStep;

import org.jetbrains.annotations.NotNull;

public class RequestFragment extends BaseContactsFragment implements NotificationManager.Client {

    @Override
    public void findChildViews(@NotNull View view) {
        super.findChildViews(view);

        buttonPlus.setVisibility(View.INVISIBLE);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final ContactStep contact = adapter.getItem(position);
                ContactRequestStepServer request = ContactRequestStepServer.requestApproveCandidature(WhereAreYouApplication.getInstance().getUserId(), contact.getName());

                HttpServer.submitToServer(request, new BaseResponseCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        adapter.remove(contact);
                        Toast.makeText(getActivity(), R.string.toast_contact_was_approve, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Exception error) {
                        Toast.makeText(getActivity(), R.string.toast_unknown_error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        tvEmptyList.setText(getString(R.string.text_empty_request_list));
        getHostActivity().getSupportActionBar().setTitle(getString(R.string.title_screen_request));
    }

    @Override
    protected String getStatusContactFilter() {
        return ContactStep.Status.pending.name();
    }

}


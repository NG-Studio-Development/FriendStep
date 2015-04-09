package com.ngstudio.friendstep.ui.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ngstudio.friendstep.FragmentPool;
import com.ngstudio.friendstep.R;
import com.ngstudio.friendstep.WhereAreYouApplication;
import com.ngstudio.friendstep.components.NotificationManager;
import com.ngstudio.friendstep.model.connectivity.BaseResponseCallback;
import com.ngstudio.friendstep.model.connectivity.HttpServer;
import com.ngstudio.friendstep.model.connectivity.requests.BaseContactRequest;
import com.ngstudio.friendstep.model.entity.ContactLocation;
import com.ngstudio.friendstep.ui.activities.MainActivity;
import com.ngstudio.friendstep.ui.activities.MapForPushActivity;
import com.ngstudio.friendstep.ui.adapters.ContactsAdapter;
import com.ngstudio.friendstep.ui.widgets.ActionBarHolder;
import com.ngstudio.friendstep.utils.ContactsHelper;
import com.ngstudio.friendstep.utils.WhereAreYouAppConstants;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RequestFragment extends BaseFragment<MainActivity> implements NotificationManager.Client {

    //final String TEMP_HARD_TITLE = "Hard title";
    private String TEMPLATE_RESULT_REQUEST = "[{\"username\"";
    private String TEMPLATE_RESULT_NEARBY_REQUEST = "[{\"name\":";
    ListView listView;

    ContactsAdapter adapter;

    private FragmentPool fragmentPool = FragmentPool.getInstance();
    private ActionBarHolder actionBarHolder;

    @Override
    public int getLayoutResID() {
        return R.layout.fragment_contacts;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getHostActivity().getActionBarHolder().setTitle(R.string.title_request);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentPool.popFragment(RequestFragment.class);
        return super.onCreateView(inflater,container,savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        NotificationManager.unregisterClient(this);
        fragmentPool.free(RequestFragment.class,this);
        super.onDestroyView();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        actionBarHolder = getHostActivity().getActionBarHolder();
    }

    @Override
    public void findChildViews(@NotNull View view) {
        super.findChildViews(view);

      /*  NotificationManager.registerClient(this);
        view.findViewById(R.id.rlPanel).setVisibility(View.GONE);
        view.findViewById(R.id.buttonPlus).setVisibility(View.GONE);
        listView = (ListView) view.findViewById(R.id.listContacts);

        if(adapter == null)
            queryContacts();

        adapter = new ContactsAdapter(getActivity(), R.layout.item_contacts, ContactsHelper.getInstance().getContactsByStatus(new Pair<>(Contact.Status.pending.name(), Contact.Status.approve.name())));
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Contact contact = (Contact) parent.getItemAtPosition(position);
                BaseContactRequest replyRequest = BaseContactRequest.createLocationContactReplyRequest(WhereAreYouApplication.getInstance().getUuid(),
                        0.0,0.0,contact.getMobilenumber(),"hi","approve");
                HttpServer.submitToServer(replyRequest,null);
            }
        }); */

    }

    public void queryGetContactsLocation(String mobile) {
        BaseContactRequest getContacts = BaseContactRequest.createGetContactLocationsRequest(WhereAreYouApplication.getInstance().getUuid(), mobile);
        HttpServer.submitToServer(getContacts, new BaseResponseCallback<String>() {
            @Override
            public void onSuccess(String result) {

                //Test json array
                result = "[{\"username\":\"androidtest3\",\"mobilenumber\":\"380970754043\",\"yourstatus\":\"pending\",\"contact_status\":\"approve\",\"contactname\":\"androidtest3\",\"latitude\":\"54.9230370\",\"longitude\":\"73.4118280\",\"last_updated\":\"1393390884\"}]";
                Gson gson = new Gson();
                try {
                    if (!result.contains(TEMPLATE_RESULT_REQUEST))
                        throw new Exception();
                    List<ContactLocation> contactLocations = gson.fromJson(result, new TypeToken<List<ContactLocation>>() {
                    }.getType());
                    NotificationManager.notifyClients(WhereAreYouAppConstants.NOTIFICATION_CONTACTS_LOCATION, contactLocations);
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

    private void queryContacts() {
        ContactsHelper.getInstance().queryContactsFromServer(new BaseResponseCallback<String>() {
            @Override
            public void onSuccess(String result) {
                if (result.contains(TEMPLATE_RESULT_REQUEST)) {
                    NotificationManager.notifyClients(WhereAreYouAppConstants.NOTIFICATION_CONTACTS_LOADED, result);
                } else {
                    Toast.makeText(getActivity(), R.string.toast_unknown_error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Exception error) {
                Toast.makeText(getActivity(), R.string.toast_unknown_error, Toast.LENGTH_SHORT).show();
            }
        });
    }


    @SuppressWarnings("unchecked")
    @Override
    public void handleNotificationMessage(int what, int arg1, int arg2, Object obj) {
        if(obj == null)
            return;
        ContactLocation contactLocation;
        if (what == WhereAreYouAppConstants.NOTIFICATION_CONTACTS_LOADED) {
            // ******** Comment for refactor ******* //
            /*Gson gson = new Gson();
            try {
                String result = (String) obj;

                if(!result.contains("No contacts have been added for this user")) {
                    List<Contact> contactList = gson.fromJson(result, new TypeToken<List<Contact>>() {
                    }.getType());

                    ContactsHelper.getInstance().saveContacts(contactList);
                    ContactsHelper.getInstance().putContacts(contactList);

                    adapter = new ContactsAdapter(getActivity(), R.layout.item_contacts, ContactsHelper.getInstance().getContactsByStatus(new Pair<>(Contact.Status.pending.name(), Contact.Status.approve.name())));
                    listView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getActivity(),R.string.toast_no_contacts,Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                Toast.makeText(getActivity(), R.string.toast_unknown_error, Toast.LENGTH_SHORT).show();
            }*/
        } else if (what == WhereAreYouAppConstants.NOTIFICATION_CONTACTS_LOCATION) {
            List<ContactLocation> loaded = (List<ContactLocation>) obj;
            contactLocation = loaded.get(0);
            MapForPushActivity.startMapRequestContact(getActivity(), contactLocation);
        }
    }

}

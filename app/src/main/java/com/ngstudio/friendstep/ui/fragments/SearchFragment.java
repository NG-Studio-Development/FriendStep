package com.ngstudio.friendstep.ui.fragments;

import android.app.SearchManager;
import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.ngstudio.friendstep.ui.activities.MainActivity;
import com.ngstudio.friendstep.ui.activities.SearchActivity;
import com.ngstudio.friendstep.ui.adapters.ContactsAdapter;
import com.ngstudio.friendstep.utils.ContactsHelper;
import com.ngstudio.friendstep.utils.SettingsHelper;
import com.ngstudio.friendstep.utils.WhereAreYouAppConstants;

import org.jetbrains.annotations.NotNull;

import java.util.List;


public class SearchFragment extends BaseFragment<SearchActivity> implements NotificationManager.Client {

    private boolean isSearchOpened = false;
    private EditText edtSearch;
    private ListView lvContacts;
    private ProgressBar pbContacts;
    private String request;
    private ContactsAdapter adapter;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public int getLayoutResID() {
        return R.layout.fragment_search;
    }


    public static SearchFragment newInstance(Bundle args) {
        SearchFragment searchFragment = new SearchFragment();
        searchFragment .setArguments(args);
        return searchFragment ;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void findChildViews(@NotNull View view) {
        super.findChildViews(view);

        NotificationManager.registerClient(this, new NotificationManager
                .MessageFilter(WhereAreYouAppConstants.NOTIFICATION_CONTACTS_FIND_BY_NAME));

        lvContacts = (ListView) view.findViewById(R.id.lvContacts);
        pbContacts = (ProgressBar) view.findViewById(R.id.pbContacts);

        adapter = new ContactsAdapter(getActivity(), R.layout.item_contacts);
        lvContacts.setAdapter(adapter);

        setHasOptionsMenu(true);
        getHostActivity().getSupportActionBar().setDisplayShowTitleEnabled(false);
        getHostActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search, menu);
        menu.findItem(R.id.search_close_btn).setVisible(isSearchOpened);
        handleMenuSearch();
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getHostActivity().onBackPressed();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void handleMenuSearch(){

        ActionBar action = getHostActivity().getSupportActionBar();
        action.setDisplayShowCustomEnabled(true); //enable it to display a
        action.setCustomView(R.layout.action_search);//add the custom view
        action.setDisplayShowTitleEnabled(false); //hide the title

        edtSearch = (EditText)action.getCustomView().findViewById(R.id.edtSearch); //the text editor

        if (request!=null && !request.isEmpty())
            edtSearch.setText(request);

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                lvContacts.setVisibility(View.INVISIBLE);
                pbContacts.setVisibility(View.VISIBLE);
                Log.d("ACTION_SEARCH_BF", "Before");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                request = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d("ACTION_SEARCH", "AFTER");

                if(!isSearchOpened) {
                    isSearchOpened = true;
                    getActivity().invalidateOptionsMenu();
                }

                querySearchContactsByName(s.toString());

            }
        });

        edtSearch.requestFocus();

        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(edtSearch, InputMethodManager.SHOW_IMPLICIT);
    }




    public void querySearchContactsByName(String name) {

        ContactRequestStepServer getContacts = ContactRequestStepServer.requestFindContactByName (name);
        HttpServer.submitToServer(getContacts, new BaseResponseCallback<String>() {
            @Override
            public void onSuccess(String result) {

                if (result != null)
                    NotificationManager.notifyClients(WhereAreYouAppConstants.NOTIFICATION_CONTACTS_FIND_BY_NAME, result);
                else
                    Toast.makeText(getHostActivity(), R.string.toast_location_is_not_available, Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onError(Exception error) {
                Toast.makeText(getActivity(), R.string.toast_unknown_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void handleNotificationMessage(int what, int arg1, int arg2, Object obj) {
        if(obj == null) return;

        if (what == WhereAreYouAppConstants.NOTIFICATION_CONTACTS_FIND_BY_NAME)
            loadList(obj);
    }

    protected void loadList(Object obj) {

        String result = (String) obj;
        List<ContactStep> findList;

        Gson gson = new Gson();
        try {
            findList = gson.fromJson(result, new TypeToken<List<ContactStep>>() {}.getType());
            adapter.clear();
            adapter.addAll(findList);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), R.string.toast_unknown_error, Toast.LENGTH_SHORT).show();
        }

        lvContacts.setVisibility(View.VISIBLE);
        pbContacts.setVisibility(View.INVISIBLE);
    }
}

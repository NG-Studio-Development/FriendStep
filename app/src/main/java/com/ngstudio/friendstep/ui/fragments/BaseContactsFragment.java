package com.ngstudio.friendstep.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alexutils.helpers.BitmapUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ngstudio.friendstep.FragmentPool;
import com.ngstudio.friendstep.R;
import com.ngstudio.friendstep.WhereAreYouApplication;
import com.ngstudio.friendstep.components.NotificationManager;
import com.ngstudio.friendstep.model.connectivity.BaseResponseCallback;
import com.ngstudio.friendstep.model.connectivity.HttpServer;
import com.ngstudio.friendstep.model.connectivity.requests.stepserver.ContactRequestStepServer;
import com.ngstudio.friendstep.model.entity.step.ContactStep;
import com.ngstudio.friendstep.ui.activities.MainActivity;
import com.ngstudio.friendstep.ui.adapters.ContactsAdapter;
import com.ngstudio.friendstep.ui.dialogs.AlertDialogBase;
import com.ngstudio.friendstep.utils.CommonUtils;
import com.ngstudio.friendstep.utils.ContactsHelper;
import com.ngstudio.friendstep.utils.InputValidationUtils;
import com.ngstudio.friendstep.utils.WhereAreYouAppConstants;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


public abstract class BaseContactsFragment extends BaseFragment<MainActivity> implements NotificationManager.Client {

    ListView listView;
    ContactsAdapter adapter;
    TextView tvEmptyList;
    ImageButton buttonPlus;
    ProgressBar pbLoadingList;
    private FragmentPool fragmentPool = FragmentPool.getInstance();

    @Override
    public int getLayoutResID() {
        return R.layout.fragment_contacts;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentPool.popFragment(BaseContactsFragment.class);
        return super.onCreateView(inflater,container,savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        NotificationManager.unregisterClient(this);
        fragmentPool.free(BaseContactsFragment.class,this);
        super.onDestroyView();
    }

    @Override
    public void findChildViews(@NotNull View view) {
        super.findChildViews(view);

        NotificationManager.registerClient(this, new NotificationManager
                .MessageFilter(WhereAreYouAppConstants.NOTIFICATION_CONTACTS_LOADED));

        buttonPlus = (ImageButton) view.findViewById(R.id.buttonPlus);
        buttonPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = LayoutInflater.from(getActivity());
                showDialogAddContacts(inflater.inflate(R.layout.view_dontent_dialog_contacts, null, false));
            }
        });

        tvEmptyList = (TextView) view.findViewById(R.id.tvEmptyList);


        pbLoadingList = (ProgressBar) view.findViewById(R.id.pbLoadingList);

        listView = (ListView) view.findViewById(R.id.listContacts);

        //if (adapter == null)
        queryContacts();
    }

    @Override
    public void onResume() {
        super.onResume();
        //adapter.notifyDataSetChanged();
    }

    private void queryContacts() {
        ContactsHelper.getInstance().queryContactsFromServer(new BaseResponseCallback<String>() {
            @Override
            public void onSuccess(String result) {

                if ( result == null || result.contains("Error") )
                    Toast.makeText(getHostActivity(), R.string.toast_unknown_error, Toast.LENGTH_SHORT).show();
                else
                    NotificationManager.notifyClients(WhereAreYouAppConstants.NOTIFICATION_CONTACTS_LOADED, result);
            }

            @Override
            public void onError(Exception error) {
                Toast.makeText(getActivity(), R.string.toast_unknown_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /*public void queryNearbyContacts() {
        Location currentLocation = CustomLocationManager.getInstance().getCurrentLocation();
        BaseContactRequest getContacts = BaseContactRequest.createGetNearbyContactsRequest(WhereAreYouApplication.getInstance().getUuid(), currentLocation.getLatitude(), currentLocation.getLongitude());
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
    } */

    public void showDialogAddContacts(View view) {

        Button buttonSend = (Button) view.findViewById(R.id.buttonAdd);
        Button buttonCancel = (Button) view.findViewById(R.id.buttonCancel);

        final EditText name = (EditText) view.findViewById(R.id.etName);
        final AlertDialogBase dialogSendLocation = new AlertDialogBase(getActivity());
        dialogSendLocation.setCustomView(view);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialogSendLocation.getWindow().getAttributes());
        lp.width = BitmapUtils.getDisplayWidth(getActivity()) - (int) (2* getResources().getDimension(R.dimen.margin_widget_default_tiny));
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.y = (int) getResources().getDimension(R.dimen.margin_widget_default_tiny);

        dialogSendLocation.show();
        dialogSendLocation.getWindow().setAttributes(lp);

        buttonSend.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final String username = name.getText().toString();
                if(!InputValidationUtils.checkNonEmptyFieldWithToast(getActivity(), username, getString(R.string.text_username)))
                    return;

                String contactName = CommonUtils.getText(name);
                dialogSendLocation.dismiss();
                getHostActivity().showProgressDialog();
                sendContactRequest(WhereAreYouApplication.getInstance().getUserId(), contactName);
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogSendLocation.dismiss();
            }
        });
    }

    private void sendContactRequest(long id, String contactName) {

        try {
            ContactRequestStepServer request = ContactRequestStepServer.requestSendCandidature(id, contactName);

            getHostActivity().showProgressDialog();
            HttpServer.submitToServer(request, new BaseResponseCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    if(result.contains("Error")) {
                        try {
                            JSONObject answer = new JSONObject(result);
                            Toast.makeText(getActivity(),answer.getString("Error"),Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if(result.contains("null")) {
                        Toast.makeText(getActivity(),R.string.toast_unknown_error,Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), R.string.toast_contact_request_sent, Toast.LENGTH_SHORT).show();
                        queryContacts();
                    }
                    getHostActivity().hideProgressDialog();
                }

                @Override
                public void onError(Exception error) {
                    getHostActivity().hideProgressDialog();
                    Toast.makeText(getActivity(),R.string.toast_unknown_error,Toast.LENGTH_SHORT).show();
                    getHostActivity().hideProgressDialog();
                }
            });
        } catch (IllegalArgumentException e) {
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }




    /*private String retrieveContactPhoto() {

        Bitmap photo = null;
        String pathPhoto = null;
        String namePhoto;

        InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(getActivity().getContentResolver(),
                ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, new Long(contactID)));

        if (inputStream != null) {
            photo = BitmapFactory.decodeStream(inputStream);
            OutputStream outputStream = null;

            namePhoto = new Date().getTime()+".jpg";
            pathPhoto = Environment.getExternalStorageDirectory()+"/"+MainActivity.DIR_NAME+"/"+namePhoto;

            try{
                outputStream =
                        new FileOutputStream(new File(pathPhoto));
                photo.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

            }catch(FileNotFoundException ex){ex.printStackTrace();}
        }
        return pathPhoto;
    }*/

    protected abstract String getStatusContactFilter();

    @SuppressWarnings("unchecked")
    @Override
    public void handleNotificationMessage(int what, int arg1, int arg2, Object obj) {
        if(obj == null)
            return;

        if (what == WhereAreYouAppConstants.NOTIFICATION_CONTACTS_LOADED) {
            Gson gson = new Gson();
            try {
                String result = (String) obj;
                
                if (!result.contains("No contacts have been added for this user")) {

                    List<ContactStep> contactStepList = gson.fromJson(result, new TypeToken<List<ContactStep>>() {
                    }.getType());

                    ContactsHelper.getInstance().saveContacts(contactStepList);
                    ContactsHelper.getInstance().putContacts(contactStepList);

                    adapter = new ContactsAdapter(getActivity(),
                            R.layout.item_contacts,
                            ContactsHelper.getInstance().getContactsByStatus(getStatusContactFilter()));

                    listView.setAdapter(adapter);
                    pbLoadingList.setVisibility(View.GONE);

                    if (adapter.getCount() == 0)
                        tvEmptyList.setVisibility(View.VISIBLE);

                } else {
                    Toast.makeText(getActivity(),R.string.toast_no_contacts,Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                Toast.makeText(getActivity(), R.string.toast_unknown_error, Toast.LENGTH_SHORT).show();
            }
        }
    }
}

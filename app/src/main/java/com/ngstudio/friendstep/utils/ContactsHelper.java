package com.ngstudio.friendstep.utils;

import android.util.Log;
import android.util.Pair;

import com.alexutils.dao.GenericDao;
import com.google.android.gms.maps.model.LatLng;
import com.ngstudio.friendstep.WhereAreYouApplication;
import com.ngstudio.friendstep.model.connectivity.BaseResponseCallback;
import com.ngstudio.friendstep.model.connectivity.HttpServer;
import com.ngstudio.friendstep.model.connectivity.requests.BaseContactRequest;
import com.ngstudio.friendstep.model.connectivity.requests.stepserver.GetContactStepServerRequest;
import com.ngstudio.friendstep.model.entity.step.ContactStep;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


public class ContactsHelper {

    //Calendar calendar;
    private ContactsHelper() {}
    private List<ContactStep> contactsList = new ArrayList<>();
    private static class ContactsHolder {
        public static final ContactsHelper HOLDER_INSTANCE = new ContactsHelper();
    }
    public static ContactsHelper getInstance() {return ContactsHolder.HOLDER_INSTANCE;}

    boolean isRequestedFromServer;

    public void putContacts(Collection<ContactStep> contactsList) {
        for (ContactStep contact : contactsList) {
            putContacts(contact);
        }
    }

    public void putContacts(ContactStep contact) {
        int position = contactsList.indexOf(contact);

        if(position == -1) {
            contactsList.add(contact);
        } else {
            contactsList.remove(position);
            contactsList.add(position,contact);
        }
    }

    @Deprecated
    public List<ContactStep> getContactsByStatus(Pair<String,String>... statuses) {

        if(contactsList.isEmpty())
            loadContacts();

        if(statuses.length == 0)
            return contactsList;

        List<ContactStep> contactsStatusFilterList = new ArrayList<>();
        List<Pair<String,String>> statusesList = Arrays.asList(statuses);

        for (ContactStep contact : contactsList) {
            if(statusesList.contains(new Pair<>(contact.getYourstatus(),contact.getContact_status()))) {
                contactsStatusFilterList.add(contact);
            }
        }

        return contactsStatusFilterList;
        // return null;
    }

    public List<ContactStep> getContactsByStatus(@NotNull String status) {

        if(contactsList.isEmpty()) {
            loadContacts();
            Log.d("CONTACTS_LIST","Contacts list is empty");
        }


        List<ContactStep> contactsStatusFilterList = new ArrayList<>();

        for (ContactStep contact : contactsList)
            if(contact.isStatus(status))
                contactsStatusFilterList.add(contact);

        return contactsStatusFilterList;
    }


    public List<ContactStep> loadContacts() {
        return (contactsList = GenericDao.getGenericDaoInstance(ContactStep.class).getObjects()) == null ? contactsList = new ArrayList<>() : contactsList;
    }

    public boolean saveContacts(Collection<ContactStep> contacts) {
        return GenericDao.getGenericDaoInstance(ContactStep.class).save(contacts);
    }

    public void updateContacts(Collection<ContactStep> contacts) {
        saveContacts(contacts);
        putContacts(contacts);
    }

    /*public void queryContactsFromServer(BaseResponseCallback<String> callback) {
        BaseContactRequest getContacts = BaseContactRequest.createGetContactsRequest(WhereAreYouApplication.getInstance().getUuid());
        HttpServer.submitToServer(getContacts, callback);
    }*/


    public void queryContactsFromServer(BaseResponseCallback<String> callback) {

        long userId = WhereAreYouApplication.getInstance().getApplicationPreferences()
                .getLong(WhereAreYouAppConstants.PREF_KEY_ID_USER, -1);
        GetContactStepServerRequest request = null;

        if (userId > -1)
            request = GetContactStepServerRequest.newInstance(userId);
        else
            throw new Error("Not found user id !!!");

        HttpServer.submitToServer(request, callback);
    }

    public void queryCreateSendLocation(LatLng latLng, String mobilenumber, BaseResponseCallback<String> callback) {
        BaseContactRequest getContacts = BaseContactRequest.createSendLocationRequest(WhereAreYouApplication.getInstance().getUuid(),
                                                                                        latLng.latitude,
                                                                                        latLng.longitude,
                                                                                        mobilenumber);
        HttpServer.submitToServer(getContacts, callback);
    }

    public boolean isRequestedFromServer() {
        return isRequestedFromServer;
    }
}

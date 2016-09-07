package com.ngstudio.friendstep.model.connectivity.requests.stepserver;

import android.location.Location;
import android.util.Log;

import com.ngstudio.friendstep.WhereAreYouApplication;
import com.ngstudio.friendstep.model.connectivity.requests.BaseRequest;
import com.ngstudio.friendstep.model.connectivity.requests.RequestType;
import com.ngstudio.friendstep.utils.WhereAreYouAppConstants;

public class ContactRequestStepServer extends BaseRequest {

    long id;
    String name;

    private ContactRequestStepServer(String method, long id, String name) {
        super(WhereAreYouAppConstants.STEP_SERVER_URL, method, RequestType.GET);
        this.id = id;
        this.name = name;
    }

    public static ContactRequestStepServer requestSendCandidature(long userId, String friendName) {
        return addContact(userId, friendName, 0);
    }

    public static ContactRequestStepServer requestApproveCandidature(long userId, String friendName) {
        return addContact(userId, friendName, 1);
    }

    public static ContactRequestStepServer requestGetNearbyContacts(Location location, int distentionFind /*long userId, String friendName*/) {
        //return addContact(userId, friendName, 1);
        //?user_id=70&user_lat=47.199702&user_long=39.626338&distance_find=50
        Log.d("QUERY_NERBY_USER", "Distantion  = " + distentionFind);
        return new ContactRequestStepServer("get_location_contact.php?user_id="+WhereAreYouApplication.getInstance().getUserId()+"&user_lat="+location.getLatitude()+"&user_long="+location.getLongitude()+"&distance_find="+distentionFind +"&temp=22",-1,"");
    }

    private static ContactRequestStepServer addContact(long userId, String friendName, int action) {
        return new ContactRequestStepServer("add_contact.php?id="+userId+"&name="+friendName+"&action="+action+"&tempValue=x", userId, friendName);
    }

    public static ContactRequestStepServer requestFindContactByName(String friendName) {
        return new ContactRequestStepServer("get_list_all_contacts.php", 0,"");
    }

}
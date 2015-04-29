package com.ngstudio.friendstep.model.connectivity.requests.stepserver;

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

    @Deprecated
    public static ContactRequestStepServer requestAddContacts(long userId, String friendName) {
        //return new AddContactsRequest("add_contact.php", userId, friendName);
        return new ContactRequestStepServer("add_contact.php?id="+userId+"&name="+friendName+"&tempValue=x", userId, friendName);
    }

    public static ContactRequestStepServer requestSendCandidature(long userId, String friendName) {
        return addContact(userId, friendName, 0);
    }

    public static ContactRequestStepServer requestApproveCandidature(long userId, String friendName) {
        return addContact(userId, friendName, 1);
    }

    private static ContactRequestStepServer addContact(long userId, String friendName, int action) {
        return new ContactRequestStepServer("add_contact.php?id="+userId+"&name="+friendName+"&action="+action+"&tempValue=x", userId, friendName);
    }

    public static ContactRequestStepServer requestGetCandidateList(long userId, String friendName) {
        //return new AddContactsRequest("add_contact.php", userId, friendName);
        return new ContactRequestStepServer("request_to_friend.php?id="+userId+"&name="+friendName+"&tempValue=x", userId, friendName);
    }
}
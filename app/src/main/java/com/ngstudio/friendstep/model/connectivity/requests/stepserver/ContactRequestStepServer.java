package com.ngstudio.friendstep.model.connectivity.requests.stepserver;

import com.ngstudio.friendstep.model.connectivity.requests.BaseRequest;
import com.ngstudio.friendstep.model.connectivity.requests.RequestType;
import com.ngstudio.friendstep.utils.WhereAreYouAppConstants;

public class ContactRequestStepServer  extends BaseRequest {

    long id;
    String name;

    private ContactRequestStepServer(String method, long id, String name) {
        super(WhereAreYouAppConstants.STEP_SERVER_URL, method, RequestType.GET);
        this.id = id;
        this.name = name;
    }

    public static ContactRequestStepServer RequestAddContacts(long userId, String friendName) {
        //return new AddContactsRequest("add_contact.php", userId, friendName);
        return new ContactRequestStepServer("add_contact.php?id="+userId+"&name="+friendName+"&tempValue=x", userId, friendName);
    }
}
package com.ngstudio.friendstep.model.connectivity.requests.stepserver;

/*public class AddContactsRequest extends BaseRequest {

    long id;
    String name;

    private AddContactsRequest(String method, long id, String name) {
        super(WhereAreYouAppConstants.STEP_SERVER_URL, method, RequestType.GET);
        this.id = id;
        this.name = name;
    }

    public static AddContactsRequest newInstance(long userId, String friendName) {
        //return new AddContactsRequest("add_contact.php", userId, friendName);
        return new AddContactsRequest("add_contact.php?id="+userId+"&name="+friendName+"&tempValue=x", userId, friendName);
    }
} */
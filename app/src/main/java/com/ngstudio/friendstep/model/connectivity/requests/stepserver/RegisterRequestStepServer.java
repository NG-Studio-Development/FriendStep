package com.ngstudio.friendstep.model.connectivity.requests.stepserver;

import com.ngstudio.friendstep.model.connectivity.requests.BaseRequest;
import com.ngstudio.friendstep.model.connectivity.requests.RequestType;
import com.ngstudio.friendstep.utils.WhereAreYouAppConstants;

public class RegisterRequestStepServer extends BaseRequest {

    private RegisterRequestStepServer(String method/*, long id, String name*/) {
        super(WhereAreYouAppConstants.STEP_SERVER_URL, method, RequestType.GET);
        //this.id = id;
        //this.name = name;
    }

    public static RegisterRequestStepServer requestSignIn(String name, String pass) {
        //return new ContactRequestStepServer("register.php?id="+userId+"&name="+friendName+"&action="+action+"&tempValue=x", userId, friendName);
        return new RegisterRequestStepServer("register.php?name="+name+"&pass="+pass+"&tempVal=0");
    }
}

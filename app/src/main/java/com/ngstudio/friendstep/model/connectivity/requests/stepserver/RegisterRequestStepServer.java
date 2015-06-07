package com.ngstudio.friendstep.model.connectivity.requests.stepserver;

import com.ngstudio.friendstep.model.connectivity.requests.BaseRequest;
import com.ngstudio.friendstep.model.connectivity.requests.RequestType;
import com.ngstudio.friendstep.utils.WhereAreYouAppConstants;

public class RegisterRequestStepServer extends BaseRequest {

    private RegisterRequestStepServer(String method) {
        super(WhereAreYouAppConstants.STEP_SERVER_URL, method, RequestType.GET);
    }

    public static RegisterRequestStepServer requestSignIn(String name, String pass, String regId) {
        return new RegisterRequestStepServer("register.php?name="+name+"&pass="+pass+"&regId="+regId+"&tempVal=0");
    }
}

package com.ngstudio.friendstep.model.connectivity.requests.stepserver;

import com.ngstudio.friendstep.model.connectivity.requests.BaseRequest;
import com.ngstudio.friendstep.model.connectivity.requests.RequestType;
import com.ngstudio.friendstep.utils.WhereAreYouAppConstants;

public class GetContactStepServerRequest extends BaseRequest {

    private GetContactStepServerRequest(String method) {
        super(WhereAreYouAppConstants.STEP_SERVER_URL,method, RequestType.GET);
    }

    public static GetContactStepServerRequest newInstance(long id) {
        return new GetContactStepServerRequest("get_list_contact.php?id="+id+"&tempValue=0");
    }


}

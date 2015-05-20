package com.ngstudio.friendstep.model.connectivity.requests.stepserver;

import com.ngstudio.friendstep.model.connectivity.requests.BaseRequest;
import com.ngstudio.friendstep.model.connectivity.requests.RequestType;
import com.ngstudio.friendstep.utils.WhereAreYouAppConstants;

public class InsertGeoCordsRequestStepServer extends BaseRequest {

    private InsertGeoCordsRequestStepServer(String method) {
        super(WhereAreYouAppConstants.STEP_SERVER_URL,method, RequestType.GET);
    }

    public static InsertGeoCordsRequestStepServer requestInsertGeoCoordinates(long idUser, double latitude, double longitude, boolean accessible_show) {
        return new InsertGeoCordsRequestStepServer("insert_user_location.php?id_user="+idUser
                                                                                +"&latitude="+latitude
                                                                                +"&longitude="+longitude
                                                                                +"&accessible_show="+accessible_show
                                                                                +"&temp=22"/*,name, null*/);
    }

    public static InsertGeoCordsRequestStepServer requestChangeAccessibleShowState(long idUser, boolean accessible_show) {
        return new InsertGeoCordsRequestStepServer("insert_user_location.php?id_user="+idUser
                +"&accessible_show="+accessible_show
                +"&temp=22");
    }
}

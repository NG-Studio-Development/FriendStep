package com.ngstudio.friendstep.model.connectivity.requests;

import com.ngstudio.friendstep.utils.WhereAreYouAppConstants;

import org.jetbrains.annotations.NotNull;

public class BaseAppRequest extends BaseRequest {

    @RequestField(type = RequestType.POST)
    private String uuid;

    @RequestField(type = RequestType.POST)
    private final String app = WhereAreYouAppConstants.SERVER_APP_NAME;

    public BaseAppRequest(@NotNull String method, @NotNull String uuid) {
        super(WhereAreYouAppConstants.BASE_SERVER_URL,method,RequestType.POST);
        this.uuid = uuid;
        setDataType("application/x-www-form-urlencoded");
        setAcceptType("application/json");
    }
}

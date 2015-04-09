package com.ngstudio.friendstep.model.connectivity.requests;

import org.jetbrains.annotations.NotNull;


public class RegisterApiRequest extends BaseAppRequest {

    @RequestField(type = RequestType.POST)
    private String pushkey;

    public RegisterApiRequest(@NotNull String uuid, @NotNull String pushkey) {
        super("registerapi.php", uuid);
        this.pushkey = pushkey;
    }
}

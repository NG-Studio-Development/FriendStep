package com.ngstudio.friendstep.model.connectivity.requests;

import org.jetbrains.annotations.NotNull;

public class BaseAvatarRequest extends BaseAppRequest {

    @RequestField(type = RequestType.POST)
    Long time_stamp;

    @RequestField(type = RequestType.POST)
    String image, mobilenumber;

    private BaseAvatarRequest(@NotNull String method, @NotNull String uuid, Long time_stamp, String image, String mobilenumber) {
        super(method, uuid);
        this.time_stamp = time_stamp;
        this.image = image;
        this.mobilenumber = mobilenumber;
    }

    public static BaseAvatarRequest getImagePostRequest(@NotNull String uuid, long time_stamp, @NotNull String image) {
        return new BaseAvatarRequest("imagepost.php",uuid,time_stamp,image, null);
    }

    public static BaseAvatarRequest getAvatarRequest(@NotNull String uuid, @NotNull String mobilenumber) {
        return new BaseAvatarRequest("getavatar.php",uuid,null,null,mobilenumber);
    }
}

package com.ngstudio.friendstep.model.connectivity.requests.stepserver;

import com.ngstudio.friendstep.model.connectivity.requests.BaseRequest;
import com.ngstudio.friendstep.model.connectivity.requests.RequestType;
import com.ngstudio.friendstep.utils.WhereAreYouAppConstants;

import org.jetbrains.annotations.NotNull;

public class AvatarRequestStepServer  extends BaseRequest {

    //@RequestField(type = RequestType.POST)
    //Long time_stamp;

    //@RequestField(type = RequestType.POST)
    //String image, mobilenumber;

    private AvatarRequestStepServer(@NotNull String method/*, @NotNull String uuid, Long time_stamp, String image, String mobilenumber*/) {
        super(WhereAreYouAppConstants.STEP_SERVER_URL,method, RequestType.GET);
        //super(method, uuid);
        //this.time_stamp = time_stamp;
        //this.image = image;
        //this.mobilenumber = mobilenumber;
    }

    public static AvatarRequestStepServer requestGetAvatar(String name) {
        return new AvatarRequestStepServer("get_avatar.php?name="+name /*,uuid,time_stamp,image, null*/ );
    }

    /*public static AvatarRequestStepServer getImagePostRequest(@NotNull String uuid, long time_stamp, @NotNull String image) {
        return new AvatarRequestStepServer("imagepost.php",uuid,time_stamp,image, null);
    }

    public static AvatarRequestStepServer getAvatarRequest(@NotNull String uuid, @NotNull String mobilenumber) {
        return new AvatarRequestStepServer("getavatar.php",uuid,null,null,mobilenumber);
    }*/
}


package com.ngstudio.friendstep.model.connectivity.requests.stepserver;

import com.ngstudio.friendstep.model.connectivity.requests.BaseRequest;
import com.ngstudio.friendstep.model.connectivity.requests.RequestField;
import com.ngstudio.friendstep.model.connectivity.requests.RequestType;
import com.ngstudio.friendstep.utils.WhereAreYouAppConstants;

import org.jetbrains.annotations.NotNull;

public class AvatarRequestStepServer extends BaseRequest {

    @RequestField(type = RequestType.POST)
    String image, name;

    private AvatarRequestStepServer(String method, String name, String image) {
        super(WhereAreYouAppConstants.STEP_SERVER_URL, method, RequestType.POST);
        this.name = name;
        this.image = image;

        setDataType("application/x-www-form-urlencoded");
        setAcceptType("application/json");
    }

    public static AvatarRequestStepServer requestGetAvatar(String name) {
        return new AvatarRequestStepServer("get_avatar.php",name, null);
    }

    public static AvatarRequestStepServer getImagePostRequest(@NotNull String name, @NotNull String image) {
        return new AvatarRequestStepServer("post_image.php", name, image);
    }
}


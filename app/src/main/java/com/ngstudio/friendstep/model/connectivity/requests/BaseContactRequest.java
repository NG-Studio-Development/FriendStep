package com.ngstudio.friendstep.model.connectivity.requests;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BaseContactRequest extends BaseAppRequest {

    @RequestField(type = RequestType.POST)
    private Double latitude, longitude;

    @RequestField(type = RequestType.POST)
    private String mobilenumber, contactname, message,  type, contactmobilenumber, contactmobilename, status;

    protected BaseContactRequest(@NotNull String method, @NotNull String uuid, Double latitude, Double longitude, @Nullable String mobilenumber, @Nullable String message, @Nullable String contactname,
                                 @Nullable String type, @Nullable String contactmobilenumber, @Nullable String contactmobilename, @Nullable String status) {
        super(method, uuid);
        this.latitude = latitude;
        this.longitude = longitude;
        this.mobilenumber = mobilenumber;
        this.contactname = contactname;
        this.message = message;
        this.type = type;
        this.contactmobilenumber = contactmobilenumber;
        this.contactmobilename = contactmobilename;
        this.status = status;
    }

    public static BaseContactRequest createLocationContactRequest(@NotNull String uuid, double latitude, double longitude, @NotNull String mobilenumber, @Nullable String message, @NotNull String contactname) {
        return new BaseContactRequest("locationrequest.php",uuid,latitude,longitude,mobilenumber,message,contactname,null,null,null,null);
    }

    public static BaseContactRequest createLocationContactReplyRequest(@NotNull String uuid, double latitude, double longitude, @NotNull String mobilenumber, @Nullable String message, @NotNull String status) {
        return new BaseContactRequest("locationrequestreply.php",uuid,latitude,longitude,mobilenumber,message,null,null,null,null,status);
    }

    public static BaseContactRequest createGetContactsRequest(@NotNull String uuid) {
        return new BaseContactRequest("getcontacts.php",uuid,null,null,null,null,null,null,null,null,null);
    }

    public static BaseContactRequest createGetContactLocationsRequest(@NotNull String uuid, @NotNull String contactmobilenumber) {
        return new BaseContactRequest("getcontactlocation.php",uuid,null,null,null,null,null,null,contactmobilenumber,null,null);
    }

    public static BaseContactRequest createGetContactsLocationsRequest(@NotNull String uuid) {
        return new BaseContactRequest("getcontactslocations.php",uuid,null,null,null,null,null,"ALL",null,null,null);
    }

    public static BaseContactRequest createRenameContactRequest(@NotNull String uuid, @NotNull String contactmobilenumber, @NotNull String contactename) {
        return new BaseContactRequest("renamecontact.php",uuid,null,null,null,null,contactename,null,contactmobilenumber,null,null);
    }

    public static BaseContactRequest createDeleteContactRequest(@NotNull String uuid, @NotNull String mobilenumber) {
        return new BaseContactRequest("deletecontact.php",uuid,null,null,mobilenumber,null,null,null,null,null,null);
    }

    public static BaseContactRequest createGetChatContactsRequest(@NotNull String uuid) {
        return new BaseContactRequest("getchatcontacts.php",uuid,null,null,null,null,null,null,null,null,null);
    }

    public static BaseContactRequest createGetNearbyContactsRequest(@NotNull String uuid, double latitude, double longitude) {
        return new BaseContactRequest("getnearbycontacts.php",uuid,latitude,longitude,null,null,null,null,null,null,null);
    }

    public static BaseContactRequest createSendLocationRequest(@NotNull String uuid, double latitude, double longitude, @NotNull String mobilenumber) {
        return new BaseContactRequest("sendlocation.php",uuid,latitude,longitude,mobilenumber,null,null,null,null,null,null);
    }

}

package com.ngstudio.friendstep.model.connectivity.requests;


import org.jetbrains.annotations.NotNull;

public class BaseMessageRequest extends BaseAppRequest {

    @RequestField(type = RequestType.POST)
    private String mobilenumber, message, sendermobilenumber;

    @RequestField(type = RequestType.POST)
    private Long messagetime;

    private BaseMessageRequest(@NotNull String method, @NotNull String uuid, String mobilenumber, String message, String sendermobilenumber, Long messagetime) {
        super(method, uuid);
        this.mobilenumber = mobilenumber;
        this.message = message;
        this.sendermobilenumber = sendermobilenumber;
        this.messagetime = messagetime;
    }

    public static BaseMessageRequest createSendMessageRequest(@NotNull String uuid, @NotNull String mobilenumber, @NotNull String message) {
        return new BaseMessageRequest("sendmessage.php",uuid,mobilenumber,message,null,null);
    }

    public static BaseMessageRequest createGetMessageRequest(@NotNull String uuid, @NotNull String sendermobilenumber, long messagetime) {
        return new BaseMessageRequest("getmessage.php",uuid,null,null,sendermobilenumber,messagetime);
    }

    public static BaseMessageRequest createGetMessagesRequest(@NotNull String uuid, @NotNull String mobilenumber) {
        return new BaseMessageRequest("getmessages.php",uuid,mobilenumber,null,null,null);
    }

    public static BaseMessageRequest createGetHistoryRequest(@NotNull String uuid) {
        return new BaseMessageRequest("gethistory.php",uuid,null,null,null,null);
    }

    public String getSendermobilenumber() {
        return sendermobilenumber;
    }
}

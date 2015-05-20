package com.ngstudio.friendstep.model.connectivity.requests.stepserver;

import com.ngstudio.friendstep.model.connectivity.requests.BaseRequest;
import com.ngstudio.friendstep.model.connectivity.requests.RequestType;
import com.ngstudio.friendstep.utils.WhereAreYouAppConstants;

public class MessageRequestStepServer extends BaseRequest {

    private MessageRequestStepServer(String method) {
        super(WhereAreYouAppConstants.STEP_SERVER_URL,method, RequestType.GET);
    }

    public static MessageRequestStepServer createSendMessageRequest(long idUser, String nameUser, long idFriend, String message) {
        return new MessageRequestStepServer("send_message.php?userId="+idUser+"&nameUser="+nameUser+"&friendId="+idFriend+"&message="+message+"&tempValue=0");
    }

    public static MessageRequestStepServer createGetMessagesRequest(long idUser, long idFriend) {
        return new MessageRequestStepServer("get_list_messages.php?userId="+idUser+"&friendId="+idFriend+"&tempValue=0");
    }
}
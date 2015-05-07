package com.ngstudio.friendstep.utils;

import com.alexutils.dao.GenericDao;
import com.ngstudio.friendstep.model.connectivity.BaseResponseCallback;
import com.ngstudio.friendstep.model.connectivity.HttpServer;
import com.ngstudio.friendstep.model.connectivity.requests.stepserver.MessageRequestStepServer;
import com.ngstudio.friendstep.model.entity.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MessagesHelpers {

    private MessagesHelpers() { /* Optional */ }

    public Map<String, List<Message>> messageMap = new HashMap<>();
    private List<Message> messageList = new ArrayList<>();

    public static class SingletonHolder {
        public static final MessagesHelpers HOLDER_INSTANCE = new MessagesHelpers();
    }

    public static MessagesHelpers getInstance() {
        return SingletonHolder.HOLDER_INSTANCE;
    }

    public void putMessages(List<Message> messageList) {

        for (Message message : messageList) {
            putMessages(message);
        }

        sortMap();
    }

    public void putMessages(Message message) {
        int position = messageList.indexOf(message);

        if(position == -1) {
            messageList.add(message);
        } else {
            messageList.remove(position);
            messageList.add(position, message);
        }
    }

    public List<Message> loadMessages(String mobile) {
        if (messageList.size() == 0)
            loadMessages();

        if (!messageMap.containsKey(mobile))
            messageMap.put(mobile, new ArrayList<Message>());

        return messageMap.get(mobile);
    }

    private List<Message> loadMessages() {
        messageList = (messageList = GenericDao.getGenericDaoInstance(Message.class).getObjects(null,null,null,null,"messagetime desc",null)) == null ? messageList = new ArrayList<>() : messageList;
        sortMap();
        return messageList;
    }

    public void sortMap() {
        Message message = null;
        Iterator<Message> iterator = null;

        if ((iterator = messageList.iterator()) == null)
            return;
        else if (!messageMap.isEmpty())
            resetMap();

        while (iterator.hasNext()) {
            message = iterator.next();
            if (!messageMap.containsKey(message.getFriendId()))
                messageMap.put(message.getFriendId(),new ArrayList<Message>());
            messageMap.get(message.getFriendId()).add(message);
        }
        sortList();
    }

    void resetMap() {
        if (messageMap != null) {
            Set<String> set = messageMap.keySet();
            for (String key : set)
                messageMap.get(key).clear();
        }
    }

    public void sortList() {
        if (messageMap != null) {
            Set<String> set = messageMap.keySet();
            for (String key : set)
                Collections.sort(messageMap.get(key), new MessageComparator());
        }
    }

    public int size(/*String mobile*/) {
        //return messageMap.get(mobile).size();
        return -1;
    }

    public boolean saveMessages(List<Message> messages) {
        return GenericDao.getGenericDaoInstance(Message.class).save(messages);
    }

    public void queryMessagesFromServer(long idUser,long idFriend, BaseResponseCallback<String> callback) {
        MessageRequestStepServer getMessages = MessageRequestStepServer.createGetMessagesRequest(idUser, idFriend);
        HttpServer.submitToServer(getMessages, callback);
    }

    /* public void queryMessagesSend(String mobile,String textToSend, BaseResponseCallback<String> callback) {
        BaseMessageRequest sendMessageRequest = BaseMessageRequest.createSendMessageRequest(WhereAreYouApplication.getInstance().getUuid(),mobile, textToSend);
        HttpServer.submitToServer(sendMessageRequest,callback);
    } */

    public void queryMessagesSend(long idUser, long idFriend, String message, BaseResponseCallback<String> callback) {
        MessageRequestStepServer messageRequestStepServer = MessageRequestStepServer.createSendMessageRequest(idUser, idFriend, message);
        HttpServer.submitToServer(messageRequestStepServer,callback);
    }

    public class MessageComparator implements Comparator<Message> {
        @Override
        public int compare(Message message, Message messageComparable) {
            return message.compareTo(messageComparable);
        }
    }
}

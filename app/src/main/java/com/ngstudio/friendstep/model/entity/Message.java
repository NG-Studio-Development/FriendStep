package com.ngstudio.friendstep.model.entity;

import com.alexutils.annotations.DbAnnotation;
import com.alexutils.annotations.DbMainAnnotation;
import com.ngstudio.friendstep.WhereAreYouApplication;
import com.ngstudio.friendstep.utils.WhereAreYouAppConstants;

@DbMainAnnotation(tableName = "messages", keyFields = {"messagetime"})
public class Message implements Comparable {

    @DbAnnotation
    String sendername, message, receivername, sender_id, receivermobilenumber, receivemessage;

    @DbAnnotation(dbType = "integer")
    long messagetime;

    public Message(String sendername, String message, String receivername, String sender_id, String receivermobilenumber, long messagetime) {
        this.sendername = sendername;
        this.message = message;
        this.receivername = receivername;
        this.sender_id = sender_id;
        this.receivermobilenumber = receivermobilenumber;
        this.messagetime = messagetime;
    }

    public Message() {

    }

    public String getSendername() {
        return sendername;
    }

    public void setSendername(String sendername) {
        this.sendername = sendername;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReceivername() {
        return receivername;
    }

    public void setReceivername(String receivername) {
        this.receivername = receivername;
    }

    public String getSenderId() {
        return sender_id;
    }

    public void setSenderId(String sender_id) {
        this.sender_id = sender_id;
    }

    public String getReceivermobilenumber() {
        return receivermobilenumber;
    }

    public void setReceivermobilenumber(String receivermobilenumber) {
        this.receivermobilenumber = receivermobilenumber;
    }

    public long getMessagetime() {
        return messagetime;
    }

    public void setMessagetime(long messagetime) {
        this.messagetime = messagetime;
    }

    public String getReceivemessage() {
        return receivemessage;
    }

    public void setReceivemessage(String receivemessage) {
        this.receivemessage = receivemessage;
    }

    @Override
    public boolean equals(Object message) {
        return (this.getMessagetime() == ((Message) message).getMessagetime());
    }

    public boolean isMine() {
        return WhereAreYouApplication.getPrefString(WhereAreYouAppConstants.PREF_KEY_EMAIL,"").equals(getSenderId());
    }

    public String getFriendId() {
        return isMine() ? getReceivermobilenumber() : getSenderId();
    }

    @Override
    public int compareTo(Object obj) {
        Message tmp = (Message)obj;
        if(this.getMessagetime() < tmp.getMessagetime())
            return 1;
        else if(this.getMessagetime() > tmp.getMessagetime())
            return -1;
        return 0;
    }
}

package com.ngstudio.friendstep.model.entity;


public class ChatContact {

    String yourmobilenumber, receivermobilenumber, message, yourname, contactname;

    long messagetime;

    public String getYourmobilenumber() {
        return yourmobilenumber;
    }

    public void setYourmobilenumber(String yourmobilenumber) {
        this.yourmobilenumber = yourmobilenumber;
    }

    public String getReceivermobilenumber() {
        return receivermobilenumber;
    }

    public void setReceivermobilenumber(String receivermobilenumber) {
        this.receivermobilenumber = receivermobilenumber;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getYourname() {
        return yourname;
    }

    public void setYourname(String yourname) {
        this.yourname = yourname;
    }

    public String getContactname() {
        return contactname;
    }

    public void setContactname(String contactname) {
        this.contactname = contactname;
    }

    public long getMessagetime() {
        return messagetime;
    }

    public void setMessagetime(long messagetime) {
        this.messagetime = messagetime;
    }
}

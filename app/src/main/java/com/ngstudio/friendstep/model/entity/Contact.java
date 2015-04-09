package com.ngstudio.friendstep.model.entity;


import com.alexutils.annotations.DbAnnotation;
import com.alexutils.annotations.DbMainAnnotation;

import java.io.Serializable;

@DbMainAnnotation(tableName = "contacts", keyFields = {"mobilenumber"})
public class Contact implements Serializable {


    public static enum Status { approve, approved, decline, delete, deleted, block, pending }

    @DbAnnotation
    String  username,
            mobilenumber,
            yourstatus,
            contact_status,
            contactname;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMobilenumber() {

        return mobilenumber;
        //return "7805642555";
    }

    public void setMobilenumber(String mobilenumber) {
        this.mobilenumber = mobilenumber;
    }

    public String getYourstatus() {
        return yourstatus;
    }

    public void setYourstatus(String yourstatus) {
        this.yourstatus = yourstatus;
    }

    public String getContact_status() {
        return contact_status;
    }

    public void setContact_status(String contact_status) {
        this.contact_status = contact_status;
    }

    public String getContactname() {
        return contactname;
    }

    public void setContactname(String contactname) {
        this.contactname = contactname;
    }


    @Override
        public boolean equals(Object o) {
            return (o instanceof Contact) && equals((Contact) o);
        }

        private boolean equals(Contact contact) {
            return !android.text.TextUtils.isEmpty(contact.getMobilenumber()) && contact.getMobilenumber().equals(getMobilenumber());
        }
}

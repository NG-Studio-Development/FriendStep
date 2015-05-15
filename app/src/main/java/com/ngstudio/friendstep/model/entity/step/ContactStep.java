package com.ngstudio.friendstep.model.entity.step;

import com.alexutils.annotations.DbAnnotation;
import com.alexutils.annotations.DbMainAnnotation;

import java.io.Serializable;

@DbMainAnnotation(tableName = "ContactStep", keyFields = {"id"})
public class ContactStep implements Serializable {

    public static enum Status { approve, approved, decline, delete, deleted, block, pending }

    @DbAnnotation
    public long id;

    @DbAnnotation
    public String name,
                  yourstatus,
                  contact_status;

    double latitude, longitude;

    public String getName() {
        return this.name;
    }
    public long getId() {
        return this.id;
    }

    public String getYourstatus() {
        return yourstatus;
    }

    public String getContact_status() {
        return contact_status;
    }

    public boolean isStatus(String status) {
        return contact_status.equals(status);
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof ContactStep) && equals((ContactStep) o);
    }

    private boolean equals(ContactStep contact) {
        //return !android.text.TextUtils.isEmpty(contact.getMobilenumber()) && contact.getMobilenumber().equals(getMobilenumber());
        return this.id == contact.getId();
    }

}

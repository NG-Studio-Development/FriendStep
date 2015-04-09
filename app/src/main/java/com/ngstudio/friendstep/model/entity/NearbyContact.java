package com.ngstudio.friendstep.model.entity;


public class NearbyContact {

    String name, mobilenumber;

    double latitude, longitude;

    long loc_time;
    double dist;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobilenumber() {
        return mobilenumber;
    }

    public void setMobilenumber(String mobilenumber) {
        this.mobilenumber = mobilenumber;
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

    public double getDist() {
        return dist;
    }

    public void setDist(double dist) {
        this.dist = dist;
    }

    public long getLoc_time() {
        return loc_time;
    }

    public void setLoc_time(long loc_time) {
        this.loc_time = loc_time;
    }
}

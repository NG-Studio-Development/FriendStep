package com.ngstudio.friendstep.model.entity.step;

public class User {
    long id;
    String regId;
    String name;
    String email;
    String pass;

    public long getId() { return id; }
    public String getGcmId() { return regId; }
    public String getNickName() { return name; }
    public String getEmail() { return email; }
    public String getPass() { return pass; }
}

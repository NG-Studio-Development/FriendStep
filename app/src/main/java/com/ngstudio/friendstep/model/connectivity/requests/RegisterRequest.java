package com.ngstudio.friendstep.model.connectivity.requests;

import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RegisterRequest extends BaseAppRequest {

    @RequestField(type = RequestType.POST)
    private String username, phonenumber, regnumber, countrycode;

    @RequestField(type = RequestType.POST)
    private boolean proximityalert;

    @RequestField(type = RequestType.POST)
    private int proximityalertdistance;

    @RequestField(type = RequestType.POST)
    private final String phonetype = "Android";

    @RequestField(type = RequestType.POST)
    private String defaultlanguage;

    public RegisterRequest(@NotNull String uuid, @NotNull String username, @NotNull String countrycode, @NotNull String phonenumber, @Nullable String regnumber, boolean proximityalert,
                           @MagicConstant(intValues = {50,100,500,1000})int proximityalertdistance, @NotNull String defaultlanguage) {
        super("register.php", uuid);
        this.username = username;
        this.phonenumber = phonenumber;
        this.regnumber = regnumber;
        this.countrycode = countrycode;
        this.proximityalert = proximityalert;
        this.proximityalertdistance = proximityalertdistance;
        this.defaultlanguage = defaultlanguage;
    }

}

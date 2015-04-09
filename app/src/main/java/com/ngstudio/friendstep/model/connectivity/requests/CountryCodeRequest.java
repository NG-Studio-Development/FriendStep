package com.ngstudio.friendstep.model.connectivity.requests;


import org.jetbrains.annotations.NotNull;

public class CountryCodeRequest extends BaseAppRequest {

    @RequestField(type = RequestType.POST)
    String countrycode;

    public CountryCodeRequest(@NotNull String uuid, String countrycode) {
        super("getcountrycode.php", uuid);
        this.countrycode = countrycode;
    }
}

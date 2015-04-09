package com.ngstudio.friendstep.model.connectivity.requests;

import android.location.Location;

import com.ngstudio.friendstep.components.CustomLocationManager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InsertGeoCordsRequest extends BaseAppRequest {


    @RequestField(type = RequestType.POST)
    private double longitude = CustomLocationManager.LAT_LON_DEFAULT, latitude = CustomLocationManager.LAT_LON_DEFAULT, altitude;

    @RequestField(type = RequestType.POST)
    private float horizontalaccuracy, verticalaccuracy, speed, course;

    @RequestField(type = RequestType.POST)
    private long time_stamp;

    @RequestField(type = RequestType.POST)
    private String status = "OK";

    public InsertGeoCordsRequest(@NotNull String uuid, @Nullable Location location) {
        super("insgeocords.php", uuid);

        if(location == null)
            return;

        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        this.altitude = location.getAltitude();
        this.horizontalaccuracy = location.getAccuracy();
        this.verticalaccuracy = location.getAccuracy();
        this.time_stamp = location.getTime() / 1000;
        this.speed = location.getSpeed();
    }

}

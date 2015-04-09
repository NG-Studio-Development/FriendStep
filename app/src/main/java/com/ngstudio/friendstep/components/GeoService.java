package com.ngstudio.friendstep.components;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.ngstudio.friendstep.utils.SettingsHelper;


public class GeoService extends Service {

    private static final String TAG = "GeoService";

    public static final int MIN_DISTANCE = 20;
    public static final int MIN_TIME = 30 * 1000;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG,"geo service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG,"geo service started");
        if(SettingsHelper.getInstance().getStateSendLocation())
            CustomLocationManager.getInstance().beginTrackLocation(MIN_TIME,MIN_DISTANCE,false);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

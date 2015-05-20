package com.ngstudio.friendstep.utils;

import android.content.SharedPreferences;

import com.ngstudio.friendstep.CodesMap;
import com.ngstudio.friendstep.WhereAreYouApplication;

import java.util.HashMap;
import java.util.Map;

public class SettingsHelper {

    //private static SettingsHelper instance;
    private static Map<Integer,Integer> distentionsMap;
    public static SettingsHelper getInstance() {
        return SettingsHolder.HOLDER_INSTANCE;
    }

    public static class SettingsHolder {
        public static final SettingsHelper HOLDER_INSTANCE = new SettingsHelper();
    }

    private SettingsHelper() {
        distentionsMap = new HashMap<>();
        distentionsMap.put(DISTANCE_50,   50);
        distentionsMap.put(DISTANCE_100,  100);
        distentionsMap.put(DISTANCE_500,  500);
        distentionsMap.put(DISTANCE_1000, 1000);

        settings = WhereAreYouApplication.getInstance().getApplicationPreferences();   //PreferenceManager.getDefaultSharedPreferences(activity);
        edit = WhereAreYouApplication.getInstance().getApplicationPreferencesEditor(); //settings.edit();
    }

    public static final int DISTANCE_50 = 0;
    public static final int DISTANCE_100 = 1;
    public static final int DISTANCE_500 = 2;
    public static final int DISTANCE_1000 = 3;

    public static final String SEND_LOCATION = "send_location";
    public static final String DISTANCE = "distance";
    public static final String LANGUAGE = "language";

    private boolean defaultStateSendLocation = true;
    private int defaultDistance = DISTANCE_50;
    private String defaultLanguage = CodesMap.getDefaultLanguageKey();

    SharedPreferences.Editor edit;
    SharedPreferences settings;


    public boolean getStateSendLocation() {
        return settings.getBoolean(SEND_LOCATION, defaultStateSendLocation);
    }

    public void putStateSendLocation(boolean stateSendLocation) {
        edit.putBoolean(SEND_LOCATION, stateSendLocation).commit();
    }

    public int getDistanceKey() {
        return settings.getInt(DISTANCE, defaultDistance);
    }

    public void putDistanceKey(int distanceState) {
        edit.putInt(DISTANCE, distanceState).commit();
    }

    public String getLanguage() {
        return settings.getString(LANGUAGE, defaultLanguage);
    }

    public void setLanguage(String language) {
        edit.putString(LANGUAGE, language).commit();
    }

    public  int getDistance() {
        return distentionsMap.get(getDistanceKey());
    }
}

package com.ngstudio.friendstep.utils;


public class WhereAreYouAppConstants {

    public static final String KEY_APPLICATION_VERSION   = "application::VersionNumber";
    public static final String KEY_APPLICATION_GCM_REGID = "application::GCMRegID";

    private static final String PROD_SERVER_ULR = "http://prod.way-app.net/";
    private static final String DEV_SERVER_URL = "http://www.geo-mobile.net/way/";

    public static final String BASE_SERVER_URL = PROD_SERVER_ULR;
    public static final String STEP_SERVER_URL = "http://akimovdev.temp.swtest.ru/server_v2/";
    public static final String SERVER_APP_NAME = "WhereAreYou";

    public static final int NOTIFICATION_INSERT_GEO_CORDS_SUCCESS = 1;
    public static final int NOTIFICATION_INSERT_GEO_CORDS_ERROR = 2;
    public static final int NOTIFICATION_CONTACTS_LOADED = 3;
    public static final int NOTIFICATION_CONTACTS_LOCATIONS_LOADED = 4;
    public static final int NOTIFICATION_USER_AVATAR_LOADED = 5;
    public static final int NOTIFICATION_CONTACTS_NEARBY = 7;
    public static final int NOTIFICATION_CONTACTS_LOCATION = 8;
    public static final int NOTIFICATION_MESSAGES = 9;
    public static final int NOTIFICATION_MESSAGE_INCOMING = 10;
    public static final int NOTIFICATION_REQUEST_REPLY = 11;
    public static final int NOTIFICATION_CONTACTS_FIND_BY_NAME = 12;

    public static final String SENDER_ID = "663182830836";

    public static final String SINGLETON_WARNING_INITIALIZED_ALREADY = " has been initialized already!";
    public static final String SINGLETON_WARNING_NOT_INITIALIZED_YET = " hasn't been initialized yet!";

    public static final String SHARED_PREFERENCE_NAME = "prefs";

    public static final String PREF_KEY_COUNTRY_NAME = "country_name";
    public static final String PREF_KEY_COUNTRY_CODE = "country_code";
    public static final String PREF_KEY_IS_LOGGED_IN = "is_logged_in";
    public static final String PREF_KEY_EMAIL = "pref_key_email";
    public static final String PREF_KEY_NAME = "name";
    public static final String PREF_KEY_ID_USER = "pref_key_id_user";
    public static final String PREF_KEY_GCM_ID_USER = "pref_key_id_user_gcm";
    public static final String PREF_KEY_SHOULD_TACK_LOCATION = "should_track_location";

    public static final String FOLDER_AVATARS = "avatars";

    public static final String KEY_CONTACT = "key_contact";

    public static String SERVER_KEY_MESSAGE = "price";
    public static String SERVER_KEY_FROM_ID = "from_id";
    public static final String SERVER_KEY_FROM_NAME = "from_name";
    public static String SERVER_KEY_MESS_TIME = "mess_time";
}

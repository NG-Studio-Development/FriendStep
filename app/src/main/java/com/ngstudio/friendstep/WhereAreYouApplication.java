package com.ngstudio.friendstep;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.alexutils.dao.GenericDao;
import com.alexutils.utility.FileUtils;
import com.ngstudio.friendstep.components.CustomLocationManager;
import com.ngstudio.friendstep.components.cache.AvatarBase64ImageDownloader;
import com.ngstudio.friendstep.components.cache.RoundImageDecoder;
import com.ngstudio.friendstep.components.database.DbHelper;
import com.ngstudio.friendstep.model.connectivity.BaseResponseCallback;
import com.ngstudio.friendstep.model.connectivity.HttpServer;
import com.ngstudio.friendstep.model.connectivity.NetworkExecutor;
import com.ngstudio.friendstep.model.connectivity.requests.CountryCodeRequest;
import com.ngstudio.friendstep.model.entity.Contact;
import com.ngstudio.friendstep.model.entity.Message;
import com.ngstudio.friendstep.model.entity.step.ContactStep;
import com.ngstudio.friendstep.ui.activities.MainActivity;
import com.ngstudio.friendstep.utils.CommonUtils;
import com.ngstudio.friendstep.utils.WhereAreYouAppConstants;
import com.ngstudio.friendstep.utils.WhereAreYouAppLog;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.MemoryCacheUtil;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.security.NoSuchAlgorithmException;


public class WhereAreYouApplication extends Application {

    private volatile static WhereAreYouApplication instance;
    private SharedPreferences applicationPreferences;

    public SharedPreferences getApplicationPreferences() {
        return applicationPreferences;
    }

    private SharedPreferences.Editor applicationPreferencesEditor;

    public SharedPreferences.Editor getApplicationPreferencesEditor() {
        return applicationPreferencesEditor;
    }

    ImageLoader avatarCache;

    public ImageLoader getAvatarCache() {
        return avatarCache;
    }

    public static int getVersion() {
        final Context context = getInstance();
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);

            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        synchronized (WhereAreYouApplication.class) {
            if (BuildConfig.DEBUG) {
                if (instance != null)
                    throw new RuntimeException("Something strange: there is another application instance.");
            }
            instance = this;

            WhereAreYouApplication.class.notifyAll();
        }

        /*GoogleCloudMessagingClient.init(getVersion(), new GoogleCloudMessagingClient.Delegate() {
            @NotNull
            @Override
            public SharedPreferences getGCMPreferences(int accessMode) {
                return getSharedPreferences(WhereAreYouAppConstants.SHARED_PREFERENCE_NAME,MODE_PRIVATE);
            }

            @NotNull
            @Override
            public GoogleCloudMessaging getGCMInstance() {
                return GoogleCloudMessaging.getInstance(WhereAreYouApplication.this);
            }

            @Override
            public void sendGCMRegistrationIdToBackend(@NotNull final String regID) {
                RegisterApiRequest registerApiRequest = new RegisterApiRequest(WhereAreYouApplication.getInstance().getUuid(),regID);
                HttpServer.submitToServer(registerApiRequest, new BaseResponseCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        Log.d("test!","rgister = " + result);
                    }

                    @Override
                    public void onError(Exception error) {

                    }
                });
            }
        }); */


        NetworkExecutor.init();
        CustomLocationManager.init(this);
        FileUtils.init(this);
        initDb();
        WhereAreYouAppLog.enableLogging(true);
        applicationPreferences = getSharedPreferences(WhereAreYouAppConstants.SHARED_PREFERENCE_NAME,MODE_PRIVATE);
        applicationPreferencesEditor = applicationPreferences.edit();
        initParams();
    }

    private void initDb() {
        DbHelper helper = DbHelper.getInstance();

        GenericDao.init(helper,Contact.class);
        GenericDao.init(helper, ContactStep.class);
        GenericDao.init(helper, Message.class);
    }

    private String currentCountry;

    public String getCurrentCountry() {
        return currentCountry;
    }

    private int currentCountryCode;

    public int getCurrentCountryCode() {
        return currentCountryCode;
    }

    private String uuid;

    public String getUuid() {
        //return uuid;
        return "358240058006297";
        //return "355765056322315";
    }

    public long getUserId() {
        long userId = getApplicationPreferences().getLong(WhereAreYouAppConstants.PREF_KEY_ID_USER,-1);
        if(userId == -1)
            throw new Error("Not found id user in preferences");
        return userId;
    }

    public String getUserName() {
        String userName = getApplicationPreferences().getString(WhereAreYouAppConstants.PREF_KEY_NAME, null);
        if(userName == null)
            throw new Error("Not found name user in preferences");
        return userName;
    }

    public String getUserEmail() {
        String userName = getApplicationPreferences().getString(WhereAreYouAppConstants.PREF_KEY_EMAIL, null);
        if(userName == null)
            throw new Error("Not found name user in preferences");
        return userName;
    }

    public boolean getIsFriendLoadedInMap() {
      return isFriendLoaded;
    }

    public void  setFriendLoadedInMap(boolean isFriendLoaded) {
        this.isFriendLoaded = isFriendLoaded;
    }

    private static boolean isFriendLoaded = false; // Temp variable, write to anywhere later

    /*private String currentMobile = "380955841708";
    //private String currentMobile = "987654321";

    public String getCurrentMobile() {
        return currentMobile;
    }

    public void setCurrentMobile(String currentMobile) {
        this.currentMobile = currentMobile;
    } */

    private String currentName;

    public String getCurrentName() {
        return this.currentName;
    }

    public void setCurrentName(String currentName) {
        this.currentName = currentName;
    }

    private void initParams() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        uuid = telephonyManager.getDeviceId();
        if(TextUtils.isEmpty(uuid)) {
            uuid = Settings.Secure.getString(getContentResolver(),
                    Settings.Secure.ANDROID_ID);
        }

        currentCountry = telephonyManager.getSimCountryIso();

        if(currentCountry != null && currentCountry.equals(applicationPreferences.getString(WhereAreYouAppConstants.PREF_KEY_COUNTRY_NAME,null))) {
            currentCountryCode = applicationPreferences.getInt(WhereAreYouAppConstants.PREF_KEY_COUNTRY_CODE,0);

        } else if(currentCountry != null) {
            CountryCodeRequest countryCodeRequest = new CountryCodeRequest(getUuid(),currentCountry.toUpperCase());
            HttpServer.submitToServer(countryCodeRequest, new BaseResponseCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    if(TextUtils.isEmpty(result))
                        return;

                    try {
                        JSONArray array = new JSONArray(result);
                        currentCountryCode = array.getJSONObject(0).getInt("mobile_code");
                        applicationPreferencesEditor.putString(WhereAreYouAppConstants.PREF_KEY_COUNTRY_NAME,currentCountry);
                        applicationPreferencesEditor.putInt(WhereAreYouAppConstants.PREF_KEY_COUNTRY_CODE,currentCountryCode);
                        applicationPreferencesEditor.commit();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(Exception error) {

                }
            });
        } else {
            currentCountry = "us";
            currentCountryCode = 1;
        }

        avatarCache = new ImageLoader();

        avatarCache.init(new ImageLoaderConfiguration.Builder(this)
                .defaultDisplayImageOptions(new DisplayImageOptions.Builder().cacheInMemory(true)
                        .showImageForEmptyUri(R.drawable.ava)
                        .showImageOnLoading(R.drawable.ava)
                        .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                        .resetViewBeforeLoading(true)
                        .cacheOnDisc(true)
                        .build())

                .imageDecoder(new RoundImageDecoder(true))
                .imageDownloader(new AvatarBase64ImageDownloader(this))
                .discCache(new UnlimitedDiscCache(getCacheDir()))
                .denyCacheImageMultipleSizesInMemory()
                .memoryCacheSizePercentage(25)
                .build());

        avatarCache.handleSlowNetwork(true);
    }

    @NotNull
    @SuppressWarnings({"ConstantConditions", "unchecked"})
    public static WhereAreYouApplication getInstance() {
        WhereAreYouApplication application = instance;
        if (application == null) {
            synchronized (WhereAreYouApplication.class) {
                if (instance == null) {
                    if (BuildConfig.DEBUG) {
                        if (Thread.currentThread() == Looper.getMainLooper().getThread())
                            throw new UnsupportedOperationException(
                                    "Current application's instance has not been initialized yet (wait for onCreate, please).");
                    }
                    try {
                        do {
                            WhereAreYouApplication.class.wait();
                        } while ((application = instance) == null);
                    } catch (InterruptedException e) {
                        /* Nothing to do */
                    }
                }
            }
        }
        return application;
    }

    public static void softInputMethodStateManage(@NotNull View v, boolean isVisible) {
        final InputMethodManager imm = ((InputMethodManager) getInstance().getSystemService(Context.INPUT_METHOD_SERVICE));

        if (isVisible) {
            v.requestFocus();
            imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
        } else {
            imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            v.clearFocus();
        }
    }


    public static void setComponentEnabledSetting(Class<?> componentClass, int newState, int flags) {
        final Context context = getInstance();

        PackageManager pm = context.getPackageManager();

        assert pm != null;
        pm.setComponentEnabledSetting(new ComponentName(context, componentClass), newState, flags);
    }

    public static String[] getStringArray(int resId) {
        return getInstance().getResources()
                .getStringArray(resId);
    }

    public static int getInteger(int resId) {
        return getInstance().getResources()
                .getInteger(resId);
    }


    private static final class MainThreadHandler {
        public static final Handler INSTANCE = new Handler(Looper.getMainLooper());
    }

    public static Handler getMainThreadHandler() {
        return MainThreadHandler.INSTANCE;
    }


    public static void runOnUiThread(@NotNull Runnable task, boolean tryImmediately) {
        if (tryImmediately && Thread.currentThread()
                .equals(Looper.getMainLooper().getThread())) {
            task.run();
            return;
        }
        getMainThreadHandler().post(task);
    }

    public static void runOnUiThread(@NotNull Runnable task) {
        getMainThreadHandler().post(task);
    }


    public static void runOnUiThread(@NotNull Runnable task, long delayMillis) {
        getMainThreadHandler().postDelayed(task, delayMillis);
    }

    private String activityVisible;
    private String topActivity;

    public boolean isActivityBackground() {
        return TextUtils.isEmpty(activityVisible);
    }

    public void activityResumed(@NotNull String activity) {
        activityVisible = activity;
        topActivity = activity;
    }

    public void activityPaused(@NotNull String activity) {
        if (activity.equals(activityVisible))
            activityVisible = null;
    }

    public String getTopActivity() {
        return topActivity;
    }

    public void checkForLocationServices(final @NotNull Activity hostActivity, final @NotNull Runnable task) {
        if (!CustomLocationManager.getInstance().checkDesiredLocationProvidersAreAvailable()) {
            new AlertDialog.Builder(hostActivity).setTitle(hostActivity.getString(R.string.notification_location_services_title))
                    .setMessage(hostActivity.getString(R.string.notification_location_services_message))
                    .setPositiveButton(getString(R.string.dialog_button_positive_generic), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            hostActivity.startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), MainActivity.REQUEST_CODE_ENABLE_GPS);
                        }
                    }).setNegativeButton(getString(R.string.dialog_button_negative_generic), null).show();
            return;
        }
        task.run();
    }


    public File getAvatarFile(String mobileNumber){
        try {
            return new File(FileUtils.getDir(WhereAreYouAppConstants.FOLDER_AVATARS), CommonUtils.calcHash(mobileNumber, "SHA-1"));
        } catch (NoSuchAlgorithmException e) {
            throw new Error(e);
        }
    }

    public Bitmap getAvatar(String mobileNumber) {
        File avatarFile = getAvatarFile(mobileNumber);
        if(avatarFile.exists()) {
            return BitmapFactory.decodeFile(avatarFile.getAbsolutePath());
        }

        return null;
    }

    public File getCurrentAvatarFile() {
        //return getAvatarFile(currentMobile);
        return getAvatarFile(currentName);
    }

    public Bitmap getCurrentUserAvatar() {

        //return getAvatar(currentMobile);
        return getAvatar(currentName);
    }

    public static String getPrefString(String key, String def) {
        return getInstance().getApplicationPreferences().getString(key,def);
    }

    public static void removeAvatarFromCache(@NotNull String mobileNumber) {
        String avatarUri = AvatarBase64ImageDownloader.getImageUriFor(mobileNumber);

        File imageFile = getInstance().getAvatarCache().getDiscCache().get(avatarUri);
        if (imageFile.exists()) {
            imageFile.delete();
        }
        MemoryCacheUtil.removeFromCache(avatarUri, getInstance().getAvatarCache().getMemoryCache());
    }

    //Notifications
    public static void showNotification(String title, String text, Class resultActivity, Bundle args, int notificationId) {
        int icon = R.drawable.ic_launcher;
        long when = System.currentTimeMillis();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(instance).
                setSmallIcon(icon).
                setContentTitle(title).
                setContentText(text).
                setWhen(when).
                setDefaults(Notification.DEFAULT_ALL);

        if (resultActivity != null) {
            Intent notificationIntent = new Intent(instance, resultActivity);
            notificationIntent.putExtras(args);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(instance);
            stackBuilder.addParentStack(resultActivity);
            stackBuilder.addNextIntent(notificationIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            builder.setContentIntent(resultPendingIntent);
        }

        NotificationManager mNotificationManager =
                (NotificationManager) instance.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(notificationId, builder.build());

    }

    public static void showNotification(String title, String text, Bitmap largeIcon, Uri sound, boolean needVibrate, Class resultActivity, Bundle args, int notificationId) {
        long when = System.currentTimeMillis();
        int smallIcon = R.drawable.ic_launcher;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(instance).
                setSmallIcon(smallIcon).
                setLargeIcon(largeIcon).
                setContentTitle(title).
                setContentText(text).
                setWhen(when).
                setSound(sound);

        if (resultActivity != null) {
            Intent notificationIntent = new Intent(instance, resultActivity);
            notificationIntent.putExtras(args);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(instance);
            stackBuilder.addParentStack(resultActivity);
            stackBuilder.addNextIntent(notificationIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            builder.setContentIntent(resultPendingIntent);
        }

        NotificationManager mNotificationManager =
                (NotificationManager) instance.getSystemService(Context.NOTIFICATION_SERVICE);

        if (needVibrate) {
            builder.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE);
        } else {
            builder.setDefaults(Notification.DEFAULT_LIGHTS);
            builder.setVibrate(new long[]{0});
        }

        mNotificationManager.notify(notificationId, builder.build());
    }
}

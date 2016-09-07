package com.ngstudio.friendstep.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.ngstudio.friendstep.R;
import com.ngstudio.friendstep.WhereAreYouApplication;
import com.ngstudio.friendstep.model.connectivity.BaseResponseCallback;
import com.ngstudio.friendstep.model.connectivity.HttpServer;
import com.ngstudio.friendstep.model.connectivity.requests.stepserver.RegisterRequestStepServer;
import com.ngstudio.friendstep.model.entity.step.User;
import com.ngstudio.friendstep.ui.fragments.LoginFragment;
import com.ngstudio.friendstep.utils.InputValidationUtils;
import com.ngstudio.friendstep.utils.WhereAreYouAppConstants;
import com.ngstudio.friendstep.utils.WhereAreYouAppLog;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;



public class LoginActivity extends BaseActivity {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    public static final String SAVED_NUMBER = "saved_number";
    public static final String SAVED_NAME = "saved_name";

    private static final String START_TYPE = "start_type";
    public static final int TYPE_SIGN_IN = 0;
    public static final int TYPE_REGISTER = 1;


    @Override
    protected int getFragmentContainerId() {
        return R.id.container;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        checkPlayServices();

        boolean isLoggedIn = WhereAreYouApplication.getInstance().getApplicationPreferences().getBoolean(WhereAreYouAppConstants.PREF_KEY_IS_LOGGED_IN,false);

        if (savedInstanceState == null && !isLoggedIn) {
            addFragment(new LoginFragment(),false);
        } else if(isLoggedIn) {
            WhereAreYouApplication.getInstance().setCurrentName(WhereAreYouApplication.getPrefString(WhereAreYouAppConstants.PREF_KEY_NAME,"380955941708"));
            startMainActivity();
        }
    }

   private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (!isGooglePlayServicesAvailable()) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                WhereAreYouAppLog.e(getString(R.string.toast_not_supported_google_services));
                finish();
            }
            return false;
        }
        return true;
   }

    public boolean isGooglePlayServicesAvailable() {
        return GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS;
    }

    public void startMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }


    public void signInListener(String name, String pass) {
        if (hasConnection(this))
            registerInBackground(name, null, pass, TYPE_SIGN_IN);
        else
            Toast.makeText(this, getString(R.string.no_internet_connection),Toast.LENGTH_LONG).show();
    }

    public void registerListener(final String username,final String email, final String pass, String repass) {

        if(!InputValidationUtils.checkNonEmptyFieldWithToast(this, username, getString(R.string.text_username)) ||
                !InputValidationUtils.checkEmailWithToast(this, email) ||
                !InputValidationUtils.checkPasswordsWithToast(this, pass,repass) ) {

            return;
        }

        if(!hasConnection(this)) {
            Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isGooglePlayServicesAvailable()) {
            Toast.makeText(this, R.string.toast_not_supported_google_services, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        registerInBackground(username, email, pass, TYPE_REGISTER);
    }


    GoogleCloudMessaging gcm;
    String regId;
    String SENDER_ID = "197291868967";

    public void registerInBackground(final String name, final String email, final String pass, final int startType) {
        new AsyncTask<Void, String, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    if (gcm == null)
                        gcm = GoogleCloudMessaging.getInstance(LoginActivity.this);

                    gcm.unregister();
                    regId = gcm.register(SENDER_ID);
                    storeRegistrationId(LoginActivity.this, regId);
                } catch (IOException ex) {
                    return "Error :" + ex.getMessage();
                }
                sendRegistrationIdToBackend(name, email, pass, regId, startType);
                return regId;
            }

            @Override
            protected void onPostExecute(String result) {

                if (result.contains("Error")) {
                    Log.d("REGISTER API", result);
                    return;
                }
            }
        }.execute(null, null, null);
    }

    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";

    private SharedPreferences getGCMPreferences(Context context) {
        return context.getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    private void sendRegistrationIdToBackend(String name, String email, String pass, String regId, int startType) {
        if (startType == TYPE_REGISTER)
            registerAction(name, email, pass);
        else if(startType == TYPE_SIGN_IN)
            logInAction(name, pass, regId);
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private void registerAction(String name, String email, String pass) {
        URI url = null;
        String result = null;
        try {
            url = new URI("http://akimovdev.temp.swtest.ru/server_v2/register.php?regId=" + regId+"&name="+name+"&email="+email+"&pass="+pass);
        } catch (URISyntaxException e) {
            e.printStackTrace(); }

        HttpClient httpclient = new DefaultHttpClient();
        HttpGet request = new HttpGet();
        request.setURI(url);
        try {
            result = getQueryResult(httpclient.execute(request));

            if(!result.contains("Error"))
                saveEnterDataFromJson(result);

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getQueryResult(HttpResponse response) throws IOException{
        BufferedReader in = new BufferedReader(new InputStreamReader(
                response.getEntity().getContent()));

        String result = null;
        String line = null;
        while ((line = in.readLine()) != null) {
            result = result == null ? line : result+line;
        }

        return result;
    }

    private void logInAction(String name, String pass, String regId) {

        RegisterRequestStepServer request = RegisterRequestStepServer.requestSignIn(name, pass, regId);
        HttpServer.submitToServer(request, new BaseResponseCallback<String>() {
            @Override
            public void onSuccess(String result) {

                if (result.contains("ERROR"))
                    Toast.makeText(LoginActivity.this, R.string.notis_authorization_incorrect_data, Toast.LENGTH_SHORT).show();
                else
                    saveEnterDataFromJson(result);
            }

            @Override
            public void onError(Exception error) {
                error.printStackTrace();
            }
        });
    }

    private void saveEnterDataFromJson(String stringJson) {
        Gson gson = new Gson();
        User user = gson.fromJson(stringJson, User.class);
        saveEnterData(user);
    }

    void saveEnterData(User user) {
        WhereAreYouApplication.getInstance().getApplicationPreferencesEditor().putBoolean(WhereAreYouAppConstants.PREF_KEY_IS_LOGGED_IN,true);
        WhereAreYouApplication.getInstance().getApplicationPreferencesEditor().putString(WhereAreYouAppConstants.PREF_KEY_EMAIL, user.getEmail());
        WhereAreYouApplication.getInstance().getApplicationPreferencesEditor().putString(WhereAreYouAppConstants.PREF_KEY_NAME,user.getNickName());
        WhereAreYouApplication.getInstance().getApplicationPreferencesEditor().putLong(WhereAreYouAppConstants.PREF_KEY_ID_USER,user.getId());
        WhereAreYouApplication.getInstance().getApplicationPreferencesEditor().putString(WhereAreYouAppConstants.PREF_KEY_GCM_ID_USER,user.getGcmId());
        WhereAreYouApplication.getInstance().getApplicationPreferencesEditor().commit();
        startMainActivity();
    }

    public boolean hasConnection(final Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (wifiInfo != null && wifiInfo.isConnected()) {
            return true;
        }

        wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifiInfo != null && wifiInfo.isConnected()) {
            return true;
        }

        wifiInfo = cm.getActiveNetworkInfo();
        if (wifiInfo != null && wifiInfo.isConnected()) {
            return true;
        }

        return false;
    }

}

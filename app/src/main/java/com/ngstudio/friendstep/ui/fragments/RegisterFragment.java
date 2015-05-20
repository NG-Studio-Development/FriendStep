package com.ngstudio.friendstep.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.ngstudio.friendstep.R;
import com.ngstudio.friendstep.WhereAreYouApplication;
import com.ngstudio.friendstep.model.connectivity.BaseResponseCallback;
import com.ngstudio.friendstep.model.connectivity.HttpServer;
import com.ngstudio.friendstep.model.connectivity.requests.stepserver.RegisterRequestStepServer;
import com.ngstudio.friendstep.model.entity.step.User;
import com.ngstudio.friendstep.ui.activities.LoginActivity;
import com.ngstudio.friendstep.ui.activities.MainActivity;
import com.ngstudio.friendstep.utils.InputValidationUtils;
import com.ngstudio.friendstep.utils.WhereAreYouAppConstants;

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

public class RegisterFragment extends BaseFragment<LoginActivity> {

    public static final String SAVED_NUMBER = "saved_number";
    public static final String SAVED_NAME = "saved_name";

    private static final String START_TYPE = "start_type";
    private static final int TYPE_SIGN_IN = 0;
    private static final int TYPE_REGISTER = 1;

    private EditText etEmail, etUserName;
    private EditText etPass;
    private EditText etRepass;
    private TextView tvClearInfo;
    private Button buttonRegister;
    private int startType;
    private Context context;

    String regId;

    public static RegisterFragment newRegisterInstance() {
        return newInstance(TYPE_REGISTER);
    }

    public static RegisterFragment newSignInInstance() {
        return newInstance(TYPE_SIGN_IN);
    }

    private static RegisterFragment newInstance(int startType) {
        RegisterFragment fragment = new RegisterFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(START_TYPE,startType);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getHostActivity().getApplicationContext();
        if (getArguments() != null ) {
            startType = getArguments().getInt(START_TYPE);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        TextView text = new TextView(activity);
        int padding = getResources().getDimensionPixelSize(R.dimen.margin_widget_default_small);
        text.setPadding(padding,padding,padding,padding);
        text.setText(R.string.content_pin_type);
        text.setTextColor(Color.WHITE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(getLayoutResID(), container, false);

        tvClearInfo = (TextView ) view.findViewById(R.id.tvClearInfo);
        tvClearInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearInfo();
            }
        });

        etPass = (EditText) view.findViewById(R.id.edPass);
        etRepass = (EditText) view.findViewById(R.id.edRepass);
        etEmail = (EditText) view.findViewById(R.id.etEmail);
        etUserName = (EditText) view.findViewById(R.id.etUserName);
        buttonRegister = (Button) view.findViewById(R.id.buttonRegister);

        String textLoginOrRegButton = startType == TYPE_REGISTER ? getString(R.string.text_button_register):getString(R.string.text_button_sign_in);

        buttonRegister.setText(textLoginOrRegButton);
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startType == TYPE_REGISTER)
                    registerListener();
                else if (startType == TYPE_SIGN_IN)
                    signInListener(etUserName.getText().toString(),etPass.getText().toString());
            }
        });

        if(startType == TYPE_SIGN_IN) {
            etEmail.setVisibility(View.GONE);
            etRepass.setVisibility(View.GONE);
        }

        loadEnterData();
        return view;
    }

    private void registerListener() {
        final String username = etUserName.getText().toString();
        final String email = etEmail.getText().toString();
        final String pass = etPass.getText().toString();

        if(!InputValidationUtils.checkNonEmptyFieldWithToast(getActivity(), username, getString(R.string.text_username)) ||
                !InputValidationUtils.checkEmailWithToast(context, email) ||
                !InputValidationUtils.checkPasswordWithToast(context, pass) ) {

            Toast.makeText(getActivity(), "HARD error in enter data", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!hasConnection(getActivity())) {
            Toast.makeText(getActivity(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!getHostActivity().isGooglePlayServicesAvailable()) {
            Toast.makeText(getActivity(), R.string.toast_not_supported_google_services, Toast.LENGTH_SHORT).show();
            getHostActivity().finish();
            return;
        }

        registerInBackground(username, email, pass);
    }

    private void signInListener(String name, String pass) {
        registerInBackground(name,null,pass);

        /*RegisterRequestStepServer request = RegisterRequestStepServer.requestSignIn(name, pass);
        HttpServer.submitToServer(request, new BaseResponseCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d("RESULT","Result = "+result);
                saveEnterDataFromJson(result);
            }

            @Override
            public void onError(Exception error) {}
        });*/

    }

    GoogleCloudMessaging gcm;
    private void registerInBackground(final String name, final String email, final String pass) {
        new AsyncTask<Void, String, String>() {
            @Override
            protected String doInBackground(Void... params) {
                //gcm = GoogleCloudMessaging.getInstance(getHostActivity());
                //String result = null;
                try {
                    if (gcm == null)
                        gcm = GoogleCloudMessaging.getInstance(context);

                    gcm.unregister();
                    regId = gcm.register(SENDER_ID);
                    //result = sendRegistrationIdToBackend(name,email,pass);
                    storeRegistrationId(context, regId);
                } catch (IOException ex) {
                    return "Error :" + ex.getMessage();
                }
                sendRegistrationIdToBackend(name, email, pass, regId);
                return regId;
            }

            @Override
            protected void onPostExecute(String result) {

                if (result.contains("Error")) {
                    Log.d("REGISTER API",result);
                    return;
                }

                Toast.makeText(context,"REGISTRATION IN BACKGROUND IS SUCCESSFULL !!!", Toast.LENGTH_LONG).show();
                //sendRegistrationIdToBackend(name,email,pass,result);
                //saveEnterDataFromJson(result);
            }
       }.execute(null, null, null);
    }

    String SENDER_ID = "197291868967";

    private void saveEnterDataFromJson(String stringJson) {
        Gson gson = new Gson();
        User user = gson.fromJson(stringJson, User.class);
        saveEnterData(user);
    }

    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");

        if (registrationId.isEmpty()) {
            Log.i("GET_REGISTRATION_ID", "Registration not found.");
            return "";
        }

        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);

        if (registeredVersion != currentVersion) {
            Log.i("GET_REGISTRATION_ID", "App version changed.");
            return "";
        }

        return registrationId;
    }

    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";


    private SharedPreferences getGCMPreferences(Context context) {
        return context.getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
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

    private void /*String*/ sendRegistrationIdToBackend(String name, String email, String pass, String regId) {

        if (startType == TYPE_REGISTER)
            regNewUserDEBUG(name, email, pass);
        else if(startType == TYPE_SIGN_IN)
            signInUserDEBUG(name, pass, regId);
        //return result;
    }


    private void regNewUserDEBUG(String name, String email, String pass) {
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
            if(!result.contains("Error")) {
                saveEnterDataFromJson(result);
            }

                Log.d("REGISTRATION_BACKEND","Response = "+result);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void signInUserDEBUG(String name, String pass, String regId) {

        RegisterRequestStepServer request = RegisterRequestStepServer.requestSignIn(name, pass, regId);
        HttpServer.submitToServer(request, new BaseResponseCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d("RESULT","Result = "+result);
                saveEnterDataFromJson(result);
            }

            @Override
            public void onError(Exception error) {
                error.printStackTrace();
                Log.d("RESULT","Result = "+error.getMessage());
            }
        });
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

    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        //Log.i("STORE_REGISTRATION_ID", "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
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

    void saveEnterData(User user/*String username, String email, String pass*/) {
        WhereAreYouApplication.getInstance().getApplicationPreferencesEditor().putBoolean(WhereAreYouAppConstants.PREF_KEY_IS_LOGGED_IN,true);
        WhereAreYouApplication.getInstance().getApplicationPreferencesEditor().putString(WhereAreYouAppConstants.PREF_KEY_EMAIL, user.getEmail());
        WhereAreYouApplication.getInstance().getApplicationPreferencesEditor().putString(WhereAreYouAppConstants.PREF_KEY_NAME,user.getNickName());
        WhereAreYouApplication.getInstance().getApplicationPreferencesEditor().putLong(WhereAreYouAppConstants.PREF_KEY_ID_USER,user.getId());
        WhereAreYouApplication.getInstance().getApplicationPreferencesEditor().putString(WhereAreYouAppConstants.PREF_KEY_GCM_ID_USER,user.getGcmId());
        WhereAreYouApplication.getInstance().getApplicationPreferencesEditor().commit();
        getHostActivity().startMainActivity();
    }

    void loadEnterData() {
        String number = getActivity().getPreferences(Context.MODE_PRIVATE).getString(SAVED_NUMBER, "");
        String name = getActivity().getPreferences(Context.MODE_PRIVATE).getString(SAVED_NAME, "");
        if (!number.isEmpty()) {
            etEmail.setText(number);
            etUserName.setText(name);
        }
    }

    void clearInfo() {
        etEmail.getText().clear();
        etUserName.getText().clear();
        etPass.getText().clear();
    }

    @Override
    public int getLayoutResID() {
        return R.layout.fragment_register;
    }
}

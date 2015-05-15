package com.ngstudio.friendstep.ui.activities;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.ngstudio.friendstep.R;
import com.ngstudio.friendstep.WhereAreYouApplication;
import com.ngstudio.friendstep.ui.fragments.MenuLoginFragment;
import com.ngstudio.friendstep.utils.WhereAreYouAppConstants;
import com.ngstudio.friendstep.utils.WhereAreYouAppLog;

public class LoginActivity extends BaseActivity {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    @Override
    protected int getFragmentContainerId() {
        return R.id.container;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        checkPlayServices();

        boolean isLoggedIn = WhereAreYouApplication.getInstance().getApplicationPreferences().getBoolean(WhereAreYouAppConstants.PREF_KEY_IS_LOGGED_IN,false);

        if (savedInstanceState == null && !isLoggedIn) {
            addFragment(new MenuLoginFragment(),false);
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

}

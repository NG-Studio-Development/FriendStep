package com.ngstudio.friendstep.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.TextView;

import com.ngstudio.friendstep.R;
import com.ngstudio.friendstep.model.entity.ContactLocation;
import com.ngstudio.friendstep.ui.fragments.IncomingRequestFragment;
import com.ngstudio.friendstep.ui.fragments.MapProfileFragment;
import com.ngstudio.friendstep.ui.fragments.MapRequestContact;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by beyka on 08.08.2014.
 */
public class MapForPushActivity extends BaseActivity {
    public static final String KEY_ALERT = "alert";
    public static final String KEY_SOUND = "sound";
    public static final String KEY_CATEGORY = "category";
    public static final String KEY_NAME = "name";
    public static final String KEY_MOBILENUMBER = "mobilenumber";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";
    public static final String KEY_TYPE = "type";
    public static final int REQUEST_CODE_ENABLE_GPS = 1;

    private String alert;
    private String sound;
    private String category;
    private String type;
    private String name;
    private String mobilenumber;
    private double latitude;
    private double longitude;

    private TextView titleTV;

    private static String addedFragment = IncomingRequestFragment.class.getName();

    public static void startMapProfile(@NotNull Context context, @Nullable ContactLocation contactLocation) {
        addedFragment = MapProfileFragment.class.getName();
        Intent intent = new Intent(context, MapForPushActivity.class);

        intent.putExtra(CONTACT_LOCATION_LATITUDE,contactLocation.getLatitude())
              .putExtra(CONTACT_LOCATION_LONGITUDE,contactLocation.getLongitude())
              .putExtra(CONTACT_MOBILE_NUMBER,contactLocation.getMobilenumber())
              .putExtra(CONTACT_USER_NAME,contactLocation.getUsername());
        context.startActivity(intent);
    }

    public static final String CONTACT_LOCATION_LATITUDE = "CONTACT_LOCATION_LATITUDE";
    public static final String CONTACT_LOCATION_LONGITUDE = "CONTACT_LOCATION_LONGITUDE";
    public static final String CONTACT_MOBILE_NUMBER = "CONTACT_MOBILE_NUMBER";
    public static final String CONTACT_USER_NAME = "CONTACT_USER_NAME";

    public static void startMapRequestContact(@NotNull Context context, @Nullable ContactLocation contactLocation) {
        addedFragment = MapRequestContact.class.getName();
        Intent intent = new Intent(context, MapForPushActivity.class);

        intent.putExtra(CONTACT_LOCATION_LATITUDE,contactLocation.getLatitude())
                .putExtra(CONTACT_LOCATION_LONGITUDE,contactLocation.getLongitude())
                .putExtra(CONTACT_MOBILE_NUMBER,contactLocation.getMobilenumber())
                .putExtra(CONTACT_USER_NAME,contactLocation.getUsername());
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_push);


//---MOCK DATA---
//        Bundle extras = new Bundle();
//        extras.putString(MapForPushActivity.KEY_ALERT, "Where Are You");
//        extras.putString(MapForPushActivity.KEY_SOUND, "sws.caf");
//        extras.putString(MapForPushActivity.KEY_CATEGORY, "mock_type");
//        extras.putString(MapForPushActivity.KEY_TYPE, "LocationRequest");
//        extras.putString(MapForPushActivity.KEY_NAME, "Test Testovich");
//        extras.putString(MapForPushActivity.KEY_MOBILENUMBER, "+38050700211");
//        extras.putString(MapForPushActivity.KEY_LATITUDE, "48.407380");
//        extras.putString(MapForPushActivity.KEY_LONGITUDE, "35.002406");

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            alert = extras.getString(KEY_ALERT);
            sound = extras.getString(KEY_SOUND);
            category = extras.getString(KEY_CATEGORY);
            type = extras.getString(KEY_TYPE);
            name = extras.getString(KEY_NAME);
            mobilenumber = extras.getString(KEY_MOBILENUMBER);
            latitude = extras.getDouble(KEY_LATITUDE);
            longitude = extras.getDouble(KEY_LONGITUDE);
        } else {
            finish();
        }

        initActionBar();
        addFragment(addedFragment, extras, false);
    }

    @Override
    protected int getFragmentContainerId() {
        return R.id.container;
    }

    private void initActionBar() {
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setIcon(null);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        actionBar.setCustomView(R.layout.actionbar_map_push);

        titleTV = (TextView) actionBar.getCustomView().findViewById(R.id.tvActionbarTitle);
        actionBar.getCustomView().findViewById(R.id.ivActionbarIcon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        titleTV.setText(title);
    }

    @Override
    public void setTitle(int titleId) {
        super.setTitle(titleId);
        titleTV.setText(getString(titleId));
    }


}

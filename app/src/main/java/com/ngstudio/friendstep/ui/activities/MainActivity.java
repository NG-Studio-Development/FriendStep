package com.ngstudio.friendstep.ui.activities;


import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ngstudio.friendstep.FragmentPool;
import com.ngstudio.friendstep.R;
import com.ngstudio.friendstep.WhereAreYouApplication;
import com.ngstudio.friendstep.components.GeoService;
import com.ngstudio.friendstep.components.NotificationManager;
import com.ngstudio.friendstep.components.cache.AvatarBase64ImageDownloader;
import com.ngstudio.friendstep.ui.adapters.ItemsAdapter;
import com.ngstudio.friendstep.ui.fragments.ContactsFragment;
import com.ngstudio.friendstep.ui.fragments.MapFragment;
import com.ngstudio.friendstep.ui.fragments.RequestFragment;
import com.ngstudio.friendstep.ui.fragments.SettingsFragment;
import com.ngstudio.friendstep.ui.widgets.ActionBarHolder;
import com.ngstudio.friendstep.ui.widgets.AdapterLinearLayout;
import com.ngstudio.friendstep.utils.WhereAreYouAppConstants;
import com.ngstudio.friendstep.utils.WhereAreYouAppLog;

public class MainActivity extends BaseActivity implements NotificationManager.Client {

    private DrawerLayout mDrawerLayout;
    private AdapterLinearLayout sliderMenu;
    private RelativeLayout leftDrawer;
    private ActionBarDrawerToggle mDrawerToggle;
    private ImageView avatar;
    ActionBarHolder actionBarHolder;

    private ItemsAdapter adapter;
    private InputMethodManager inputMethodManager;

    public static final int REQUEST_CODE_ENABLE_GPS = 1;

    @Override
    protected int getFragmentContainerId() {
        return R.id.container;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("MAIN_ACTIVITY", "LOG");
        /*new Thread(new Runnable() {
            @Override
            public void run() {
                GoogleCloudMessagingClient.getInstance().register();
            }
        }).start();*/

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        sliderMenu = (AdapterLinearLayout) findViewById(R.id.lnSliderMenu);
        leftDrawer = (RelativeLayout) findViewById(R.id.leftDrawer);
        avatar = (ImageView) findViewById(R.id.ivAvatar);

        sliderMenu.setAdapter(adapter = ItemsAdapter.getSideMenuAdapter(this));
        sliderMenu.setOnItemClickListener(new AdapterLinearLayout.OnItemClickListener() {
            @Override
            public void onItemClick(Adapter adapter, int pos, View v) {
                selectItem(pos);
            }
        });

        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.drawable.ic_sidemenu,
                R.string.content_pin_type,
                R.string.content_pin_type
        ) {
            public void onDrawerClosed(View view) {
                ActivityCompat.invalidateOptionsMenu(MainActivity.this);
            }

            public void onDrawerOpened(View drawerView) {
                ActivityCompat.invalidateOptionsMenu(MainActivity.this);
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        WhereAreYouApplication.getInstance().checkForLocationServices(this,new Runnable() {
            @Override
            public void run() {
                startService(new Intent(MainActivity.this, GeoService.class));
            }
        });

        initActionBar();
        if (savedInstanceState == null) {
            selectItem(0);
        }
    }

    private void initActionBar() {
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setIcon(null);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        actionBarHolder = new ActionBarHolder();
        actionBar.setCustomView(actionBarHolder.initHolder(this));
        actionBarHolder.setActionBarIconClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputMethodManager.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
                if (!mDrawerLayout.isDrawerOpen(leftDrawer))
                    mDrawerLayout.openDrawer(leftDrawer);
                else
                    mDrawerLayout.closeDrawer(leftDrawer);
            }
        });
    }


    public ActionBarHolder getActionBarHolder() {
        return actionBarHolder;
    }

    @Override
    protected void onDestroy() {
        NotificationManager.unregisterClient(this);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //String str = AvatarBase64ImageDownloader.getImageUriFor(WhereAreYouApplication.getInstance().getCurrentMobile());
        WhereAreYouApplication.getInstance().getAvatarCache().displayImage(AvatarBase64ImageDownloader.getImageUriFor(WhereAreYouApplication.getInstance().getCurrentMobile()),avatar);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        WhereAreYouAppLog.i("onActivityResult " + requestCode);
        if(requestCode == REQUEST_CODE_ENABLE_GPS) {
            startService(new Intent(MainActivity.this, GeoService.class));
        }
    }

    private void selectItem(int position) {
        if(!sliderMenu.setSelected(position))
            return;

        ItemsAdapter.MenuItem item = adapter.getItem(position);
        Fragment fragment;

        switch(item.getIconId()) {
            case R.drawable.drawable_item_menu_map:
                fragment = FragmentPool.getInstance().newObject(MapFragment.class);
                //fragment = FragmentPool.getInstance().newObject(SettingsFragment.class);
                break;

            case R.drawable.drawable_item_menu_contacts:
                fragment = FragmentPool.getInstance().newObject(ContactsFragment.class);
                break;

            case R.drawable.drawable_item_requests:
                fragment = FragmentPool.getInstance().newObject(RequestFragment.class);
                break;

            case R.drawable.drawable_item_menu_settings:
                fragment = FragmentPool.getInstance().newObject(SettingsFragment.class);
                break;
            default:
                return;
        }
        switchFragment(fragment,false);

        actionBarHolder.enterState(item.getIconId());
        mDrawerLayout.closeDrawer(leftDrawer);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void handleNotificationMessage(int what, int arg1, int arg2, Object obj) {
        if(what == WhereAreYouAppConstants.NOTIFICATION_USER_AVATAR_LOADED) {
            WhereAreYouApplication.getInstance().getAvatarCache().displayImage(AvatarBase64ImageDownloader.getImageUriFor(WhereAreYouApplication.getInstance().getCurrentMobile()),avatar);
        }
    }
}





package com.ngstudio.friendstep.ui.activities;


import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ngstudio.friendstep.FragmentPool;
import com.ngstudio.friendstep.R;
import com.ngstudio.friendstep.WhereAreYouApplication;
import com.ngstudio.friendstep.components.GeoService;
import com.ngstudio.friendstep.components.NotificationManager;
import com.ngstudio.friendstep.ui.adapters.ItemsAdapter;
import com.ngstudio.friendstep.ui.adapters.ItemsAdapterOLD;
import com.ngstudio.friendstep.ui.fragments.ContactsFragment;
import com.ngstudio.friendstep.ui.fragments.MapFragment;
import com.ngstudio.friendstep.ui.fragments.RequestFragment;
import com.ngstudio.friendstep.ui.fragments.SettingsFragment;
import com.ngstudio.friendstep.ui.widgets.ActionBarHolder;
import com.ngstudio.friendstep.utils.WhereAreYouAppConstants;
import com.ngstudio.friendstep.utils.WhereAreYouAppLog;

public class MainActivity extends BaseActivity implements NotificationManager.Client {

    private ListView lvLeftDrawer;
    private DrawerLayout mDrawerLayout;

    private ItemsAdapterOLD adapter;
    private ActionBarDrawerToggle mDrawerToggle;

    public static final int REQUEST_CODE_ENABLE_GPS = 1;

    @Override
    protected int getFragmentContainerId() {
        return R.id.container;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        lvLeftDrawer = (ListView) findViewById(R.id.left_drawer);
        lvLeftDrawer.setAdapter(ItemsAdapter.getSideMenuAdapter(this));

        lvLeftDrawer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectItem(i);
            }
        });

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                toolbar, R.string.drawer_open, R.string.drawer_close) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        Log.d("MAIN_ACTIVITY", "LOG");
        adapter = ItemsAdapterOLD.getSideMenuAdapter(this);

        WhereAreYouApplication.getInstance().checkForLocationServices(this,new Runnable() {
            @Override
            public void run() {
                startService(new Intent(MainActivity.this, GeoService.class));
            }
        });

        //initActionBar();
        if (savedInstanceState == null) {
            selectItem(0);
        }
    }

    public ActionBarHolder getActionBarHolder() {
        return null;
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
        //WhereAreYouApplication.getInstance().getAvatarCache().displayImage(AvatarBase64ImageDownloader.getImageUriFor(WhereAreYouApplication.getInstance().getCurrentMobile()),avatar);
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
        //if(!sliderMenu.setSelected(position)) return;

        ItemsAdapterOLD.MenuItem item = adapter.getItem(position);
        Fragment fragment;

        switch(item.getIconId()) {
            case R.drawable.drawable_item_menu_map:
                fragment = FragmentPool.getInstance().newObject(MapFragment.class);
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

        //actionBarHolder.enterState(item.getIconId());
         mDrawerLayout.closeDrawer(lvLeftDrawer);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void handleNotificationMessage(int what, int arg1, int arg2, Object obj) {
        if(what == WhereAreYouAppConstants.NOTIFICATION_USER_AVATAR_LOADED) {
            // WhereAreYouApplication.getInstance().getAvatarCache().displayImage(AvatarBase64ImageDownloader.getImageUriFor(WhereAreYouApplication.getInstance().getCurrentMobile()),avatar);
        }
    }
}





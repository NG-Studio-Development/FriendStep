package com.ngstudio.friendstep.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.ngstudio.friendstep.FragmentPool;
import com.ngstudio.friendstep.R;
import com.ngstudio.friendstep.WhereAreYouApplication;
import com.ngstudio.friendstep.components.GeoService;
import com.ngstudio.friendstep.components.NotificationManager;
import com.ngstudio.friendstep.ui.adapters.ItemsAdapter;
import com.ngstudio.friendstep.ui.fragments.ContactsFragment;
import com.ngstudio.friendstep.ui.fragments.MapFragment;
import com.ngstudio.friendstep.ui.fragments.RequestFragment;
import com.ngstudio.friendstep.ui.fragments.SettingsFragment;
import com.ngstudio.friendstep.utils.WhereAreYouAppConstants;
import com.ngstudio.friendstep.utils.WhereAreYouAppLog;

public class MainActivity extends BaseActivity implements NotificationManager.Client {

    RecyclerView rvDrawerContainer;
    ItemsAdapter menuAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle mDrawerToggle;

    public static final int REQUEST_CODE_ENABLE_GPS = 1;

    @Override
    protected int getFragmentContainerId() {
        return R.id.container;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        float dist = distFrom(0.0f, 0.0f, 1.1f, 1.1f);
        Log.d("DISTANTION", "Distantion = "+dist);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rvDrawerContainer = (RecyclerView) findViewById(R.id.rwDrawerContainer);
        rvDrawerContainer.setHasFixedSize(true);
        menuAdapter = new ItemsAdapter(this,
                WhereAreYouApplication.getInstance().getUserName(),
                WhereAreYouApplication.getInstance().getUserEmail(), new ItemsAdapter.OnItemClickListener() {
                                                                                @Override
                                                                                public void onItemClick(int position) {
                                                                                    selectItem(position);
                                                                                }
                                                                            });

        rvDrawerContainer.setAdapter(menuAdapter);

        mLayoutManager = new LinearLayoutManager(this);
        rvDrawerContainer.setLayoutManager(mLayoutManager);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,toolbar,R.string.drawer_open,R.string.drawer_close) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };

        drawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        WhereAreYouApplication.getInstance().checkForLocationServices(this,new Runnable() {
            @Override
            public void run() {
                startService(new Intent(MainActivity.this, GeoService.class));
            }
        });

        if (savedInstanceState == null) {
            selectItem(0);
        }
    }

    public static float distFrom(float lat1, float lng1, float lat2, float lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        float dist = (float) (earthRadius * c);

        return dist;
    }

    @Override
    protected void onDestroy() {
        NotificationManager.unregisterClient(this);
        super.onDestroy();
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
          

        ItemsAdapter.MenuItem item = menuAdapter.getItem(position);
        Fragment fragment;

        switch(item.getIconId()) {
            case R.drawable.drawable_item_menu_map:
                //fragment = FragmentPool.getInstance().newObject(MapFragment.class);
                //break;

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
        drawerLayout.closeDrawers();
    }

    @Override
    public void handleNotificationMessage(int what, int arg1, int arg2, Object obj) {
        if(what == WhereAreYouAppConstants.NOTIFICATION_USER_AVATAR_LOADED) {
             //WhereAreYouApplication.getInstance().getAvatarCache().displayImage(AvatarBase64ImageDownloader.getImageUriFor(WhereAreYouApplication.getInstance().getCurrentMobile()),avatar);
        } else if (what == WhereAreYouAppConstants.NOTIFICATION_CONTACTS_NEARBY) {

        }
    }
}





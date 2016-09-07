package com.ngstudio.friendstep.ui.activities;


import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.ngstudio.friendstep.R;
import com.ngstudio.friendstep.model.entity.step.ContactStep;
import com.ngstudio.friendstep.ui.fragments.ProfileFragment;
import com.ngstudio.friendstep.ui.widgets.ActionBarHolder;
import com.ngstudio.friendstep.utils.WhereAreYouAppConstants;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ProfileActivity extends BaseActivity {

    ActionBarHolder actionBarHolder;

    public static void  startProfileActivity(@NotNull Context context, @Nullable ContactStep contact) {
        Intent intent = new Intent(context, ProfileActivity.class);
        intent.putExtra(WhereAreYouAppConstants.KEY_CONTACT,contact);
        context.startActivity(intent);
    }

    @Override
    protected int getFragmentContainerId() {
        return R.id.container;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(savedInstanceState == null) {
            addFragment(ProfileFragment.class,getIntent().getExtras(),false);
        }
    }

    public void initActionBar(boolean isMyProfile) {
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setIcon(null);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setBackgroundDrawable(new ColorDrawable());
        actionBarHolder = new ActionBarHolder();
        actionBar.setCustomView(actionBarHolder.initHolder(this));
        actionBarHolder.setActionBarIcon(R.drawable.drawable_ic_back);
        actionBarHolder.setActionBarIconClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        actionBarHolder.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        actionBarHolder.enterState(isMyProfile ? 0 : ActionBarHolder.STATE_CONTACT_PROFILE);
    }

    public ActionBarHolder getActionBarHolder() {
        return actionBarHolder;
    }

    /*@Override
    public void onBackPressed() {
        if(actionBarHolder.collapseSearchField(actionBarHolder.findViewById(R.id.ivEdit))) {
            return;
        }

        super.onBackPressed();
    }*/
}

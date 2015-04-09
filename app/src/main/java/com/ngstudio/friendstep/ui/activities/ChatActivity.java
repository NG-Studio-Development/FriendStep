package com.ngstudio.friendstep.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.View;

import com.ngstudio.friendstep.R;
import com.ngstudio.friendstep.model.entity.step.ContactStep;
import com.ngstudio.friendstep.ui.fragments.ChatFragment;
import com.ngstudio.friendstep.ui.widgets.ActionBarHolder;
import com.ngstudio.friendstep.utils.WhereAreYouAppConstants;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChatActivity extends BaseActivity {
    ActionBarHolder actionBarHolder;

    public static void startChatActivity (@NotNull Context context, @Nullable ContactStep contact) {
        Intent intent = new Intent(context, ChatActivity.class);
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
        setContentView(R.layout.activity_chat);
        addFragment(ChatFragment.instance(getIntent().getExtras()),false);
    }



    public void initActionBar() {
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setIcon(null);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        actionBarHolder = new ActionBarHolder();
        actionBar.setCustomView(actionBarHolder.initHolder(this));

        actionBarHolder.setActionBarIcon(R.drawable.drawable_ic_back);
        actionBarHolder.enterState(0);
        actionBarHolder.setActionBarIconClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();

            }
        });
    }

    public ActionBarHolder getActionBarHolder() {
        return actionBarHolder;
    }

    @Override
    public void onBackPressed() {
        if(actionBarHolder.collapseSearchField(actionBarHolder.findViewById(R.id.ivEdit))) {
            return;
        }

        super.onBackPressed();
    }
}

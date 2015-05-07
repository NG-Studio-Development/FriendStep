package com.ngstudio.friendstep.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.ngstudio.friendstep.R;
import com.ngstudio.friendstep.model.entity.step.ContactStep;
import com.ngstudio.friendstep.ui.fragments.ChatFragment;
import com.ngstudio.friendstep.utils.WhereAreYouAppConstants;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChatActivity extends BaseActivity {

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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        addFragment(ChatFragment.instance(getIntent().getExtras()),false);
    }

}

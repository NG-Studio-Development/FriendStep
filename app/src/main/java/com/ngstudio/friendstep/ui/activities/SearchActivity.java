package com.ngstudio.friendstep.ui.activities;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.ngstudio.friendstep.R;
import com.ngstudio.friendstep.ui.fragments.SearchFragment;

public class SearchActivity extends BaseActivity {

    @Override
    protected int getFragmentContainerId() {
        return R.id.container;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        addFragment(SearchFragment.newInstance(getIntent().getExtras()),false);

    }
}

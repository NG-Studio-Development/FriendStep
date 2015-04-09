package com.ngstudio.friendstep.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ngstudio.friendstep.R;
import com.ngstudio.friendstep.ui.activities.LoginActivity;


public class MenuLoginFragment extends BaseFragment<LoginActivity> implements View.OnClickListener {


    /*public static MenuLoginFragment createSendMessageRequest() {
        MenuLoginFragment fragment = new MenuLoginFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }*/



    /*@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }*/



    @Override
    public int getLayoutResID() {
        return R.layout.fragment_menu_login;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu_login, container, false);
        view.findViewById(R.id.buttonRegister).setOnClickListener(this);
        view.findViewById(R.id.buttonSignin).setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonSignin:
                getHostActivity().switchFragment(RegisterFragment.newSignInInstance(),false);
                break;
            case R.id.buttonRegister:
                getHostActivity().switchFragment(RegisterFragment.newRegisterInstance(),false);
                break;
        }
    }
}

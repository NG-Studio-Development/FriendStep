package com.ngstudio.friendstep.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ngstudio.friendstep.R;
import com.ngstudio.friendstep.ui.activities.LoginActivity;


public class LoginFragment extends BaseFragment<LoginActivity> {

    @Override
    public int getLayoutResID() {
        return R.layout.fragment_menu_login;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_menu_login, container, false);
        final EditText etLogin = (EditText) view.findViewById(R.id.etLogin);
        final EditText etPass = (EditText) view.findViewById(R.id.etPass);
        TextView tvCreateAccount = (TextView) view.findViewById(R.id.tvCreateAccount);
        view.findViewById(R.id.buttonEnter).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                //getHostActivity().registerInBackground(etLogin.getText().toString(), null, etPass.getText().toString(), LoginActivity.TYPE_SIGN_IN);
                logInAction(etLogin.getText().toString(), etPass.getText().toString());
            }
        });

        tvCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getHostActivity().switchFragment(new RegisterFragment(), true);
            }
        });

        return view;
    }

    private void logInAction(String login, String pass) {
        if (!chackValidEnterData(login, pass)) {
            Toast.makeText(getHostActivity(),getString(R.string.notis_enter_valid_data), Toast.LENGTH_SHORT).show();
        } else {
            getHostActivity().signInListener(login, pass);
        }




    }

    private boolean chackValidEnterData(String login, String pass) {
        return !login.isEmpty() && !pass.isEmpty();
    }
}

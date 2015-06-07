package com.ngstudio.friendstep.ui.fragments;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ngstudio.friendstep.R;
import com.ngstudio.friendstep.ui.activities.LoginActivity;

public class RegisterFragment extends BaseFragment<LoginActivity> {

    private EditText etEmail, etUserName;
    private EditText etPass;
    private EditText etRepass;
    private Button buttonRegister;
    private ImageButton ibBack;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        TextView text = new TextView(activity);
        int padding = getResources().getDimensionPixelSize(R.dimen.margin_widget_default_small);
        text.setPadding(padding, padding, padding, padding);
        text.setText(R.string.content_pin_type);
        text.setTextColor(Color.WHITE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(getLayoutResID(), container, false);

        etPass = (EditText) view.findViewById(R.id.edPass);
        etRepass = (EditText) view.findViewById(R.id.edRepass);
        etEmail = (EditText) view.findViewById(R.id.etEmail);
        etUserName = (EditText) view.findViewById(R.id.etUserName);
        buttonRegister = (Button) view.findViewById(R.id.buttonRegister);

        buttonRegister.setText(getString(R.string.text_button_register));
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getHostActivity().registerListener(etUserName.getText().toString(),
                        etEmail.getText().toString(),
                        etPass.getText().toString(),
                        etRepass.getText().toString() );
            }
        });

        ibBack = (ImageButton) view.findViewById(R.id.ibBack);
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             getHostActivity().onBackPressed();
            }
        });

        return view;
    }

    void clearInfo() {
        etEmail.getText().clear();
        etUserName.getText().clear();
        etPass.getText().clear();
    }

    @Override
    public int getLayoutResID() {
        return R.layout.fragment_register;
    }
}

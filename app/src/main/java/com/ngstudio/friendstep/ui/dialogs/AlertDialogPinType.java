package com.ngstudio.friendstep.ui.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import com.ngstudio.friendstep.R;

/**
 * Created by Николай on 22.07.2014.
 */
public class AlertDialogPinType extends AlertDialogBase implements View.OnClickListener{

    public AlertDialogPinType(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.title_pin_type);
        addPositiveButton(R.string.text_button_ok,new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

    }

    @Override
    public void onClick(View v) {
        dismiss();
    }
}

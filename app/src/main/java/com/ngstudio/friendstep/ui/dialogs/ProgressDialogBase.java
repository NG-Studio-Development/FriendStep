package com.ngstudio.friendstep.ui.dialogs;

import android.app.ProgressDialog;
import android.content.Context;

public class ProgressDialogBase extends ProgressDialog{
    private static ProgressDialogBase progressDialog;

    private ProgressDialogBase(Context context) {
        super(context);
    }

    public static ProgressDialogBase getInstance(Context context) {
        if (progressDialog == null)
            progressDialog = new ProgressDialogBase(context);

        return progressDialog;
    }

    public ProgressDialog setContent(String title, String message) {
        progressDialog.setTitle(title);
        progressDialog.setMessage(message);
        return progressDialog;
    }
}

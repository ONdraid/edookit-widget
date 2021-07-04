package com.vomaon.edookit.widget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;

public class LoginDialog {

    Activity activity;
    AlertDialog alertDialog;

    LoginDialog(Activity activity) {
        this.activity = activity;
    }

    @SuppressLint("InflateParams")
    public void showLoginDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.login_dialog, null));
        builder.setCancelable(false);

        alertDialog = builder.create();
        alertDialog.show();
    }

    public void hideLoginDialog() {
        alertDialog.dismiss();
    }
}

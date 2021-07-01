package com.vomaon.edookit.widget;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    SharedPreferences sharedPref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = this.getSharedPreferences("LoginData", Context.MODE_PRIVATE);

        Boolean loginStatus = sharedPref.getBoolean("loginStatus", false);
        if (loginStatus.equals(false)) {
            openLoginActivity();
        } else {
            Intent intent = new Intent(this, AfterLoginActivity.class);
            startActivity(intent);
            finish();
        }

    }

    public void openLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
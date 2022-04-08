package com.vomaon.edookit.widget;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences sharedPref;
    private Boolean canContinue = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = this.getSharedPreferences("UserData", Context.MODE_PRIVATE);

        // Remove this on next update
        PackageInfo packageInfo = null;
        try {
            packageInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        assert packageInfo != null;
        int versionCode = packageInfo.versionCode;
        int registeredVersionCode = sharedPref.getInt("versionCode", 0);

        if (versionCode > registeredVersionCode) {
            @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = sharedPref.edit();
            editor.clear();
            editor.putInt("versionCode", versionCode);
            editor.apply();
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED) {
                canContinue = false;
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
            }
        }

        if (canContinue) {
            redirect();
        }

    }

    public void redirect() {
        Boolean loginStatus = sharedPref.getBoolean("loginStatus", false);
        Boolean introduced = sharedPref.getBoolean("introduced", false);
        boolean darkModeControl = sharedPref.getBoolean("darkModeControl", false);
        Intent intent;

        if (darkModeControl) {
            boolean darkMode = sharedPref.getBoolean("darkMode", false);
            if (darkMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        }

        if (introduced.equals(false)) {
            intent = new Intent(this, OnboardingScreenActivity.class);
        } else if (loginStatus.equals(false)) {
            intent = new Intent(this, LoginActivity.class);
        } else {
            intent = new Intent(this, AfterLoginActivity.class);
        }
        startActivity(intent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 101) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                redirect();
            } else {
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}
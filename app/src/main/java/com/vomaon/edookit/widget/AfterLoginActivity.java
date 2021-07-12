package com.vomaon.edookit.widget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AfterLoginActivity extends AppCompatActivity {

    Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_login);

        TextView welcomeTextView = findViewById(R.id.welcomeTextView);
        button = findViewById(R.id.signOutButton);

        SharedPreferences sharedPref = this.getSharedPreferences("UserData", Context.MODE_PRIVATE);
        String fullname = sharedPref.getString("fullname", "");
        String welcomeText = getString(R.string.welcome) + ' ' + fullname + "!";
        welcomeTextView.setText(welcomeText);

        button.setOnClickListener(view -> {
            Context context = getApplicationContext();

            SharedPreferences.Editor editor;
            editor = sharedPref.edit();
            editor.remove("schoolID");
            editor.remove("username");
            editor.remove("password");
            editor.remove("timetableHtml");
            editor.remove("fullname");
            editor.putBoolean("loginStatus", false);
            editor.apply();

            Intent updateIntent = new Intent(context, EdookitWidgetProvider.class);
            updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), EdookitWidgetProvider.class));
            updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            sendBroadcast(updateIntent);

            Intent intent = new Intent(context, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}

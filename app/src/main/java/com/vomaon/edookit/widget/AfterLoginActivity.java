package com.vomaon.edookit.widget;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;

public class AfterLoginActivity extends AppCompatActivity {

    Button button;
    WebView edookitWebView;

    @SuppressLint({"SetJavaScriptEnabled", "RequiresFeature"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_login);

        TextView welcomeTextView = findViewById(R.id.welcomeTextView);
        button = findViewById(R.id.signOutButton);
        edookitWebView = findViewById(R.id.edookitWebView);

        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                WebSettingsCompat.setForceDark(edookitWebView.getSettings(), WebSettingsCompat.FORCE_DARK_ON);
                WebSettingsCompat.setForceDarkStrategy(edookitWebView.getSettings(), WebSettingsCompat.DARK_STRATEGY_USER_AGENT_DARKENING_ONLY);
                edookitWebView.setBackgroundColor(Color.BLACK);
            }
        }

        SharedPreferences sharedPref = this.getSharedPreferences("UserData", Context.MODE_PRIVATE);
        String fullname = sharedPref.getString("fullname", "");
        String welcomeText = getString(R.string.welcome) + " " + fullname + "!";
        welcomeTextView.setText(welcomeText);

        edookitWebView.setWebViewClient(new WebViewClient());
        edookitWebView.getSettings().setJavaScriptEnabled(true);
        String schoolIDStr = sharedPref.getString("schoolID", "");
        edookitWebView.loadUrl("https://" + schoolIDStr + ".edookit.net/user/login");

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

    @Override
    public void onBackPressed() {
        if (edookitWebView.canGoBack()) {
            edookitWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }

}

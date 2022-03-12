package com.vomaon.edookit.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;

public class TimetableActivity extends AppCompatActivity {

    WebView webView;
    SharedPreferences sharedPref;
    String data;

    @SuppressLint({"SetJavaScriptEnabled", "RequiresFeature"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable);

        webView = findViewById(R.id.timetableWebView);
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                WebSettingsCompat.setForceDark(webView.getSettings(), WebSettingsCompat.FORCE_DARK_ON);
                WebSettingsCompat.setForceDarkStrategy(webView.getSettings(), WebSettingsCompat.DARK_STRATEGY_USER_AGENT_DARKENING_ONLY);
                webView.setBackgroundColor(Color.TRANSPARENT);
            }
        }
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);

        sharedPref = this.getSharedPreferences("UserData", Context.MODE_PRIVATE);
        data = sharedPref.getString("timetableHtml", "");
        webView.loadDataWithBaseURL(null, data, "text/html", "UTF-8", null);
    }
}
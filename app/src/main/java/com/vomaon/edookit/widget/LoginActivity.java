package com.vomaon.edookit.widget;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

public class LoginActivity extends AppCompatActivity {

    EditText schoolID, username, password;
    WebView webView;
    String schoolIDStr, usernameStr, passwordStr, data;
    Button loginButton;



    @SuppressLint("SetJavaScriptEnabled")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginButton = findViewById(R.id.loginButton);
        webView = findViewById(R.id.webView);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.setWebChromeClient(new WebChromeClient());

        loginButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("CommitPrefEdits")
            @Override
            public void onClick(View view) {
                Context context = getApplicationContext();
                if(!Python.isStarted())
                    Python.start(new AndroidPlatform(context));
                Python py = Python.getInstance();
                PyObject pyObj = py.getModule("gethtmltable");

                SharedPreferences sharedPref = context.getSharedPreferences("LoginData", Context.MODE_PRIVATE);
                schoolID = findViewById(R.id.schoolIDInput);
                username = findViewById(R.id.usernameInput);
                password = findViewById(R.id.passwordInput);

                schoolIDStr = schoolID.getText().toString();
                usernameStr = username.getText().toString();
                passwordStr = password.getText().toString();

                PyObject obj = null;
                obj = pyObj.callAttr("main", usernameStr, passwordStr, schoolIDStr);
                data = obj.toString();
                if (data.equals("error")) {
                    Toast.makeText(context,"Chyba: nesprávné přihlašovací údaje", Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(context,"Registrace proběhla úspěšně", Toast.LENGTH_SHORT).show();

                    webView.loadDataWithBaseURL(null, data, null, "UTF-8", null);

                    SharedPreferences.Editor editor;
                    editor = sharedPref.edit();
                    editor.putString("schoolID", schoolIDStr);
                    editor.putString("username", usernameStr);
                    editor.putString("password", passwordStr);
                    editor.putString("timetableHtml", data);
                    editor.putBoolean("loginStatus", true);
                    editor.apply();

                    Intent updateIntent = new Intent(LoginActivity.this, EdookitWidgetProvider.class);
                    updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                    int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), EdookitWidgetProvider.class));
                    updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
                    sendBroadcast(updateIntent);
                }

            }
        });
    }

}
package com.vomaon.edookit.widget;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
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
    String schoolIDStr, usernameStr, passwordStr, data;
    Button loginButton;

    Handler mainHandler = new Handler();


    @SuppressLint("SetJavaScriptEnabled")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        LoginDialog loginDialog = new LoginDialog(LoginActivity.this);

        loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("CommitPrefEdits")
            @Override
            public void onClick(View view) {
                schoolID = findViewById(R.id.schoolIDInput);
                username = findViewById(R.id.usernameInput);
                password = findViewById(R.id.passwordInput);

                schoolIDStr = schoolID.getText().toString();
                usernameStr = username.getText().toString();
                passwordStr = password.getText().toString();

                loginDialog.showLoginDialog();

                startLoginRunnable();
            }

            public void startLoginRunnable() {
                LoginRunnable runnable = new LoginRunnable();
                new Thread(runnable).start();
            }

            class LoginRunnable implements Runnable {

                @Override
                public void run() {
                    Context context = getApplicationContext();

                    if(!Python.isStarted())
                        Python.start(new AndroidPlatform(context));
                    Python py = Python.getInstance();
                    PyObject pyObj = py.getModule("gethtmltable");

                    PyObject obj = null;
                    obj = pyObj.callAttr("main", usernameStr, passwordStr, schoolIDStr);
                    data = obj.toString();

                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (data.equals("error")) {
                                Toast.makeText(context,"Chyba: nesprávné přihlašovací údaje", Toast.LENGTH_LONG).show();
                                loginDialog.hideLoginDialog();
                            }
                            else {
                                Toast.makeText(context,"Registrace proběhla úspěšně", Toast.LENGTH_SHORT).show();

                                SharedPreferences sharedPref = context.getSharedPreferences("LoginData", Context.MODE_PRIVATE);
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

                                Intent intent = new Intent(context, AfterLoginActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });

                }
            }

        });
    }

}
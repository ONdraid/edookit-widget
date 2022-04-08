package com.vomaon.edookit.widget;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private EditText schoolID, username, password;
    private String schoolIDStr, usernameStr, passwordStr, fullname;
    private boolean error;
    private Button loginButton;
    private ConstraintLayout loginForm;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private Thread thread;

    @SuppressLint("SetJavaScriptEnabled")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        LoginDialog loginDialog = new LoginDialog(LoginActivity.this);
        Network network = new Network(LoginActivity.this);
        PyWrapper pyWrapper = new PyWrapper(LoginActivity.this);

        loginForm = findViewById(R.id.loginFormConstraintLayout);
        schoolID = findViewById(R.id.schoolIDInput);
        TextInputLayout schoolIDLayout = findViewById(R.id.schoolIDInputLayout);
        username = findViewById(R.id.usernameInput);
        password = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        ImageView themeSwitcherImageView = findViewById(R.id.themeSwitcherImageView);

        sharedPref = this.getSharedPreferences("UserData", Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        schoolID.addTextChangedListener(LoginWatcher);
        username.addTextChangedListener(LoginWatcher);
        password.addTextChangedListener(LoginWatcher);

        schoolIDLayout.setEndIconOnClickListener(view -> loginDialog.showHelperDialog());

        themeSwitcherImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int defaultNightMode = AppCompatDelegate.getDefaultNightMode();
                if (defaultNightMode == AppCompatDelegate.MODE_NIGHT_YES) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    editor.putBoolean("darkMode", false);
                }
                else if (defaultNightMode == AppCompatDelegate.MODE_NIGHT_NO){
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    editor.putBoolean("darkMode", true);
                } else {
                    int nightModeFlags =
                            getApplicationContext().getResources().getConfiguration().uiMode &
                                    Configuration.UI_MODE_NIGHT_MASK;
                    switch (nightModeFlags) {
                        case Configuration.UI_MODE_NIGHT_YES:
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                            editor.putBoolean("darkMode", false);
                            break;

                        case Configuration.UI_MODE_NIGHT_NO:

                        case Configuration.UI_MODE_NIGHT_UNDEFINED:
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                            editor.putBoolean("darkMode", true);
                            break;
                    }
                }
                editor.putBoolean("darkModeControl", true);
                editor.apply();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("CommitPrefEdits")
            @Override
            public void onClick(View view) {
                if(!network.isConnected()) {
                    Toast.makeText(getApplicationContext(), R.string.toast_network_error, Toast.LENGTH_LONG).show();
                } else {
                    schoolIDStr = schoolID.getText().toString().trim();
                    usernameStr = username.getText().toString().trim();
                    passwordStr = password.getText().toString().trim();

                    loginDialog.showLoginDialog();

                    startLoginRunnable();
                }

            }

            public void startLoginRunnable() {
                if(thread!=null) {
                    thread.interrupt();
                    thread = null;
                }
                LoginRunnable runnable = new LoginRunnable();
                thread = new Thread(runnable);
                thread.start();
            }

            class LoginRunnable implements Runnable {

                @Override
                public void run() {
                    fullname = pyWrapper.getFullname(usernameStr, passwordStr, schoolIDStr);

                    runOnUiThread(() -> {
                        Context context = getApplicationContext();
                        if (fullname.equals("error")) {
                            loginForm.setBackgroundResource(R.drawable.login_form_error_bg);
                            error = true;

                            Toast.makeText(context,R.string.toast_login_error, Toast.LENGTH_LONG).show();
                            loginDialog.hideLoginDialog();

                        } else if (fullname.equals("network_error")) {
                            Toast.makeText(context,R.string.toast_network_error, Toast.LENGTH_LONG).show();
                            loginDialog.hideLoginDialog();

                        }
                        else {
                            sharedPref = context.getSharedPreferences("UserData", Context.MODE_PRIVATE);
                            editor = sharedPref.edit();

                            editor.putString("schoolID", schoolIDStr);
                            editor.putString("username", usernameStr);
                            editor.putString("password", passwordStr);
                            editor.putString("fullname", fullname);
                            editor.putBoolean("loginStatus", true);
                            editor.apply();

                            Intent updateIntent = new Intent(LoginActivity.this, EdookitWidgetProvider.class);
                            updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                            int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), EdookitWidgetProvider.class));
                            updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
                            sendBroadcast(updateIntent);

                            Intent intent = new Intent(context, AfterLoginActivity.class);
                            loginDialog.hideLoginDialog();
                            startActivity(intent);
                            finish();
                        }
                    });
                }
            }
        });
    }

    private final TextWatcher LoginWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String s = schoolID.getText().toString().trim();
            String u = username.getText().toString().trim();
            String p = password.getText().toString().trim();

            loginButton.setEnabled(!s.isEmpty() && !u.isEmpty() && !p.isEmpty());
        }

        @Override
        public void afterTextChanged(Editable editable) {
            if(error) {
                loginForm.setBackgroundResource(R.drawable.login_form_bg);
                error = false;
            }
        }
    };

    @Override
    protected void onDestroy() {
        if(thread!=null) {
            thread.interrupt();
        }
        super.onDestroy();
    }
}
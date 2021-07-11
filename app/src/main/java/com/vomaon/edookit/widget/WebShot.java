package com.vomaon.edookit.widget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RemoteViews;
import android.widget.Toast;

public class WebShot extends Activity {

    private WebView webView;
    private String data;
    private SharedPreferences sharedPref;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Network network = new Network(WebShot.this);
        if (!network.isConnected()) {
            Toast.makeText(this,R.string.toast_network_error, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        sharedPref = this.getSharedPreferences("UserData", Context.MODE_PRIVATE);
        boolean logged = sharedPref.getBoolean("loginStatus", false);
        if (!logged) {
            Intent loginIntent = new Intent(this, LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(loginIntent);
            Toast.makeText(this,R.string.toast_not_logged, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_popup);
        webView = findViewById(R.id.timetableWebView);

        LoadTableRunnable runnable = new LoadTableRunnable();
        new Thread(runnable).start();
    }

    class LoadTableRunnable implements Runnable {
        @Override
        public void run() {
            Context context = WebShot.this;
            String usernameStr = sharedPref.getString("username", "");
            String passwordStr = sharedPref.getString("password", "");
            String schoolIDStr = sharedPref.getString("schoolID", "");

            PyWrapper pyWrapper = new PyWrapper(WebShot.this);
            data = pyWrapper.getData(usernameStr, passwordStr, schoolIDStr);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = sharedPref.edit();

                    if (data.equals("network_error")) {
                        Toast.makeText(context,R.string.toast_network_error, Toast.LENGTH_LONG).show();
                        finish();

                    }
                    else if (data.equals("error")) {
                        Toast.makeText(context, R.string.toast_login_error, Toast.LENGTH_LONG).show();
                        editor.putBoolean("loginStatus", false);
                        editor.apply();

                        Intent loginIntent = new Intent(context, LoginActivity.class);
                        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(loginIntent);
                        finish();

                    } else {
                        editor.putString("timetableHtml", data);
                        editor.apply();

                        webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
                        webView.getSettings().setBuiltInZoomControls(false);
                        webView.getSettings().setLoadWithOverviewMode(true);
                        webView.getSettings().setUseWideViewPort(true);

                        webView.setWebViewClient(new WebViewClient() {
                            @Override
                            public void onPageFinished(WebView view, String url) {
                                super.onPageFinished(view, url);
                                webView.postDelayed(capture, 1000);
                            }
                        });

                        webView.loadDataWithBaseURL(null, data, null, "UTF-8", null);
                    }
                }
            });
        }
    }

    private final Runnable capture = new Runnable() {
        @Override
        public void run() {
            Bitmap b = screenshot(webView);
            updateWidgets(b);
            finish();
        }
    };


    private static Bitmap screenshot(WebView webView) {
        webView.measure(View.MeasureSpec.makeMeasureSpec(
                View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        webView.layout(0, 0, webView.getMeasuredWidth(), webView.getMeasuredHeight());
        webView.setDrawingCacheEnabled(true);
        webView.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(webView.getMeasuredWidth(),
                webView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        int iHeight = bitmap.getHeight();
        canvas.drawBitmap(bitmap, 0, iHeight, paint);
        webView.draw(canvas);
        return bitmap;
    }


    private void updateWidgets(Bitmap bmp) {
        final AppWidgetManager widgetManager = AppWidgetManager.getInstance(this);
        final int[] ids = widgetManager.getAppWidgetIds(
                new ComponentName(this, EdookitWidgetProvider.class));

        if (ids.length < 1) {
            return;
        }

        String schoolID = sharedPref.getString("schoolID", "");
        Uri uri = Uri.parse("https://" + schoolID + ".edookit.net/user/login");

        Intent uriIntent = new Intent(Intent.ACTION_VIEW, uri);
        PendingIntent pendingUriIntent = PendingIntent.getActivity(this, 0, uriIntent, 0);

        Intent timetableIntent = new Intent(this, TimetableActivity.class);
        PendingIntent pendingTimetableIntent = PendingIntent.getActivity(this, 0, timetableIntent, 0);

        Intent reloadWidgetIntent = new Intent(this, WebShot.class);
        PendingIntent pendingReloadWidgetIntent = PendingIntent.getActivity(this, 0, reloadWidgetIntent, 0);

        final RemoteViews views = new RemoteViews(getPackageName(), R.layout.timetable_widget);
        views.setImageViewBitmap(R.id.timetableImageView, bmp);

        views.setOnClickPendingIntent(R.id.timetableImageView, pendingTimetableIntent);
        views.setOnClickPendingIntent(R.id.edookitButton, pendingUriIntent);
        views.setOnClickPendingIntent(R.id.refreshButton, pendingReloadWidgetIntent);

        widgetManager.updateAppWidget(ids, views);

        Toast.makeText(this, R.string.toast_widget_update, Toast.LENGTH_SHORT).show();
    }
}

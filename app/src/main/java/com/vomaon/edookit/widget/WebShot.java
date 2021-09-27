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
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
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
    private Boolean nightMode;
    private int[] currentIds;
    private SharedPreferences sharedPref;
    private Thread thread;

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

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            nightMode = extras.getBoolean("dark_theme");
            currentIds = extras.getIntArray("current_ids");
        }

        LoadTableRunnable runnable = new LoadTableRunnable();
        thread = new Thread(runnable);
        thread.start();
    }

    class LoadTableRunnable implements Runnable {
        @Override
        public void run() {
            String usernameStr = sharedPref.getString("username", "");
            String passwordStr = sharedPref.getString("password", "");
            String schoolIDStr = sharedPref.getString("schoolID", "");

            PyWrapper pyWrapper = new PyWrapper(WebShot.this);
            data = pyWrapper.getData(usernameStr, passwordStr, schoolIDStr);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Context context = WebShot.this;
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
                        webView.setBackgroundColor(Color.WHITE);
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
            if (nightMode) {b = invert(b);}
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

    public Bitmap invert(Bitmap src)
    {
        int height = src.getHeight();
        int width = src.getWidth();

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();

        ColorMatrix matrixGrayscale = new ColorMatrix();
        matrixGrayscale.setSaturation(1);

        ColorMatrix matrixInvert = new ColorMatrix();
        matrixInvert.set(new float[]
                {
                        -1.0f, 0.0f, 0.0f, 0.0f, 255.0f,
                        0.0f, -1.0f, 0.0f, 0.0f, 255.0f,
                        0.0f, 0.0f, -1.0f, 0.0f, 255.0f,
                        0.0f, 0.0f, 0.0f, 1.0f, 0.0f
                });
        matrixInvert.preConcat(matrixGrayscale);

        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrixInvert);
        paint.setColorFilter(filter);

        canvas.drawBitmap(src, 0, 0, paint);
        return bitmap;
    }

    private void updateWidgets(Bitmap bmp) {
        final int[] ids;
        final AppWidgetManager widgetManager = AppWidgetManager.getInstance(this);
        if (nightMode) {
            ids = widgetManager.getAppWidgetIds(
                    new ComponentName(this, EdookitWidgetProviderDark.class));
        } else {
            ids = widgetManager.getAppWidgetIds(
                    new ComponentName(this, EdookitWidgetProvider.class));
        }
        if (ids.length < 1) {
            finish();
            return;
        }

        String schoolID = sharedPref.getString("schoolID", "");
        Uri uri = Uri.parse("https://" + schoolID + ".edookit.net/user/login");

        Intent uriIntent = new Intent(Intent.ACTION_VIEW, uri);
        PendingIntent pendingUriIntent = PendingIntent.getActivity(this, 0, uriIntent, 0);

        Intent timetableIntent = new Intent(this, TimetableActivity.class);
        PendingIntent pendingTimetableIntent = PendingIntent.getActivity(this, 0, timetableIntent, 0);

        Intent reloadWidgetIntent = new Intent(this, WebShot.class);
        reloadWidgetIntent.putExtra("dark_theme", nightMode);
        reloadWidgetIntent.putExtra("current_ids", currentIds);
        PendingIntent pendingReloadWidgetIntent = PendingIntent.getActivity(this, 0, reloadWidgetIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        final RemoteViews views;
        if (nightMode) {
            views = new RemoteViews(getPackageName(), R.layout.timetable_widget_dark);
        } else {
            views = new RemoteViews(getPackageName(), R.layout.timetable_widget);
        }

        views.setImageViewBitmap(R.id.timetableImageView, bmp);

        views.setOnClickPendingIntent(R.id.timetableImageView, pendingTimetableIntent);
        views.setOnClickPendingIntent(R.id.edookitButton, pendingUriIntent);
        views.setOnClickPendingIntent(R.id.refreshButton, pendingReloadWidgetIntent);

        widgetManager.updateAppWidget(currentIds, views);

        Toast.makeText(this, R.string.toast_widget_update, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        if(thread!=null) {
            thread.interrupt();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if(thread!=null) {
            thread.interrupt();
        }
        super.onDestroy();
    }
}

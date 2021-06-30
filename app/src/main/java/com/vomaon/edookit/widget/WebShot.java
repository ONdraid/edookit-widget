package com.vomaon.edookit.widget;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class WebShot extends AppCompatActivity {

    WebView webView;
    SharedPreferences sharedPref;
    String data;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_timetable);

        webView = findViewById(R.id.timetableWebView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.setWebChromeClient(new WebChromeClient());

        sharedPref = this.getSharedPreferences("LoginData", Context.MODE_PRIVATE);
        data = sharedPref.getString("timetableHtml", "");
        webView.loadDataWithBaseURL(null, data, null, "UTF-8", null);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Bitmap b = screenshot(webView);
                updateWidgets(b);
                finish();
            }
        }, 2000);
        webView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {

            }
        });

    }


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

        final RemoteViews views = new RemoteViews(getPackageName(), R.layout.timetable_widget);
        views.setImageViewBitmap(R.id.timetableImageView, bmp);
        widgetManager.updateAppWidget(ids, views);

        Toast.makeText(this, "WebShot Update", Toast.LENGTH_LONG).show();
    }

}

package com.vomaon.edookit.widget;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.RemoteViews;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

public class EdookitWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {

            SharedPreferences sharedPref = context.getSharedPreferences("LoginData", Context.MODE_PRIVATE);

            String usernameStr = sharedPref.getString("username", "");
            String passwordStr = sharedPref.getString("password", "");
            String schoolIDStr = sharedPref.getString("schoolID", "");

            if(!Python.isStarted())
                Python.start(new AndroidPlatform(context));
            Python py = Python.getInstance();
            PyObject pyObj = py.getModule("gethtmltable");
            PyObject obj = pyObj.callAttr("main", usernameStr, passwordStr, schoolIDStr);
            String data = obj.toString();

            @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("timetableHtml", data);
            editor.apply();

            String schoolID = sharedPref.getString("schoolID", "");
            Uri uri = Uri.parse("https://" + schoolID + ".edookit.net/user/login");

            Intent uriIntent = new Intent(Intent.ACTION_VIEW, uri);
            PendingIntent pendingUriIntent = PendingIntent.getActivity(context, 0, uriIntent, 0);

            Intent timetableIntent = new Intent(context, TimetableActivity.class);
            PendingIntent pendingTimetableIntent = PendingIntent.getActivity(context, 0, timetableIntent, 0);

            Intent reloadWidgetIntent = new Intent(context, EdookitWidgetProvider.class);
            reloadWidgetIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            reloadWidgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            PendingIntent pendingReloadWidgetIntent = PendingIntent.getBroadcast(context, appWidgetId, reloadWidgetIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            @SuppressLint("RemoteViewLayout") RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.timetable_widget);
            views.setOnClickPendingIntent(R.id.timetableImageView, pendingTimetableIntent);
            views.setOnClickPendingIntent(R.id.edookitButton, pendingUriIntent);
            views.setOnClickPendingIntent(R.id.refreshButton, pendingReloadWidgetIntent);

            AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, views);
        }
    }
}

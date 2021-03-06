package com.vomaon.edookit.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class EdookitWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        final int[] currentIds = appWidgetManager.getAppWidgetIds(
                new ComponentName(context, EdookitWidgetProvider.class));

        if (currentIds.length < 1) {
            return;
        }

        Intent reloadWidgetIntent = new Intent(context, WebShot.class);
        reloadWidgetIntent.putExtra("dark_theme", false);
        reloadWidgetIntent.putExtra("current_ids", currentIds);
        PendingIntent pendingReloadWidgetIntent = PendingIntent.getActivity(context, 0, reloadWidgetIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.default_timetable_widget);
        views.setOnClickPendingIntent(R.id.widget_layout, pendingReloadWidgetIntent);
        AppWidgetManager.getInstance(context).updateAppWidget(currentIds, views);
    }
}

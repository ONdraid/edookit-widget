package com.vomaon.edookit.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class EdookitWidgetProviderDark extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        final int[] currentIds = appWidgetManager.getAppWidgetIds(
                new ComponentName(context, EdookitWidgetProviderDark.class));

        if (currentIds.length < 1) {
            return;
        }

        Intent reloadWidgetIntent = new Intent(context, WebShot.class);
        reloadWidgetIntent.putExtra("dark_theme", true);
        reloadWidgetIntent.putExtra("current_ids", currentIds);
        PendingIntent pendingReloadDarkWidgetIntent = PendingIntent.getActivity(context, 0, reloadWidgetIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.default_timetable_dark_widget);
        views.setOnClickPendingIntent(R.id.widget_dark_layout, pendingReloadDarkWidgetIntent);
        AppWidgetManager.getInstance(context).updateAppWidget(currentIds, views);
    }
}

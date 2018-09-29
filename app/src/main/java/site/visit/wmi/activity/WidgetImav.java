package site.visit.wmi.activity;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import site.visit.wmi.R;
import site.visit.wmi.helper.SQLiteHandler;
import site.visit.wmi.helper.SessionManager;
import site.visit.wmi.visit.myVisit;

import static site.visit.wmi.app.AppConfig.URL_MY_VISIT;

/**
 * Created by NOC WMI on 17/11/2016.
 */

public class WidgetImav extends AppWidgetProvider {
    private SQLiteHandler db;
    private SessionManager session;

        DateFormat df = new SimpleDateFormat("hh:mm:ss");

        public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
            final int N = appWidgetIds.length;

            Log.i("ExampleWidget",  "Updating widgets " + Arrays.asList(appWidgetIds));

            // Perform this loop procedure for each App Widget that belongs to this
            // provider
            for (int i = 0; i < N; i++) {
                int appWidgetId = appWidgetIds[i];

                // Create an Intent to launch ExampleActivity
                Intent intent = new Intent(context, SettingActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

                // Get the layout for the App Widget and attach an on-click listener
                // to the button
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_imav);
                views.setOnClickPendingIntent(R.id.btnAdd, pendingIntent);

                // To update a label
                views.setTextViewText(R.id.textView33, df.format(new Date()));

                // Tell the AppWidgetManager to perform an update on the current app
                // widget
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        }
    }
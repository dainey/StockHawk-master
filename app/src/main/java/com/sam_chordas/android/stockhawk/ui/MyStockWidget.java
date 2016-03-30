package com.sam_chordas.android.stockhawk.ui;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.service.StockWidgetService;

import java.util.Random;

/**
 * Created by ogayle on 28/03/2016.
 */
public class MyStockWidget extends AppWidgetProvider {


    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (intent.getAction().equals(Utils.ACTION_DATA_UPDATED)) {
            AppWidgetManager appWManager = AppWidgetManager.getInstance(context);
            int[] ids = appWManager.getAppWidgetIds(new ComponentName(context, getClass()));
            appWManager.notifyAppWidgetViewDataChanged(ids, R.id.widget_list_view);
        }

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        if (appWidgetIds.length > 0)
            for (int id : appWidgetIds) {

                RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

                //Intent to launch Main app
                Intent mainStocks = new Intent(context, MyStocksActivity.class);
                PendingIntent mainPI = PendingIntent.getActivity(context, 0, mainStocks, 0);
                remoteViews.setOnClickPendingIntent(R.id.banner, mainPI);

                //Intent to Update Remote View Data
                Intent intent = new Intent(context, StockWidgetService.class);
                remoteViews.setRemoteAdapter(R.id.widget_list_view, intent);
                remoteViews.setEmptyView(R.id.widget_list_view, R.id.empty_view);

                boolean useDetailsView = context.getResources().getBoolean(R.bool.use_details_view);

                //Template Intent to access individual Items
                Intent fireIntent = useDetailsView ? new Intent(context, MyStockDetailsActivity.class)
                                                    : new Intent(context, MyStocksActivity.class);
                PendingIntent fireIntentTemplate = TaskStackBuilder.create(context)
                        .addNextIntentWithParentStack(fireIntent)
                        .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                remoteViews.setPendingIntentTemplate(R.id.widget_list_view, fireIntentTemplate);


                appWidgetManager.updateAppWidget(id, remoteViews);

            }


    }
}

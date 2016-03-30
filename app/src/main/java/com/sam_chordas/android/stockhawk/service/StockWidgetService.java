package com.sam_chordas.android.stockhawk.service;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.rest.StockRemoteViewFactory;

/**
 * Created by ogayle on 28/03/2016.
 */
public class StockWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return (new StockRemoteViewFactory(this.getApplicationContext()));
    }
}

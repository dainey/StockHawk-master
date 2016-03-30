package com.sam_chordas.android.stockhawk.rest;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.model.Stock;

import java.util.ArrayList;

/**
 * Created by ogayle on 28/03/2016.
 */
public class StockRemoteViewFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context context = null;
    private ArrayList<Stock> stocks;

    public StockRemoteViewFactory(Context c) {
        stocks = null;
        context = c;
    }

    private void getStockData(Context appContext) {
        Cursor stockCursor = appContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                null, QuoteColumns.ISCURRENT + "= ?",
                new String[]{"1"}, null);

        if (stockCursor == null)
            return;

        if (!stockCursor.moveToFirst()) {
            stockCursor.close();
            return;
        }

        stocks = Utils.getStocksFromCursor(stockCursor, appContext);
        stockCursor.close();
    }

    @Override
    public int getCount() {
        return stocks == null ? 0 : stocks.size();
    }

    @Override
    public void onCreate() {
        getStockData(context);
    }

    @Override
    public void onDataSetChanged() {
        final long token = Binder.clearCallingIdentity();
        getStockData(context);
        Binder.restoreCallingIdentity(token);
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public RemoteViews getViewAt(int position) {

        if (stocks == null) return null;

        final RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.widget_line_item);
        Stock s = stocks.get(position);
        remoteView.setTextViewText(R.id.widget_stock_symbol, s.symbol);
        remoteView.setTextViewText(R.id.widget_stock_company, s.name);
        remoteView.setTextViewText(R.id.widget_bid_price, s.bidPrice);
        remoteView.setTextViewText(R.id.widget_change, s.change);
        //remoteView.setTextViewText(R.id.stock_last_updated, s.lastUpdated);

        remoteView.setInt(R.id.widget_change, "setBackgroundResource", s.isUp? R.drawable.percent_change_pill_green : R.drawable.percent_change_pill_red);
        int sdk = Build.VERSION.SDK_INT;


        Intent fillInIntent = new Intent();
        fillInIntent.setData(QuoteProvider.Quotes.withSymbol(s.symbol));
        remoteView.setOnClickFillInIntent(R.id.line_item, fillInIntent);

        return remoteView;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        if (stocks != null)
            return stocks.get(position).id;

        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}

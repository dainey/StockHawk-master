package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.util.Log;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.model.Stock;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

    public static final int STOCK_DETAILS_LOADER = 1;


    public static final String ACTION_DATA_UPDATED = "com.sam_chordas.android.stockhawk.DATA_UPDATED";
    public static final String STOCK_DETAILS_URI = "stock_details_uri";
    public static final String PERIODIC = "periodic";
    public static final String INIT = "initialize";
    public static final String ADD = "add";


    private static String LOG_TAG = Utils.class.getSimpleName();

    public static boolean showPercent = true;

    public static ArrayList quoteJsonToContentVals(String JSON) {
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
        JSONObject jsonObject = null;
        JSONArray resultsArray = null;
        try {
            jsonObject = new JSONObject(JSON);
            if (jsonObject != null && jsonObject.length() != 0) {
                jsonObject = jsonObject.getJSONObject("query");
                int count = Integer.parseInt(jsonObject.getString("count"));
                if (count == 1) {
                    jsonObject = jsonObject.getJSONObject("results").getJSONObject("quote");

                    batchOperations.add(buildBatchOperation(jsonObject));
                } else {
                    resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

                    if (resultsArray != null && resultsArray.length() != 0) {
                        for (int i = 0; i < resultsArray.length(); i++) {
                            jsonObject = resultsArray.getJSONObject(i);
                            batchOperations.add(buildBatchOperation(jsonObject));
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "quoteJsonToContentVals");
            e.printStackTrace();
        }
        return batchOperations;
    }

    public static String truncateBidPrice(String bidPrice) throws Exception {
        try {
            bidPrice = String.format(Locale.getDefault(), "%.2f", Float.parseFloat(bidPrice));
        } catch (NumberFormatException nfe) {
            Log.e(LOG_TAG, "truncateBidPrice");
            nfe.printStackTrace();
            throw new Exception("Failed to Convert floating point entry");
        }
        return bidPrice;
    }

    public static String truncateChange(String change, boolean isPercentChange) {
        String weight = change.substring(0, 1);
        String ampersand = "";
        if (isPercentChange) {
            ampersand = change.substring(change.length() - 1, change.length());
            change = change.substring(0, change.length() - 1);
        }
        change = change.substring(1, change.length());
        double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
        change = String.format(Locale.getDefault(), "%.2f", round);
        StringBuffer changeBuffer = new StringBuffer(change);
        changeBuffer.insert(0, weight);
        changeBuffer.append(ampersand);
        change = changeBuffer.toString();
        return change;
    }

    public static ContentProviderOperation buildBatchOperation(JSONObject jsonObject)
            throws Exception {
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                QuoteProvider.Quotes.CONTENT_URI);
        try {
            String change = jsonObject.getString("Change");
            builder.withValue(QuoteColumns.SYMBOL, jsonObject.getString("symbol"));
            builder.withValue(QuoteColumns.NAME, jsonObject.getString("Name"));
            builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(jsonObject.getString("Bid")));
            builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(jsonObject.getString("ChangeinPercent"), true));
            builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
            builder.withValue(QuoteColumns.ISCURRENT, 1);
            if (change.charAt(0) == '-') {
                builder.withValue(QuoteColumns.ISUP, 0);
            } else {
                builder.withValue(QuoteColumns.ISUP, 1);
            }

            builder.withValue(QuoteColumns.ASKING_PRICE, jsonObject.getString("Ask"));
            builder.withValue(QuoteColumns.CURRENCY, jsonObject.getString("Currency"));
            builder.withValue(QuoteColumns.LAST_TRADE_DATE, jsonObject.getString("LastTradeDate"));
            builder.withValue(QuoteColumns.YEAR_HIGH, jsonObject.getString("YearHigh"));
            builder.withValue(QuoteColumns.YEAR_LOW, jsonObject.getString("YearLow"));


            Calendar calendar = Calendar.getInstance();
            DateFormat iso8601DateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.getDefault());
            iso8601DateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            String date = iso8601DateFormat.format(calendar.getTime());
            builder.withValue(QuoteColumns.LAST_UPDATED, date);

        } catch (Exception e) {
            Log.e(LOG_TAG, "buildBatchOperation");
            e.printStackTrace();
            throw new Exception("Data Processing error during buildBatchOperation");
        }
        return builder.build();
    }

    public static Stock getStockFromCursor(Cursor cursor, Context mContext) {

        Stock stock = new Stock();
        int index = cursor.getColumnIndex(QuoteColumns.SYMBOL);
        stock.symbol = cursor.getString(index);
        stock.bidPrice = cursor.getString(cursor.getColumnIndex(QuoteColumns.BIDPRICE));
        stock.name = cursor.getString(cursor.getColumnIndex(QuoteColumns.NAME));
        stock.id = cursor.getInt(cursor.getColumnIndex(QuoteColumns._ID));
        String date = cursor.getString(cursor.getColumnIndex(QuoteColumns.LAST_UPDATED));
        String dateStr = null;
        String timeStr = null;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.US);
        try {
            calendar.setTime(sdf.parse(date));
            calendar.setTimeZone(TimeZone.getDefault());
            calendar.set(Calendar.HOUR_OF_DAY, Calendar.AM_PM + Calendar.HOUR);
            Date dateTime = calendar.getTime();

            sdf = new SimpleDateFormat("EEEE MMM yyyy", Locale.US);
            dateStr = sdf.format(dateTime);

            sdf = new SimpleDateFormat("hh:mm:ss a", Locale.US);
            timeStr = sdf.format(dateTime);

        } catch (ParseException pe) {
            Log.e(LOG_TAG, "onBindViewHolder");
            pe.printStackTrace();
        }
        stock.lastUpdated = String.format(Locale.getDefault(), "%s : %s %s", mContext.getResources().getString(R.string.last_updated), (dateStr == null ? "--:--:--" : dateStr), (timeStr == null ? "--:--:--" : timeStr));


        stock.percentChange = cursor.getString(cursor.getColumnIndex(QuoteColumns.PERCENT_CHANGE));

        stock.change = cursor.getString(cursor.getColumnIndex(QuoteColumns.CHANGE));
        stock.askingPrice = cursor.getString(cursor.getColumnIndex(QuoteColumns.ASKING_PRICE));
        stock.currency = cursor.getString(cursor.getColumnIndex(QuoteColumns.CURRENCY));
        stock.lastTradeDate = cursor.getString(cursor.getColumnIndex(QuoteColumns.LAST_TRADE_DATE));
        stock.yearHigh = cursor.getString(cursor.getColumnIndex(QuoteColumns.YEAR_HIGH));
        stock.yearLow = cursor.getString(cursor.getColumnIndex(QuoteColumns.YEAR_LOW));

        return stock;
    }

    public static ArrayList<Stock> getStocksFromCursor(Cursor c, Context context){
        ArrayList<Stock> stocks = new ArrayList<>();
        if(c.moveToFirst())
        do
        {
            stocks.add(getStockFromCursor(c, context));
        }while(c.moveToNext());


        return stocks;
    }

}

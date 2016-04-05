package com.sam_chordas.android.stockhawk.ui;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.FloatRange;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.model.Stock;
import com.sam_chordas.android.stockhawk.rest.Utils;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by ogayle on 28/03/2016.
 */
public class MyStockDetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private String LOG_TAG = MyStockDetailsFragment.class.getSimpleName();
    private Uri stockUri;


    private static String[] STOCK_DETAILS_COLUMNS = new String[]{
            QuoteColumns._ID,
            QuoteColumns.SYMBOL,
            QuoteColumns.NAME,
            QuoteColumns.BIDPRICE,
            QuoteColumns.PERCENT_CHANGE,
            QuoteColumns.CHANGE,
            QuoteColumns.ISUP,
            QuoteColumns.LAST_UPDATED,
            QuoteColumns.ISCURRENT,
            QuoteColumns.ASKING_PRICE,
            QuoteColumns.CURRENCY,
            QuoteColumns.LAST_TRADE_DATE,
            QuoteColumns.YEAR_HIGH,
            QuoteColumns.YEAR_LOW
    };


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle myArgs = getArguments();
        if (myArgs != null) {
            stockUri = myArgs.getParcelable(Utils.STOCK_DETAILS_URI);
        }
        return inflater.inflate(R.layout.fragment_stock_details, container, false);
    }


    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(Utils.STOCK_DETAILS_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (stockUri == null)
            return null;
        return new CursorLoader(getContext(), stockUri, STOCK_DETAILS_COLUMNS, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null || getView() == null)
            return;

        if (!data.moveToFirst())
            return;

        ArrayList<Stock> stocks = Utils.getStocksFromCursor(data, getContext());

        int i = stocks.size();
        String[] labels = new String[i];
        float[] values = new float[i];
        for (int x = 0; x < i; x++) {

            labels[x] = String.valueOf(x + 1);
            values[x] = Float.parseFloat(stocks.get(x).change);
        }

        Typeface robotoLight = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Light.ttf");
        Typeface robotoThin = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Thin.ttf");
        Typeface robotoBold = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Bold.ttf");
        Typeface robotoRegular = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Regular.ttf");

        TextView symbols = (TextView) getView().findViewById(R.id.detail_stock_symbol);
        symbols.setText(stocks.get(i-1).symbol);
        symbols.setTypeface(robotoBold);

        TextView companyName = (TextView) getView().findViewById(R.id.detail_stock_company);
        companyName.setText(stocks.get(i-1).name);
        companyName.setTypeface(robotoLight);

        String currencyTxt = String.format(Locale.getDefault(), "%s %s", getContext().getString(R.string.asking_price_txt), stocks.get(i - 1).currency);
        TextView currency = (TextView) getView().findViewById(R.id.currency);
        currency.setText(currencyTxt);
        currency.setTypeface(robotoRegular);

        TextView askingPrice = (TextView) getView().findViewById(R.id.asking_price);
        askingPrice.setText(stocks.get(i - 1).askingPrice);
        askingPrice.setTypeface(robotoBold);


        String lastTradeText = String.format(Locale.getDefault(), "%s %s", getContext().getString(R.string.last_trade_date_txt), stocks.get(i - 1).lastTradeDate);
        TextView lastTradeDate = (TextView) getView().findViewById(R.id.last_trade_date);
        lastTradeDate.setText(lastTradeText);
        lastTradeDate.setTypeface(robotoRegular);

        String highText = String.format(Locale.getDefault(), "%s %s", getContext().getString(R.string.year_high_txt), stocks.get(i - 1).yearHigh);
        TextView yearHigh = (TextView) getView().findViewById(R.id.year_high);
        yearHigh.setText(highText);
        yearHigh.setTypeface(robotoRegular);


        String lowText = String.format(Locale.getDefault(), "%s %s", getContext().getString(R.string.year_low_txt), stocks.get(i - 1).yearLow);
        TextView yearLow = (TextView) getView().findViewById(R.id.year_low);
        yearLow.setText(lowText);
        yearLow.setTypeface(robotoRegular);


        LineSet chartData = new LineSet(labels, values);
        chartData.setColor(ContextCompat.getColor(getContext(), R.color.chart_data_color))
                .setDotsColor(ContextCompat.getColor(getContext(), R.color.stock_chart_dot))
                .setThickness(2)
                .setDashed(new float[]{10f, 10f})
                .beginAt(0);

        LineChartView lineChart = (LineChartView) getView().findViewById(R.id.linechart);

        if (lineChart == null) return;


        lineChart.addData(chartData);

        lineChart.setBorderSpacing(Tools.fromDpToPx(1))
                .setTypeface(robotoBold)
                .setYLabels(AxisController.LabelPosition.OUTSIDE)
                .setXLabels(AxisController.LabelPosition.NONE)
                .setLabelsColor(ContextCompat.getColor(getContext(), R.color.material_blue_700))
                .setAxisColor(ContextCompat.getColor(getContext(), R.color.material_blue_500))
                .setXAxis(true)
                .setYAxis(true);
        lineChart.show();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}

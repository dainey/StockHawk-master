package com.sam_chordas.android.stockhawk.rest;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.model.Stock;
import com.sam_chordas.android.stockhawk.touch_helper.ItemTouchHelperAdapter;
import com.sam_chordas.android.stockhawk.touch_helper.ItemTouchHelperViewHolder;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by sam_chordas on 10/6/15.
 *  Credit to skyfishjy gist:
 *    https://gist.github.com/skyfishjy/443b7448f59be978bc59
 * for the code structure
 */
public class QuoteCursorAdapter extends CursorRecyclerViewAdapter<QuoteCursorAdapter.ViewHolder>
    implements ItemTouchHelperAdapter{

  private String LOG_TAG = QuoteCursorAdapter.class.getSimpleName();
  private static Context mContext;
  private static Typeface robotoLight;
  private boolean isPercent;

  private StockSelection defaultSelection = new StockSelection() {
    @Override
    public void selectStockItem(String symbol) {
      return;
    }
  };
  public StockSelection stockSelection = defaultSelection;

  public QuoteCursorAdapter(Context context, Cursor cursor){
    super(context, cursor);
    mContext = context;

    if(mContext instanceof StockSelection)
      stockSelection = (StockSelection) mContext;
  }

  @Override
  public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
    stockSelection = defaultSelection;
    super.onDetachedFromRecyclerView(recyclerView);
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
  {
    robotoLight = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Light.ttf");
    View itemView = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.list_item_quote, parent, false);
    ViewHolder vh = new ViewHolder(itemView);
    return vh;
  }

  @Override
  public void onBindViewHolder(final ViewHolder viewHolder, final Cursor cursor)
  {
    final Stock s = new Stock();
    s.symbol = cursor.getString(cursor.getColumnIndex(QuoteColumns.SYMBOL));
    viewHolder.symbol.setText(cursor.getString(cursor.getColumnIndex(QuoteColumns.SYMBOL)));
    viewHolder.bidPrice.setText(cursor.getString(cursor.getColumnIndex(QuoteColumns.BIDPRICE)));
    viewHolder.companyName.setText(cursor.getString(cursor.getColumnIndex(QuoteColumns.NAME)));
    viewHolder.container.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Log.e("onBindViewHolder", s.symbol);
        stockSelection.selectStockItem(s.symbol);
      }
    });
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

    } catch (ParseException pe)
    {
      Log.e(LOG_TAG, "onBindViewHolder");
      pe.printStackTrace();
    }
    String formattedDateTime = String.format(Locale.getDefault(), "%s : %s %s",mContext.getResources().getString(R.string.last_updated), (dateStr == null ? "--:--:--" : dateStr),(timeStr == null ? "--:--:--" : timeStr));
    viewHolder.lastUpdated.setText(formattedDateTime);


    int sdk = Build.VERSION.SDK_INT;

    if (cursor.getInt(cursor.getColumnIndex(QuoteColumns.ISUP)) == 1)
    {
      if (sdk < Build.VERSION_CODES.JELLY_BEAN){
        viewHolder.change.setBackgroundDrawable(
            mContext.getResources().getDrawable(R.drawable.percent_change_pill_green));
      }else {
        viewHolder.change.setBackground(
            mContext.getResources().getDrawable(R.drawable.percent_change_pill_green));
      }
    } else{
      if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
        viewHolder.change.setBackgroundDrawable(
            mContext.getResources().getDrawable(R.drawable.percent_change_pill_red));
      } else{
        viewHolder.change.setBackground(
            mContext.getResources().getDrawable(R.drawable.percent_change_pill_red));
      }
    }
    if (Utils.showPercent){
      viewHolder.change.setText(cursor.getString(cursor.getColumnIndex(QuoteColumns.PERCENT_CHANGE)));
    } else{
      viewHolder.change.setText(cursor.getString(cursor.getColumnIndex(QuoteColumns.CHANGE)));
    }
  }

  @Override public void onItemDismiss(int position) {
    Cursor c = getCursor();
    c.moveToPosition(position);
    String symbol = c.getString(c.getColumnIndex(QuoteColumns.SYMBOL));
    mContext.getContentResolver().delete(QuoteProvider.Quotes.withSymbol(symbol), null, null);
    notifyItemRemoved(position);
  }

  @Override public int getItemCount() {
    return super.getItemCount();
  }

  public static class ViewHolder
          extends RecyclerView.ViewHolder
          implements ItemTouchHelperViewHolder, View.OnClickListener
  {
    public final TextView symbol;
    public final TextView companyName;
    public final TextView bidPrice;
    public final TextView change;
    public final TextView lastUpdated;
    public final View container;

    public ViewHolder(View itemView)
    {
      super(itemView);
      symbol = (TextView) itemView.findViewById(R.id.stock_symbol);
      symbol.setTypeface(robotoLight);
      container = itemView;
      companyName = (TextView) itemView.findViewById(R.id.stock_company);
      lastUpdated = (TextView) itemView.findViewById(R.id.stock_last_updated);
      bidPrice = (TextView) itemView.findViewById(R.id.bid_price);
      change = (TextView) itemView.findViewById(R.id.change);
    }

    @Override
    public void onItemSelected(){
      itemView.setBackgroundColor(Color.LTGRAY);
    }

    @Override
    public void onItemClear(){
      itemView.setBackgroundColor(0);
    }

    @Override
    public void onClick(View v) {
      Log.e("ViewHolder", symbol.getText().toString());
    }
  }


}

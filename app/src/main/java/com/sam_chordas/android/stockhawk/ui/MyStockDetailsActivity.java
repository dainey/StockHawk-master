package com.sam_chordas.android.stockhawk.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.rest.Utils;

/**
 * Created by ogayle on 28/03/2016.
 */
public class MyStockDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_line_graph);
        Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
        if (tb != null)
            setSupportActionBar(tb);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        if (savedInstanceState != null)
            return;

        Bundle bundle = new Bundle();
        bundle.putParcelable(Utils.STOCK_DETAILS_URI, getIntent().getData());
        MyStockDetailsFragment stockDF = new MyStockDetailsFragment();
        stockDF.setArguments(bundle);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.stock_details_container, stockDF)
                .commit();
        getSupportFragmentManager().executePendingTransactions();
    }
}

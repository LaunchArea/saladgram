package com.saladgram.assemble;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction() == Service.ACTION_FETCH_FAILED) {
                Toast.makeText(context, intent.getStringExtra("reason"), Toast.LENGTH_SHORT).show();
            } else if (intent.getAction() == Service.ACTION_FETCH_DONE) {
                refreshUI();
            }
        }
    };
    private TextView tvTotalOrders;
    private TextView tvCurruntOrder;
    private RecyclerView lvOrders;
    private RecyclerView lvItems;
    private Button btnReady;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initControl();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Service.ACTION_FETCH_FAILED);
        filter.addAction(Service.ACTION_FETCH_DONE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);
    }

    private void initView() {
        tvTotalOrders = (TextView) findViewById(R.id.total_orders);
        tvCurruntOrder = (TextView) findViewById(R.id.currunt_order);
        lvOrders = (RecyclerView) findViewById(R.id.order_list);
        lvItems = (RecyclerView) findViewById(R.id.item_list);
        btnReady = (Button) findViewById(R.id.ready_button);
    }

    private void initControl() {
//        btnReady.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                postReady();
//            }
//        });
//
//        lvItems.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private Context getActivity() {
        return this;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    private void refreshUI() {
        tvTotalOrders.setText(""+Service.orderList.size());
    }
}

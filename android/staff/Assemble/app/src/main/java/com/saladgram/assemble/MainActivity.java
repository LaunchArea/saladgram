package com.saladgram.assemble;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.util.LinkedList;
import java.util.List;

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
    private TextView tvOrderSummary;
    private RecyclerView lvOrders;
    private RecyclerView lvItems;
    private Button btnReady;

    private RecyclerViewClickListener mOrderClickListener;
    private RecyclerViewClickListener mOrderItemClickListener;
    private OrderAdapter mOrderAdapter;
    private OrderItemAdapter mOrderItemAdapter;

    private List<Order> mOrderList;
    private int mSelectedId;

    private Order mSelectedOrder;
    private TextView tvSelectedOrderId;

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
        tvOrderSummary = (TextView) findViewById(R.id.orderSummary);
        lvOrders = (RecyclerView) findViewById(R.id.order_list);
        lvItems = (RecyclerView) findViewById(R.id.item_list);
        btnReady = (Button) findViewById(R.id.ready_button);
        tvSelectedOrderId = (TextView) findViewById(R.id.selected_order_id);
    }

    private void initControl() {
        btnReady.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postReady();
            }
        });

        mOrderClickListener = new RecyclerViewClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                mSelectedId = mOrderList.get(position).id;
                refreshUI();
            }

            @Override
            public void onItemLongClick(View view, int position) {
                Order item = mOrderList.get(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                try {
                    builder.setMessage(item.json.toString(2));
                } catch (JSONException e) {
                    builder.setMessage(e.getMessage());
                    e.printStackTrace();
                }
                builder.show();
            }
        };

        mOrderItemClickListener = new RecyclerViewClickListener() {
            @Override
            public void onItemClick(View view, int position) {
            }

            @Override
            public void onItemLongClick(View view, int position) {
                OrderItem item = mSelectedOrder.orderItems.get(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                try {
                    builder.setMessage(item.json.toString(2));
                } catch (JSONException e) {
                    builder.setMessage(e.getMessage());
                    e.printStackTrace();
                }
                builder.show();
            }
        };

        lvOrders.setLayoutManager(new LinearLayoutManager(getActivity()));
        lvItems.setLayoutManager(new LinearLayoutManager(getActivity()));

        mOrderAdapter = new OrderAdapter(this,mOrderClickListener);
        lvOrders.setAdapter(mOrderAdapter);

        mOrderItemAdapter = new OrderItemAdapter(this, mOrderItemClickListener);
        lvItems.setAdapter(mOrderItemAdapter);
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
        mOrderList = new LinkedList<>(Service.orderList);

        mSelectedOrder = null;
        for (Order order : mOrderList) {
            if (order.id == mSelectedId) {
                mSelectedOrder = order;
                break;
            }
        }

        mOrderAdapter.setSelectedId(mSelectedId);
        mOrderAdapter.setList(mOrderList);
        mOrderAdapter.notifyDataSetChanged();

        if (mSelectedOrder != null) {
            mOrderItemAdapter.setList(mSelectedOrder.orderItems);
            mOrderItemAdapter.notifyDataSetChanged();
            tvSelectedOrderId.setText(""+mSelectedOrder.id + " " + mSelectedOrder.type.name());
        }

        int[] arr = new int[4];
        for (Order order : mOrderList) {
            for (OrderItem item: order.orderItems) {
                arr[item.type.ordinal()]++;
            }
        }
        tvOrderSummary.setText("총주문:" + mOrderList.size()
                + " 샐러드:"+arr[0]
                + " 스프:" +arr[1]
                + " 그외:"+arr[2]
                + " 음료:"+arr[3]
        );
    }

    private void postReady() {
        for(Order order : mOrderList) {
            if(order.id > mSelectedId) {
                mSelectedId = order.id;
                break;
            }
        }
        refreshUI();
    }

}

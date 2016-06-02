package com.saladgram.deliver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.saladgram.model.Order;

import org.json.JSONException;

import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView lvOrders;

    private RecyclerViewClickListener mOrderClickListener;
    private OrderAdapter mOrderAdapter;

    private List<Order> mOrderList = new LinkedList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction() == Service.ACTION_FETCH_FAILED) {
                Toast.makeText(context, intent.getStringExtra("reason"), Toast.LENGTH_SHORT).show();
            }
        }
    };
    private String mDelivererId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initControl();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Service.ACTION_FETCH_FAILED);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);

        checkDeliverId();
    }

    private void checkDeliverId() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("deliverer_id");
        final EditText input = new EditText(this);
        alert.setView(input);
        alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if(input.getText().length() > 0) {
                    mDelivererId = input.getText().toString();
                    mSwipeRefreshLayout.setEnabled(true);
                    refreshUI();
                }
            }
        });
        alert.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    private void initView() {
        lvOrders = (RecyclerView) findViewById(R.id.order_list);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setEnabled(false);
    }

    private void initControl() {

        mOrderClickListener = new RecyclerViewClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                postDone(mOrderList.get(position));
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

        lvOrders.setLayoutManager(new LinearLayoutManager(getActivity()));

        mOrderAdapter = new OrderAdapter(this,mOrderClickListener);
        mOrderAdapter.setList(mOrderList);
        lvOrders.setAdapter(mOrderAdapter);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshUI();
            }
        });
    }

    private void postDone(Order order) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(mDelivererId);
        builder.setMessage("" + order.id);
        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    private Context getActivity() {
        return this;
    }

    private void refreshUI() {
        ProgressAsyncTask<Void, Void, Void> task = new ProgressAsyncTask<Void, Void, Void>(getActivity()) {

            @Override
            protected Void doInBackground(Void... params) {
                Service.onetimeFetch();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                mOrderList = new LinkedList<>(Service.orderList);

                mOrderAdapter.setList(mOrderList);
                mOrderAdapter.notifyDataSetChanged();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        };
        task.execute();
    }
}

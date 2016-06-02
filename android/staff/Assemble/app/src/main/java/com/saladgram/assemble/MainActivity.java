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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.saladgram.model.Order;
import com.saladgram.model.OrderItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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
            tvSelectedOrderId.setText(""+mSelectedOrder.id + " " + mSelectedOrder.orderType.name());
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
        UpdateStatusTask task = new UpdateStatusTask(getActivity(), mSelectedId) {
            @Override
            protected void onPostExecute(Integer code) {
                super.onPostExecute(code);
                if(code == 200) {
                    for(Order order : mOrderList) {
                        if(order.id > mSelectedId) {
                            mSelectedId = order.id;
                            break;
                        }
                    }
                    refreshUI();
                } else {
                    Toast.makeText(getActivity(), "error" + code, Toast.LENGTH_SHORT).show();
                }
            }
        };
        task.execute();
    }
    class UpdateStatusTask extends ProgressAsyncTask<Void, Void, Integer> {
        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");
        private final int mId;

        public UpdateStatusTask(Context context, int id) {
            super(context);
            this.mId = id;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient();

            try {
                JSONObject signInJson = new JSONObject();

                signInJson.put("id", "saladgram");
                signInJson.put("password", "saladgram");
                RequestBody body = RequestBody.create(JSON, signInJson.toString());

                String url = "https://saladgram.com/api/sign_in.php";
                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();

                Response response = null;
                response = client.newCall(request).execute();
                if (response.code() != 200) {
                    return response.code();
                }
                String jwt = new JSONObject(response.body().string()).getString("jwt");

                HashMap<String, Object> m = new HashMap<>();

                m.put("order_id", mId);
                m.put("status", Order.Status.READY.ordinal() + 1);

                JSONObject json = new JSONObject(m);
                body = RequestBody.create(JSON, json.toString());
                Log.d("yns", json.toString(2));
                url = "https://www.saladgram.com/api/update_order.php";
                request = new Request.Builder()
                        .header("jwt", jwt)
                        .url(url)
                        .post(body)
                        .build();

                response = client.newCall(request).execute();
                Log.d("yns", response.body().string());
                if (response.code() == 200) {
                    try {
                        Service.update();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), e.getMessage() + " at order refresh", Toast.LENGTH_SHORT).show();
                    }
                }
                return response.code();

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return -1;
        }
    }
}

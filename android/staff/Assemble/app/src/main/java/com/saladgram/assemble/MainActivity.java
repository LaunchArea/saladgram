package com.saladgram.assemble;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static String mMirrorIP = "192.168.0.5";

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction() == Service.ACTION_FETCH_FAILED) {
                Toast.makeText(context, intent.getStringExtra("reason"), Toast.LENGTH_SHORT).show();
            } else if (intent.getAction() == Service.ACTION_FETCH_DONE) {
                refreshUI();
                int now = Service.orderList.size();
                if (previousCount != -1 && previousCount < now) {
                    doNotification();
                }
                previousCount = now;
            }
        }
    };

    private int previousCount = -1;
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

        int[] arr = new int[6];
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

        if (mSelectedOrder != null && (mSelectedOrder.orderType == Order.OrderType.DINE_IN || mSelectedOrder.orderType == Order.OrderType.TAKE_OUT)) {
            btnReady.setText("DONE");
        } else {
            btnReady.setText("READY");
        }
    }

    private void postReady() {
        UpdateStatusTask task = new UpdateStatusTask(getActivity(), mSelectedOrder) {
            @Override
            protected void onPostExecute(Integer code) {
                super.onPostExecute(code);
                if(code == 200) {
                    if (mOrderList.size() > 0) {
                        mSelectedId = mOrderList.get(0).id;
                    } else {
                        mSelectedId = -1;
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
        private final Order mOrder;

        public UpdateStatusTask(Context context, Order order) {
            super(context);
            this.mOrder = order;
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

                m.put("order_id", mOrder.id);
                if (mOrder.orderType == Order.OrderType.DINE_IN || mOrder.orderType == Order.OrderType.TAKE_OUT) {
                    m.put("status", Order.Status.DONE.ordinal() + 1);
                } else {
                    m.put("status", Order.Status.READY.ordinal() + 1);
                    sendToPrinter(mOrder);
                }

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


    private void doNotification() {
            Notification.Builder builder = new Notification.Builder(this);

            // 작은 아이콘 이미지.
            builder.setSmallIcon(R.mipmap.ic_launcher);

            // 알림 출력 시간.
            builder.setWhen(System.currentTimeMillis());
            builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS);
            builder.setAutoCancel(true);

            // 우선순위.
            builder.setPriority(Notification.PRIORITY_MAX);

            // 고유ID로 알림을 생성.
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nm.notify(123456, builder.build());

    }

    private static CharSequence formatTime(Date reservation_time) {
        return new SimpleDateFormat("MM/dd HH:mm:ss", Locale.KOREA).format(reservation_time);
    }

    private void sendToPrinter(Order order) {
        if (mMirrorIP == null) {
            return;
        }

        StringBuffer buffer = new StringBuffer();

        buffer.append("" + order.id + "/" + order.orderType.name().toLowerCase() + "\n");
        buffer.append("주소 : " + order.addr + "\n");
        buffer.append("결제 : " + order.paymentType.name() + " " + order.actual_price + "원\n");
        if (order.reservation_time.getTime() > 0) {
            buffer.append("예약 : " + formatTime(order.reservation_time) + "\n");
        }
        buffer.append("\n");
        for (OrderItem item : order.orderItems) {
            buffer.append(item.name + " ");
            buffer.append(item.amount != null ? item.amount : " ");
            buffer.append(" x " + item.quantity + "\n");
        }
        buffer.append("\n\n\n\n\n");


        HashMap<String,Object> map = new HashMap<>();
        map.put("data", buffer.toString());

        final String message = new JSONObject(map).toString();
        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    InetAddress serverAddr = InetAddress.getByName(mMirrorIP);
                    Socket socket = new Socket(serverAddr, 6000);
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                    out.println(message);
                    out.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }

                return true;
            }

            @Override
            protected void onPostExecute(Boolean succ) {
                super.onPostExecute(succ);
                if (!succ) {
                    Toast.makeText(getActivity(), "Mirror failed", Toast.LENGTH_SHORT).show();
                }
            }
        };
        task.execute();
    }
}

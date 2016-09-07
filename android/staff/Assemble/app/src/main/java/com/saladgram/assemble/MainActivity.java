package com.saladgram.assemble;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.SortedList;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.saladgram.model.MenuItem;
import com.saladgram.model.Order;
import com.saladgram.model.OrderItem;
import com.saladgram.model.SaladItem;
import com.saladgram.model.SaleItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.annotation.Target;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
                int now = Service.orderList.size();
                if (previousCount != -1 && previousCount < now) {
                    doNotification();
                }
                previousCount = now;
            }
        }
    };

    private List<com.saladgram.model.MenuItem> mMenuList = new LinkedList<>();

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

        new MenuListFetchTask(this).execute();
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
                final Order item = mOrderList.get(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                try {
                    builder.setMessage(item.json.toString(2));
                } catch (JSONException e) {
                    builder.setMessage(e.getMessage());
                    e.printStackTrace();
                }
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        confirmCancel(item.id);
                    }
                });
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

        findViewById(R.id.reservation_time_set_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setReservationTime();
            }
        });

        findViewById(R.id.show_ingredients_button).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                showIngredients();
            }
        });
    }

    private void showIngredients() {
        Map<String, Map<String, Integer>> map = new HashMap<>();
        map.put("재료", new HashMap<String,Integer>());
        map.put("드레싱", new HashMap<String,Integer>());
        map.put("스프", new HashMap<String,Integer>());
        map.put("아더", new HashMap<String,Integer>());

        for(Order order : mOrderList) {
            for(OrderItem item : order.orderItems) {
                String key;
                Map<String, Integer> m;
                int amount;

                m = null;
                key = "unknown";
                amount = 1;

                switch(item.type) {
                    case SALAD:
                        for (SaladItem sItem : item.saladItems) {
                            if (sItem.type == SaladItem.Type.DRESSINGS) {
                                m = map.get("드레싱");
                                key = sItem.name + sItem.amount;
                                amount = 1 * item.quantity;
                            } else {
                                m = map.get("재료");
                                key = sItem.name;
                                amount = Integer.parseInt(sItem.amount.replace("g","")) * item.quantity;
                            }
                            if(!m.containsKey(key)) {
                                m.put(key, 0);
                            }
                            m.put(key, (Integer)m.get(key) + amount);
                        }
                        break;
                    case SOUP:
                        m = map.get("스프");
                        key = item.name;
                        amount = Integer.parseInt(item.amount.replace("g", "")) * item.quantity;
                        if(!m.containsKey(key)) {
                            m.put(key, 0);
                        }
                        m.put(key, (Integer)m.get(key) + amount);
                        break;
                    case OTHERS:
                        m = map.get("아더");
                        key = item.name;
                        amount = item.quantity;
                        if(!m.containsKey(key)) {
                            m.put(key, 0);
                        }
                        m.put(key, (Integer)m.get(key) + amount);
                        break;
                }
            }
        }

        String buf = "";
        buf += "\n드레싱\n";
        buf += mapToSortedString(map.get("드레싱"));
        buf += "\n재료\n";
        buf += mapToSortedString(map.get("재료"));
        buf += "\n아더\n";
        buf += mapToSortedString(map.get("아더"));
        buf += "\n스프\n";
        buf += mapToSortedString(map.get("스프"));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(buf);
        builder.show();
    }

    private String mapToSortedString(Map<String, Integer> m) {
        List<String> l = new LinkedList<>();
        for(Map.Entry<String,Integer> e : m.entrySet()) {
            String s = e.getKey() + " : " + e.getValue();
            l.add(s);
        }
        Collections.sort(l);
        StringBuffer buf = new StringBuffer();
        for(String s : l) {
            buf.append(s);
            buf.append("\n");
        }
        return buf.toString();
    }

    private void confirmCancel(final int order_id) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(""+order_id+" 번 주문을 취소합니까?");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setRawInputType(Configuration.KEYBOARD_12KEY);
        input.setHint("취소하려면 12를 입력");
        alert.setView(input);
        alert.setPositiveButton("canel_order", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //Put actions for OK button here
                if(input.getText().length() > 0) {
                    int onetwo = Integer.parseInt(input.getText().toString());
                    if (onetwo == 12) {
                        CancelOrder(order_id);
                    } else {
                        Toast.makeText(MainActivity.this, "취소를 취소함", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        alert.show();
    }

    private void CancelOrder(int order_id) {
        CancelOrderTask task = new CancelOrderTask(this, order_id) {
            @Override
            protected void onPostExecute(Integer code) {
                super.onPostExecute(code);
                if(code == 200) {
                    refreshUI();
                } else {
                    Toast.makeText(getActivity(), "error" + code, Toast.LENGTH_SHORT).show();
                }
            }
        };
        task.execute();
    }

    private void setReservationTime() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("몇 분 전의 order까지 표시하나요? (분단위)");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setRawInputType(Configuration.KEYBOARD_12KEY);
        input.setText(""+Service.getReservationTimeParam());
        input.setSelection(input.getText().length());
        alert.setView(input);
        alert.setPositiveButton("변경", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //Put actions for OK button here
                if(input.getText().length() > 0) {
                    int time = Integer.parseInt(input.getText().toString());
                    Service.setReservationTimeParam(time);
                }
            }
        });
        alert.show();
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
            mOrderItemAdapter.setMenuList(mMenuList);
            mOrderItemAdapter.notifyDataSetChanged();
            tvSelectedOrderId.setText(""+mSelectedOrder.id + " " + mSelectedOrder.orderType.name());
        } else {
            mOrderItemAdapter.setList(new LinkedList<OrderItem>());
            mOrderItemAdapter.notifyDataSetChanged();
            tvSelectedOrderId.setText("");
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

        btnReady.setEnabled(mSelectedOrder != null);
    }

    private void postReady() {

        UpdateStatusTask task = new UpdateStatusTask(getActivity(), mSelectedOrder) {
            @Override
            protected void onPostExecute(Integer code) {
                super.onPostExecute(code);
                if(code == 200) {
                    if (mOrderList.size() > 1) {
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
                signInJson.put("password", "saladgramadmin1!");
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

    class CancelOrderTask extends ProgressAsyncTask<Void, Void, Integer> {
        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");
        private final int order_id;

        public CancelOrderTask(Context context, int order_id) {
            super(context);
            this.order_id = order_id;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient();

            try {
                JSONObject signInJson = new JSONObject();

                signInJson.put("id", "saladgram");
                signInJson.put("password", "saladgramadmin1!");
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

                request = new Request.Builder()
                        .url("https://www.saladgram.com/api/cancel_order.php?id=saladgram&order_id=" + order_id)
                        .header("jwt", jwt)
                        .build();

                Log.d("yns", request.url().toString());
                Log.d("yns", request.headers().toString());

                response = client.newCall(request).execute();
                Log.d("yns", response.body().string());
                if (response.code() == 200) {
                    try {
                        Service.update();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), e.getMessage() + " at cancel_order", Toast.LENGTH_SHORT).show();
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


    @TargetApi(16)
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

    class MenuListFetchTask extends ProgressAsyncTask<Void,Void,String> {

        public MenuListFetchTask(Context context) {
            super(context);
        }

        @Override
        protected String doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient();


            String url = "https://saladgram.com/api/menu_list.php";
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = null;
            try {
                response = client.newCall(request).execute();
                return response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                HashMap<String, ArrayList<HashMap<String, Object>>> map = new Gson().fromJson(result, new TypeToken<HashMap<String, ArrayList<HashMap<String, Object>>>>() {
                }.getType());
                buildMenuList(map.get("salads"), MenuItem.Type.SALAD);
                buildMenuList(map.get("soups"), MenuItem.Type.SOUP);
                buildMenuList(map.get("others"), MenuItem.Type.OTHER);
                buildMenuList(map.get("beverages"), MenuItem.Type.BEVERAGE);

                setJsonSaladItems(result);
            }
        }

        private void setJsonSaladItems(String result) {
            try {
                JSONObject root = new JSONObject(result);
                JSONArray arr = root.getJSONArray("salads");
                for(int i = 0; i < arr.length(); i++) {
                    JSONObject salad = arr.getJSONObject(i);
                    for(MenuItem item : mMenuList) {
                        if (item.name.equals(salad.getString("name"))) {
                            item.jsonSaladItems = salad.getJSONArray("salad_items");
                            break;
                        }
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void buildMenuList(ArrayList<HashMap<String, Object>> list, MenuItem.Type type) {
            for(HashMap<String, Object> each : list) {
//                if (each.containsKey("hide") && ((Double)each.get("hide")).intValue() == 1) {
//                    continue;
//                }
                MenuItem item = new MenuItem();
                item.data = each;
                item.name = (String) each.get("name");
                item.price = each.containsKey("price") ? ((Double)each.get("price")).intValue() : -1;
                item.available = ((Double)each.get("available")).intValue() == 1;
                item.type = type;
                item.amount = (String) each.get("amount");

                item.checkSize = (type == MenuItem.Type.SOUP);
                item.checkToGo = (type == MenuItem.Type.SALAD);
                if (item.price != -1) {
                    mMenuList.add(item);
                }
            }
        }
    }
}

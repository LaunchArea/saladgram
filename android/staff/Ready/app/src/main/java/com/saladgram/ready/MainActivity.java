package com.saladgram.ready;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.saladgram.model.Order;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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
            }
        }
    };
    private RecyclerView lvOrders;

    private RecyclerViewClickListener mOrderClickListener;
    private OrderAdapter mOrderAdapter;

    private List<Order> mOrderList;
    private HashSet<Integer> mSelectedItems = new HashSet<>();
    private HashSet<String> mDeliverers = new HashSet<>(); //deliverer_id

    private TabLayout addrTabLayout;
    private Button btnShipping;
    private Button btnReset;

    private Order.OrderType mSelectedOrderType = Order.OrderType.PICK_UP;
    private String mSelectedAddr;

    private String[] tabLabelList = {"픽업", "한화", "푸르지오", "아이파크", "배달"};
    private Map<String, Integer> mCounter = new HashMap<>();
    private Button btnDeliverer;

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
        addrTabLayout = (TabLayout) findViewById(R.id.tabs);

        for(int i = 0; i < tabLabelList.length; i++) {
            TabLayout.Tab tab = addrTabLayout.newTab();
            tab.setTag(tabLabelList[i]);
            tab.setText(tabLabelList[i]);
            addrTabLayout.addTab(tab);
        }

        lvOrders = (RecyclerView) findViewById(R.id.order_list);
        btnShipping = (Button) findViewById(R.id.shipping_button);
        btnReset = (Button) findViewById(R.id.reset_button);
        btnDeliverer = (Button) findViewById(R.id.deliverer_add_button);
    }

    private void initControl() {
        btnShipping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mSelectedOrderType == Order.OrderType.PICK_UP) {
                    postPickup();
                } else {
                    postShipping();
                }
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedItems.clear();
                refreshUI();
            }
        });
        btnDeliverer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDeliverer();
            }
        });

        mOrderClickListener = new RecyclerViewClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                int id = mOrderList.get(position).id;
                if (mSelectedOrderType == Order.OrderType.PICK_UP) {
                    if (mSelectedItems.contains(id)) {
                        mSelectedItems.remove(id);
                    } else {
                        mSelectedItems.clear();
                        mSelectedItems.add(id);
                    }
                } else {
                    if (mSelectedItems.contains(id)) {
                        mSelectedItems.remove(id);
                    } else {
                        mSelectedItems.add(id);
                    }
                }

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

        addrTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String tag = (String) tab.getTag();
                if (tag == tabLabelList[0]) {
                    mSelectedOrderType = Order.OrderType.PICK_UP;
                    mSelectedAddr = null;
                    mSelectedItems.clear();
                } else if (tag == tabLabelList[tabLabelList.length-1]) {
                    mSelectedOrderType = Order.OrderType.DELIVERY;
                    mSelectedAddr = null;
                } else {
                    mSelectedOrderType = Order.OrderType.DELIVERY;
                    mSelectedAddr = tag;
                }
                refreshUI();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                String tag = (String) tab.getTag();
                if (tag == tabLabelList[0]) {
                    mSelectedItems.clear();
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        lvOrders.setLayoutManager(new LinearLayoutManager(getActivity()));

        mOrderAdapter = new OrderAdapter(this,mOrderClickListener);
        lvOrders.setAdapter(mOrderAdapter);
    }

    private void addDeliverer() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("배달자 추가");
        final EditText input = new EditText(this);
        alert.setView(input);
        alert.setPositiveButton("저장", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if(input.getText().length() > 0) {
                    mDeliverers.add(input.getText().toString());
                }
            }
        });
        alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
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
        mCounter.clear();
        for (String label : tabLabelList) {
            mCounter.put(label, 0);
        }

        LinkedList<Order> fullList = new LinkedList<>(Service.orderList);
        LinkedList<Order> filteredList = new LinkedList<>();
        for (Order each : fullList) {
            // filter
            if (mSelectedOrderType == Order.OrderType.PICK_UP && each.orderType == Order.OrderType.PICK_UP) {
                filteredList.add(each);
            } else if (mSelectedOrderType == Order.OrderType.DELIVERY && each.orderType == Order.OrderType.DELIVERY) {
                if (mSelectedAddr == null) {
                    filteredList.add(each);
                } else if (each.addr != null && each.addr.contains(mSelectedAddr)) {
                    filteredList.add(each);
                }
            }
            // count

            if (each.orderType == Order.OrderType.PICK_UP) {
                mCounter.put(tabLabelList[0], mCounter.get(tabLabelList[0]) + 1);
            } else if (each.orderType == Order.OrderType.DELIVERY) {
                if (each.addr != null) {
                    for (String label : tabLabelList) {
                        if (each.addr.contains(label)) {
                            mCounter.put(label, mCounter.get(label) + 1);
                            break;
                        }
                    }
                    String key = tabLabelList[tabLabelList.length - 1];
                    mCounter.put(key, mCounter.get(key) + 1);
                }
            }
        }
        mOrderList = new LinkedList<>(filteredList);
        mOrderAdapter.setList(mOrderList);
        mOrderAdapter.setSelectedIdList(mSelectedItems);
        mOrderAdapter.notifyDataSetChanged();

        btnShipping.setEnabled(mSelectedItems.size() > 0);
        if(mSelectedOrderType == Order.OrderType.DELIVERY) {
            btnShipping.setText("" + mSelectedItems.size() + " 개 배달나가기");
        } else {
            btnShipping.setText("픽업 주기");
        }

        for(int i = 0; i < addrTabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = addrTabLayout.getTabAt(i);
            String key = tabLabelList[i];
            tab.setText(key + (mCounter.containsKey(key) ? "\n" + mCounter.get(key): ""));
        }
    }

    private void postPickup() {
        Order order = null;
        for (Integer id : mSelectedItems) {
            for(Order each : mOrderList) {
                if(each.id == id) {
                    order = each;
                    break;
                }
            }
        }
        if (order == null) {
            return;
        }

        if (order.paymentType == Order.PaymentType.AT_PICK_UP) {
            choosePaymentTypeAndConfirmDone(order);
        } else {
            confirmDone(order, null);
        }
    }

    private void choosePaymentTypeAndConfirmDone(final Order order) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setNeutralButton("카드", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                confirmDone(order, Order.PaymentType.CARD);
            }
        });
        builder.setPositiveButton("현금", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                confirmDone(order, Order.PaymentType.CASH);
            }
        });
        builder.setNegativeButton("현금영수증", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                confirmDone(order, Order.PaymentType.CASH_RECEIPT);
            }
        });
        builder.show();
    }

    private void confirmDone(final Order order, final Order.PaymentType paymentType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("done " + order.id + (paymentType == null ? "" : " with " + paymentType.name()));
        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                UpdateStatusTask task = new UpdateStatusTask(getActivity(), order, paymentType) {
                    @Override
                    protected void onPostExecute(Integer code) {
                        super.onPostExecute(code);
                        Toast.makeText(getActivity(), "" + (code == 200 ? "Success" : code), Toast.LENGTH_SHORT).show();
                        if (code == 200) {
                            mSelectedItems.clear();
                            refreshUI();
                        }
                    }
                };
                task.execute();
            }
        });
        builder.show();
    }

    private void postShipping() {
        if(mDeliverers.size() == 0) {
            addDeliverer();
            return;
        }
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());
        builderSingle.setTitle("배달자 선택");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.select_dialog_singlechoice);
        for (String each : mDeliverers) {
            arrayAdapter.add(each);
        }

        builderSingle.setAdapter(
                arrayAdapter,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String strName = arrayAdapter.getItem(which);
                        ShippingTask task = new ShippingTask(getActivity(), strName, mSelectedItems) {
                            @Override
                            protected void onPostExecute(Integer integer) {
                                super.onPostExecute(integer);
                                mSelectedItems.clear();
                                Toast.makeText(getActivity(),"SHIP " + mSelectedItems.size() + " by " + strName,Toast.LENGTH_SHORT).show();
                                refreshUI();
                            }
                        };
                        task.execute();
                    }
                });
        builderSingle.show();
    }
    class ShippingTask extends ProgressAsyncTask<Void, Void, Integer> {
        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");
        private final String mDeliverId;
        private final HashSet<Integer> mIDs;

        public ShippingTask(Context context, String deliverId, HashSet<Integer> ids) {
            super(context);
            this.mDeliverId = deliverId;
            this.mIDs = new HashSet<Integer>(ids);
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
                for(Integer id : mIDs) {
                    m.clear();
                    m.put("order_id", id);

                    if (mDeliverId != null) {
                        m.put("status", Order.Status.SHIPPING.ordinal() + 1);
                        m.put("deliverer_id", mDeliverId);
                    } else {
                        m.put("status", Order.Status.DONE.ordinal() + 1);
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

                    if (response.code() != 200) {
                        Toast.makeText(getActivity(), "!!!! Failed to update [Order:" + id +"] ("+response.code()+")!!!!!\n", Toast.LENGTH_LONG).show();
                    }
                }

                try {
                    Service.update();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), e.getMessage() + " at order refresh", Toast.LENGTH_SHORT).show();
                }

                return 200;

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return -1;
        }
    }

    class UpdateStatusTask extends ProgressAsyncTask<Void, Void, Integer> {
        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");
        private final Order mOrder;
        private final Order.PaymentType mPaymentType;

        public UpdateStatusTask(Context context, Order order, Order.PaymentType paymentType) {
            super(context);
            this.mOrder = order;
            this.mPaymentType = paymentType;
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
                m.put("paid", mOrder.json.getInt("actual_price"));
                m.put("status", Order.Status.DONE.ordinal() + 1);
                if (mPaymentType != null) {
                    m.put("payment_type", mPaymentType.ordinal() + 1);
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
                try {
                    Service.update();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), e.getMessage() + " at order refresh", Toast.LENGTH_SHORT).show();
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

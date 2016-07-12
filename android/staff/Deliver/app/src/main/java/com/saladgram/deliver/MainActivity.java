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
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.saladgram.model.Order;

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

import static com.saladgram.model.Order.PaymentType.CASH_RECEIPT;

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
                    Service.setDeliverId(mDelivererId);
                    refreshUI();
                } else {
                    finish();
                }
            }
        });
        alert.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
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
        if(order.paymentType == Order.PaymentType.AT_DELIVERY) {
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
                confirmDone(order, Order.PaymentType.DELIVER_CARD);
            }
        });
        builder.setPositiveButton("현금", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                confirmDone(order, Order.PaymentType.DELIVER_CASH);
            }
        });
        builder.setNegativeButton("현금영수증", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                confirmDone(order, Order.PaymentType.DELIVER_CASH_RECEIPT);
            }
        });
        builder.show();
    }

    private void confirmDone(final Order order, final Order.PaymentType paymentType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String message = "done " + order.id + (paymentType == null ? "" : " with " + paymentType.name());
        message += "\n\n계산금액 : " + order.actual_price;
        builder.setMessage(message);
        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                UpdateStatusTask task = new UpdateStatusTask(getActivity(), order, paymentType) {
                    @Override
                    protected void onPostExecute(Integer code) {
                        super.onPostExecute(code);
                        Toast.makeText(getActivity(), "" + (code == 200 ? "Success" : code), Toast.LENGTH_SHORT).show();
                        if (code == 200) {
                            refreshUI();
                        }
                    }
                };
                task.execute();
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

package com.saladgram.ready;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;

import com.saladgram.model.Order;
import com.saladgram.model.OrderItem;
import com.saladgram.model.SaladItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by yns on 5/30/16.
 */
public class Service {
    static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    public static final String ACTION_FETCH_FAILED = "com.saladgram.assemble.action.fetch.failed";
    public static final String ACTION_FETCH_DONE = "com.saladgram.assemble.action.fetch.done";

    static ScheduledFuture<?> delayFuture;
    static ScheduledThreadPoolExecutor sch = (ScheduledThreadPoolExecutor)
            Executors.newScheduledThreadPool(1);
    private static long jwtTime = 0;
    private static String jwt;
    private static Context mContext;
    public static List<Order> orderList = new LinkedList<>();

    public synchronized static void start(Context context) {
        mContext = context;
        if (delayFuture == null) {

            Runnable delayTask = new Runnable() {
                @Override
                public void run() {
                    try {
                        if (getJWT() != null) {
                            update();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        reportError("IOException " + e.getMessage());
                    } catch (JSONException e) {
                        e.printStackTrace();
                        reportError("JSONException " + e.getMessage());
                    }
                }
            };

            delayFuture = sch.scheduleWithFixedDelay(delayTask, 3, 3, TimeUnit.SECONDS);
        }
    }

    public synchronized static void update() throws IOException, JSONException {
        fetch("https://www.saladgram.com/api/orders.php?id=saladgram&status=2");
    }

    public synchronized static void stop() {
        if (delayFuture != null) {
            delayFuture.cancel(false);
            delayFuture = null;
        }
    }

    private static void fetch(String url) throws IOException, JSONException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .header("jwt", jwt)
                .build();

        Response response = client.newCall(request).execute();
        if (response.code() == 200) {
            orderList.clear();
            String result = response.body().string();
            JSONArray orders = new JSONArray(result);
            for (int i = 0; i < orders.length(); i++) {
                Order order = new Order(orders.getJSONObject(i));
                JSONArray orderItems = orders.getJSONObject(i).getJSONArray("order_items");
                for (int j = 0; j < orderItems.length(); j++) {
                    OrderItem orderItem = new OrderItem(orderItems.getJSONObject(j));
                    order.orderItems.add(orderItem);

                    if (orderItems.getJSONObject(j).has("salad_items")) {
                        JSONArray saladItems = orderItems.getJSONObject(j).getJSONArray("salad_items");
                        for (int k = 0; k < saladItems.length(); k++) {
                            SaladItem saladItem = new SaladItem(saladItems.getJSONObject(k));
                            orderItem.saladItems.add(saladItem);
                        }
                        SaladItem.sort(orderItem.saladItems);
                    }
                }
                orderList.add(order);
            }
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(ACTION_FETCH_DONE));
        } else {
            reportError("fetch response " + response.code());
        }
    }

    private static void reportError(String reason) {
        Intent intent = new Intent(ACTION_FETCH_FAILED);
        intent.putExtra("reason", reason);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private static String getJWT() throws IOException {

        long now = SystemClock.elapsedRealtime();
        if (jwtTime + 60 * 1000 < now) {
            jwt = signIn();
            if (jwt != null) {
                jwtTime = now;
            }
        }
        return jwt;
    }

    private static String signIn() throws IOException {
        OkHttpClient client = new OkHttpClient();
        JSONObject signInJson = new JSONObject();

        try {
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
            if (response.code() == 200) {
                return new JSONObject(response.body().string()).getString("jwt");
            } else {
                reportError("signin response " + response.code());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}

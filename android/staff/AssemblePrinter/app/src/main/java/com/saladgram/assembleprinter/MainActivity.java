package com.saladgram.assembleprinter;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.saladgram.model.Order;
import com.saladgram.model.OrderItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction() == Service.ACTION_FETCH_FAILED) {
                Toast.makeText(context, intent.getStringExtra("reason"), Toast.LENGTH_SHORT).show();
            } else if (intent.getAction() == Service.ACTION_FETCH_DONE) {
                int now = Service.orderList.size();
                checkReadyItemAndPrint();
            }
        }
    };

    HashMap<Integer, Order.Status> mStatus = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Service.ACTION_FETCH_FAILED);
        filter.addAction(Service.ACTION_FETCH_DONE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);
    }

    boolean first = true;
    private void checkReadyItemAndPrint() {

        LinkedList<Order> clone = new LinkedList<Order>();
        clone.addAll(Service.orderList);
        if (first) {
            first = false;
            for(Order each : clone) {
                mStatus.put(each.id, each.status);
            }
            return;
        }
        for(Order each : clone) {
            if (!mStatus.containsKey(each.id)) {
                doPrint(each);
                mStatus.put(each.id, each.status);
            }
        }
    }

    private void doPrint(Order order) {
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

        PrinterService.doPrint(buffer.toString());
    }

    SimpleDateFormat fmt = new SimpleDateFormat("MM/dd HH:mm:ss");
    private String formatTime(Date reservation_time) {
        return fmt.format(reservation_time);
    }

}
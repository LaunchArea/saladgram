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
    private synchronized void checkReadyItemAndPrint() {

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

        buffer.append("주문번호 : " + order.id + " (" + order.orderType.name().toLowerCase() + ")\n");
        buffer.append("고객정보 : " + (order.user_id != null ? order.user_id : ("none" + " ")) + " / " + (order.phone != null ? order.phone : "none"));
        buffer.append("\n");
        buffer.append("주    소 : " + order.addr + "\n");
        buffer.append("결제방법 : ");
        switch (order.paymentType) {
            case INIPAY:
                buffer.append("결제완료 (인터넷)");
            case AT_DELIVERY:
                buffer.append("수령시 결제");
                break;
            case REWARD_ONLY:
                buffer.append("결제완료 (리워드)");
                break;
            case AT_PICK_UP:
                buffer.append("픽업시 결제");
                break;
            case CARD:
            case CASH:
            case CASH_RECEIPT:
            case DELIVER_CARD:
            case DELIVER_CASH:
            case DELIVER_CASH_RECEIPT:
                buffer.append(order.paymentType.name());
        }
        buffer.append("\n");

        if (order.order_time.getTime() > 0) {
            buffer.append("주문시간 : " + formatTime(order.order_time));
        }
        if (order.reservation_time.getTime() != order.order_time.getTime()) {
            buffer.append(" (" + formatTime(order.reservation_time)+ " 예약)");
        }
        buffer.append("\n\n");
        buffer.append("주문내역\n");
        for (OrderItem item : order.orderItems) {
            buffer.append("    ");
            buffer.append(item.name);
            buffer.append(item.amount != null ? (" " + item.amount) : "");
            buffer.append(" x " + item.quantity + "\n");
        }
        buffer.append("\n\n\n\n\n");

        PrinterService.doPrint(buffer.toString());
    }

    SimpleDateFormat fmt = new SimpleDateFormat("MM/dd HH:mm");
    private String formatTime(Date reservation_time) {
        return fmt.format(reservation_time);
    }

}
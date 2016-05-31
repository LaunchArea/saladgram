package com.saladgram.deliver;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by yns on 5/31/16.
 */
public class Order {

    public JSONObject json;
    private String orderItemSummary = null;

    public Order(JSONObject each) throws JSONException {
        this.json = each;
        id = each.getInt("order_id");
        order_time = new Date(each.getLong("order_time") * 1000);
        reservation_time = new Date(each.getLong("reservation_time") * 1000);
        addr = each.optString("addr", null);

        switch (each.getInt("order_type")) {
            case 1: orderType = OrderType.PICK_UP; break;
            case 2: orderType = OrderType.DELIVERY; break;
            case 3: orderType = OrderType.SUBSCRIBE; break;
            case 4: orderType = OrderType.DINE_IN; break;
            case 5: orderType = OrderType.TAKE_OUT; break;
        }

        switch (each.getInt("status")) {
            case 1: status = Status.TODO; break;
            case 2: status = Status.READY; break;
            case 3: status = Status.SHIPPING; break;
            case 4: status = Status.DONE; break;
            case 5: status = Status.CANCELED; break;
        }

        paymentType = PaymentType.values()[each.getInt("payment_type")-1];
    }

    public String getOrderItemSummary() {
        if (orderItemSummary == null) {
            int[] arr = new int[4];
            for (OrderItem item : orderItems) {
                arr[item.type.ordinal()]++;
            }
            StringBuffer buf = new StringBuffer();
            for (int i =0; i < arr.length; i++) {
                int cnt = arr[i];
                if (cnt > 0) {
                    String text = "";
                    switch(i) {
                        case 0: text = "샐"; break;
                        case 1: text = "스"; break;
                        case 2: text = "아"; break;
                        case 3: text = "음"; break;
                    }
                    buf.append(text + cnt + " ");
                }
            }
            orderItemSummary = buf.toString();
        }
        return orderItemSummary;
    }

    enum OrderType {PICK_UP, DELIVERY, SUBSCRIBE, DINE_IN, TAKE_OUT}
    enum Status {TODO, READY, SHIPPING, DONE, CANCELED}
    enum PaymentType {CARD, CASH, CASH_RECEIPT, DELIVER_CARD, DELIVER_CASH, DELIVER_CASH_RECEIPT, PICK_UP, INIPAY, REWARD_ONLY}

    int id;
    OrderType orderType;
    PaymentType paymentType;
    Date order_time;
    Date reservation_time;
    Status status;
    String addr;
    List<OrderItem> orderItems = new LinkedList<>();

}

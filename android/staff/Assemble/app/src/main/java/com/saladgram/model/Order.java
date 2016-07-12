package com.saladgram.model;

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
        actual_price = each.getInt("actual_price");
        user_id = each.optString("id", null);

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
            int[] arr = new int[6];
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
                        case 4: text = "샐샐"; break;
                        case 5: text = "샐스"; break;
                    }
                    buf.append(text + cnt + " ");
                }
            }
            orderItemSummary = buf.toString();
        }
        return orderItemSummary;
    }

    public enum OrderType {PICK_UP, DELIVERY, SUBSCRIBE, DINE_IN, TAKE_OUT}
    public enum Status {TODO, READY, SHIPPING, DONE, CANCELED}
    public enum PaymentType {
        CARD,
        CASH,
        CASH_RECEIPT,
        DELIVER_CARD,
        DELIVER_CASH,
        DELIVER_CASH_RECEIPT,
        INIPAY,
        AT_PICK_UP,
        AT_DELIVERY,
        REWARD_ONLY
    }

    public int id;
    public OrderType orderType;
    public PaymentType paymentType;
    public Date order_time;
    public Date reservation_time;
    public Status status;
    public String addr;
    public String user_id;
    public int actual_price;
    public List<OrderItem> orderItems = new LinkedList<>();

}

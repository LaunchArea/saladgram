package com.saladgram.assemble;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by yns on 5/31/16.
 */
public class Order {

    public Order(JSONObject each) throws JSONException {
        id = each.getInt("order_id");
        order_time = new Date(each.getLong("order_time") * 1000);

        switch (each.getInt("order_type")) {
            case 1: type = Type.PICK_UP; break;
            case 2: type = Type.DELIVERY; break;
            case 3: type = Type.SUBSCRIBE; break;
            case 4: type = Type.DINE_IN; break;
            case 5: type = Type.TAKE_OUT; break;
        }

        switch (each.getInt("status")) {
            case 1: status = Status.TODO; break;
            case 2: status = Status.READY; break;
            case 3: status = Status.SHIPPING; break;
            case 4: status = Status.DONE; break;
            case 5: status = Status.CANCELED; break;
        }
    }

    enum Type {PICK_UP, DELIVERY, SUBSCRIBE, DINE_IN, TAKE_OUT}
    enum Status {TODO, READY, SHIPPING, DONE, CANCELED}

    int id;
    Type type;
    Date order_time;
    Status status;
    List<OrderItem> orderItems = new LinkedList<>();
}

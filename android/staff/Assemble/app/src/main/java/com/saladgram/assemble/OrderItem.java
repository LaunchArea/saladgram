package com.saladgram.assemble;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by yns on 5/31/16.
 */
public class OrderItem {
    public final JSONObject json;

    public OrderItem(JSONObject item) throws JSONException {
        json = item;
        id = item.getInt("item_id");
        name = item.getString("name");
        quantity = item.getInt("quantity");
        if (item.has("amount")) {
            amount = item.getString("amount");
        }
        switch(item.getInt("order_item_type")) {
            case 1: type = Type.SALAD; break;
            case 2: type = Type.SOUP; break;
            case 3: type = Type.OTHERS; break;
            case 4: type = Type.BEVERAGES; break;
        }
    }

    enum Type {SALAD, SOUP, OTHERS, BEVERAGES}
    int id;
    String amount = null;
    String name;
    Type type;
    int quantity;

    List<SaladItem> saladItems = new LinkedList<>();
}

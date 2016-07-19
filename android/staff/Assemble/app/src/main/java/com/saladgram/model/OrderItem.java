package com.saladgram.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
            case 5: type = Type.SELF_SALAD; break;
            case 6: type = Type.SELF_SOUP; break;
        }
        packageType = PackageType.values()[item.getInt("package_type")-1];
    }

    public enum Type {SALAD, SOUP, OTHERS, BEVERAGES, SELF_SALAD, SELF_SOUP}
    public enum PackageType {TAKE_OUT, DINE_IN}
    public int id;
    public String amount;
    public String name;
    public Type type;
    public int quantity;
    public PackageType packageType;

    public List<SaladItem> saladItems = new LinkedList<>();

    public JSONArray jsonSaladItems = null;
}

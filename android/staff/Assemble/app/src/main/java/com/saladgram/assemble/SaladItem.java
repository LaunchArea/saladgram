package com.saladgram.assemble;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by yns on 5/31/16.
 */
public class SaladItem {
    public SaladItem(JSONObject item) throws JSONException {
        id = item.getInt("item_id");
        amount = item.getString("amount");
        name = item.getString("name");
        switch (item.getInt("salad_item_type")) {
            case 1: type = Type.BASE; break;
            case 2: type = Type.VEGETABLES; break;
            case 3: type = Type.FRUITS; break;
            case 4: type = Type.PROTEINS; break;
            case 5: type = Type.OTHERS; break;
            case 6: type = Type.DRESSINGS; break;
        }
    }

    enum Type {BASE, VEGETABLES, FRUITS, PROTEINS, OTHERS, DRESSINGS}
    int id;
    String name;
    Type type;
    String amount;
}

package com.saladgram.posmirror;

import java.util.HashMap;

public class MenuItem {


    public int getAmount(int amount_type) {
        String key = "amount" + amount_type;
        return ((Double)data.get(key)).intValue();
    }

    enum Type {SALAD, SOUP, OTHER, BEVERAGE}

    String name;
    int price;
    Type type;
    public boolean available;
    public String amount;
    HashMap<String, Object> data;

    boolean checkWeight = false;
    boolean checkSize = false;
}

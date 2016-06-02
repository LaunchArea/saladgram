package com.saladgram.model;

import java.util.HashMap;

public class MenuItem {


    public int getAmount(int amount_type) {
        String key = "amount" + amount_type;
        return ((Double)data.get(key)).intValue();
    }

    public enum Type {SALAD, SOUP, OTHER, BEVERAGE}

    public String name;
    public int price;
    public Type type;
    public boolean available;
    public String amount;
    public HashMap<String, Object> data;

    public boolean checkWeight = false;
    public boolean checkSize = false;
}

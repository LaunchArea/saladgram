package com.saladgram.pos;

import java.util.HashMap;

public class MenuItem {


    enum Type {SALAD, SOUP, OTHER, BEVERAGE}

    String name;
    int price;
    Type type;
    public boolean available;
    public String amount;
    HashMap<String, Object> data;

    boolean checkToGo = false;
    boolean checkWeight = false;
}

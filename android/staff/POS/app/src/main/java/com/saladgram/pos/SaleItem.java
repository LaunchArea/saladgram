package com.saladgram.pos;

/**
 * Created by yns on 5/27/16.
 */
public class SaleItem {
    static final int PRICE_PER_AMOUNT = 2500;
    MenuItem menuItem;
    boolean takeout = false;
    int amount = 0;
    public int quantity = 1;

    public int getPrice() {
        if(amount > 0) {
            return (int) (((double)amount/100) * PRICE_PER_AMOUNT);
        } else if (quantity > 1) {
            return menuItem.price * quantity;
        } else {
            return menuItem.price;
        }
    }
}

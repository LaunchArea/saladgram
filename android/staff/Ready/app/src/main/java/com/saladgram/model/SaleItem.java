package com.saladgram.model;

/**
 * Created by yns on 5/27/16.
 */
public class SaleItem {
    static final int PRICE_PER_AMOUNT = 2500;
    public MenuItem menuItem;
    public boolean takeout = false;
    public int amount = 0;
    public int quantity = 1;
    public int amount_type = 0;

    public int getPricePerEach() {
        if(menuItem.type == MenuItem.Type.SOUP) {
            return ((amount * menuItem.price) / 100);
        } else {
            if (amount > 0) {
                return (int) (((double) amount / 100) * PRICE_PER_AMOUNT);
            } else {
                return menuItem.price;
            }
        }
    }

    public int getTotalPrice() {
        return getPricePerEach() * quantity;
    }

    public boolean isSameKind(SaleItem that) {
        if(this.menuItem.hashCode() == that.menuItem.hashCode()) {
            if(that.menuItem.type == MenuItem.Type.SOUP) {
                return this.amount_type == that.amount_type;
            }
            return true;
        }
        return false;
    }

    public int getCaloriePerEach() {
        int calorie = ((Double) menuItem.data.get("calorie")).intValue();
        if (menuItem.type == MenuItem.Type.SOUP) {
            return (calorie * amount) / 100;
        } else {
            return calorie;
        }
    }
}

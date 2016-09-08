package com.saladgram.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
        amount_type = item.getInt("amount_type");
    }

    public enum Type {BASE, VEGETABLES, FRUITS, PROTEINS, OTHERS, DRESSINGS}
    public int id;
    public String name;
    public Type type;
    public String amount;
    public int amount_type;

    private final static Comparator comparator = new Comparator<SaladItem>() {
        @Override
        public int compare(SaladItem l,SaladItem r) {
            if (l.type == r.type) return 0;
            switch (l.type) {
                case BASE:
                    switch(r.type) {
                        case BASE:
                            return 0;
                        case FRUITS:
                        case PROTEINS:
                        case VEGETABLES:
                        case OTHERS:
                        case DRESSINGS:
                            return -1;
                    }
                case FRUITS:
                    switch(r.type) {
                        case BASE:
                            return 1;
                        case FRUITS:
                            return 0;
                        case PROTEINS:
                        case VEGETABLES:
                        case OTHERS:
                        case DRESSINGS:
                            return -1;
                    }
                case PROTEINS:
                    switch(r.type) {
                        case BASE:
                        case FRUITS:
                            return 1;
                        case PROTEINS:
                            return 0;
                        case VEGETABLES:
                        case OTHERS:
                        case DRESSINGS:
                            return -1;
                    }
                case VEGETABLES:
                    switch(r.type) {
                        case BASE:
                        case FRUITS:
                        case PROTEINS:
                            return 1;
                        case VEGETABLES:
                            return 0;
                        case OTHERS:
                        case DRESSINGS:
                            return -1;
                    }
                case OTHERS:
                    switch(r.type) {
                        case BASE:
                        case FRUITS:
                        case PROTEINS:
                        case VEGETABLES:
                            return 1;
                        case OTHERS:
                            return 0;
                        case DRESSINGS:
                            return -1;
                    }
                case DRESSINGS:
                    switch(r.type) {
                        case BASE:
                        case FRUITS:
                        case PROTEINS:
                        case VEGETABLES:
                        case OTHERS:
                            return 1;
                        case DRESSINGS:
                            return 0;
                    }
                    break;
            }
            return 0;
        }
    };

    static public void sort(List<SaladItem> items) {
        Collections.sort(items, comparator);
    }
}

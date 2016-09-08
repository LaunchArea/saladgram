package com.saladgram.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by yns on 7/19/16.
 */
public class Utils {
    public static boolean jsonArrayEquals(JSONArray left, JSONArray right) {
        if(left.length() != right.length()) {
            return false;
        }
        Map<String, JSONObject> mapL = new HashMap<>();
        Map<String, JSONObject> mapR = new HashMap<>();
        for(int i = 0; i < left.length(); i++) {
            try {
                JSONObject jo = left.getJSONObject(i);
                mapL.put(jo.getString("item_id") + "X" + jo.getString("salad_item_type"), jo);
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }
        for(int i = 0; i < right.length(); i++) {
            try {
                JSONObject jo = right.getJSONObject(i);
                mapR.put(jo.getString("item_id") + "X" + jo.getString("salad_item_type"), jo);
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }
        for(String key : mapL.keySet()) {
            JSONObject l = mapL.get(key);
            JSONObject r = mapR.get(key);
            if (r == null) {
                return false;
            }
            if (!jsonObjectEquals(l,r)) {
                return false;
            }
        }
        return true;
    }
    public static boolean jsonObjectEquals(JSONObject left, JSONObject right)
    {
        HashSet<String> lKeyStrings = new HashSet<>();
        HashSet<String> rKeyStrings = new HashSet<>();
        Iterator<String> iterLeftKeys = left.keys();
        Iterator<String> iterRightKeys = right.keys();

        if (iterLeftKeys == null || iterRightKeys == null) {
            return false;
        }
        while(iterLeftKeys.hasNext()) {
            lKeyStrings.add(iterLeftKeys.next());
        }
        while(iterRightKeys.hasNext()) {
            rKeyStrings.add(iterRightKeys.next());
        }

        if(lKeyStrings.size() != rKeyStrings.size()) {
            return false;
        }

        for(String key : lKeyStrings) {
            if (!rKeyStrings.contains(key)) {
                return false;
            }
            try {
                if(!left.get(key).equals(right.get(key))) {
                    return false;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
}

package com.saladgram.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by yns on 7/19/16.
 */
public class Utils {
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

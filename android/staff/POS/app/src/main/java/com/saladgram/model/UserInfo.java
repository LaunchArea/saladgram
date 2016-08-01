package com.saladgram.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yns on 8/1/16.
 */
public class UserInfo {
    public JSONObject json = new JSONObject();
    public String id;
    public String name;
    public String addr;
    public int reward;
    public String phone;

    public UserInfo() throws JSONException
    {
        /*
        json = info;
        id = info.getString("id");
        name = info.getString("name");
        addr = info.getString("addr");
        reward = info.getInt("reward");
        phone = info.getString("phone");
        */
    }

    public UserInfo(JSONObject info) throws JSONException {
     json = info;
     id = info.getString("id");
     name = info.getString("name");
     addr = info.getString("addr");
     reward = info.getInt("reward");
     phone = info.getString("phone");
    }
}

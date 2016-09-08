package com.saladgram.assembleprinter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.saladgram.model.Order;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by yns on 7/13/16.
 */
public class OrderAdapter extends BaseAdapter {

    private final Context mContext;
    private List<String> mOrders = new LinkedList<>();

    public OrderAdapter(Context context) {
        mContext = context;
    }

    public void setList(List<String> orders) {
        mOrders.clear();
        mOrders.addAll(orders);
    }

    @Override
    public int getCount() {
        return mOrders.size();
    }

    @Override
    public Object getItem(int position) {
        return mOrders.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = new TextView(mContext);
        }
        TextView tv = (TextView) convertView;
        tv.setPadding(0,40,0,40);
        String text = mOrders.get(position);
        tv.setText(text);
        if (text.contains("PRINT")) {
            tv.setTextColor(Color.BLUE);
        } else {
            tv.setTextColor(Color.DKGRAY);
        }

        return convertView;
    }
}

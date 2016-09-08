package com.saladgram.assemble;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.saladgram.model.MenuItem;
import com.saladgram.model.OrderItem;
import com.saladgram.model.SaladItem;
import com.saladgram.model.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by yns on 5/31/16.
 */

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.SimpleViewHolder> {

    private final Context mContext;
    private final RecyclerViewClickListener mListener;
    private List<OrderItem> mList = new LinkedList<>();
    private Map<String, MenuItem> mapMenuList = new HashMap<>();

    public void setList(List<OrderItem> list) {
        this.mList = list;
        notifyDataSetChanged();
    }

    public void setMenuList(List<MenuItem> menuList) {
        mapMenuList.clear();
        for(MenuItem item : menuList) {
            mapMenuList.put(item.name, item);
        }
    }

    public static class SimpleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private final RecyclerViewClickListener mListener;
        private final TextView name;
        private final TextView type;
        private final TextView detail0;
        private final TextView detail1;
        private final TextView quantity;
        private final TextView detail2;
        private final TextView detail3;
        private final TextView package_type;


        public SimpleViewHolder(View view, RecyclerViewClickListener listener) {
            super(view);
            name = (TextView) view.findViewById(R.id.name);
            type = (TextView) view.findViewById(R.id.type);
            package_type = (TextView) view.findViewById(R.id.package_type);

            detail0 = (TextView) view.findViewById(R.id.detail0);
            detail1 = (TextView) view.findViewById(R.id.detail1);
            detail2 = (TextView) view.findViewById(R.id.detail2);
            detail3 = (TextView) view.findViewById(R.id.detail3);

            quantity = (TextView) view.findViewById(R.id.quantity);
            mListener = listener;
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(mListener != null) {
                mListener.onItemClick(v, getAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if(mListener != null) {
                mListener.onItemLongClick(v, getAdapterPosition());
            }
            return false;
        }
    }

    public OrderItemAdapter(Context context, RecyclerViewClickListener listner) {
        mContext = context;
        mListener = listner;
    }

    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.orderitem_listitem, parent, false);
        return new SimpleViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(SimpleViewHolder holder, final int position) {
        OrderItem item = mList.get(position);
        holder.name.setBackgroundColor(Color.WHITE);

        List<String> dressingNames = new LinkedList<>();

        holder.name.setText(item.name);
        holder.type.setText(item.type.name().toLowerCase());
        holder.quantity.setText("x " + item.quantity);
        holder.package_type.setVisibility(item.packageType == OrderItem.PackageType.DINE_IN ? View.VISIBLE : View.GONE);

        holder.detail0.setText("");
        holder.detail2.setText("");
        holder.detail3.setText("");
            switch(item.type) {
                case SALAD:
                    boolean diff = false;
                    MenuItem menuItem = mapMenuList.get(item.name);
                    Map<String, Integer> mapStandardAmount = new HashMap<>();
                    if (menuItem != null) {
                        if (menuItem.jsonSaladItems != null && item.jsonSaladItems != null &&
                                !Utils.jsonArrayEquals(menuItem.jsonSaladItems, item.jsonSaladItems)) {
                            holder.name.setBackgroundColor(Color.RED);
                            diff = true;
                            for (int i = 0; i < menuItem.jsonSaladItems.length(); i++) {
                                try {
                                    JSONObject jo = menuItem.jsonSaladItems.getJSONObject(i);
                                    mapStandardAmount.put(""+jo.getInt("item_id"), jo.getInt("amount"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    String[] details = new String[4];
                    for(int i = 0; i < details.length; i++) {
                        details[i] = "";
                    }
                    for(SaladItem each : item.saladItems) {
                        if (diff) {
                            Integer standardAmount = mapStandardAmount.get("" + each.id);
                            if (standardAmount == null) {
                                details[0] += "N\n";
                            } else if((""+standardAmount).equalsIgnoreCase(each.amount)) {
                                details[0] += (" \n");
                            } else {
                                details[0] += ("!!!\n");
                            }
                        } else {
                            details[0] += (" \n");
                        }
                        if (each.type == SaladItem.Type.DRESSINGS) {
                            details[1] += ("<font color=#ff4265>"+each.name+"</font><br>");
                        } else {
                            details[1] += (each.name + "<br>");
                        }
                        details[2] += ("" + each.amount + "\n");
                        details[3] += ("x" + amountTypeToMultiplier(each.amount_type) + "\n");

                    }
                    for(int i = 0; i < details.length; i++) {
                        details[i] = details[i].substring(0, details[i].length()-1);
                    }
                    holder.detail0.setText(details[0]);
                    holder.detail1.setText(Html.fromHtml(details[1]));
                    holder.detail2.setText(details[2]);
                    holder.detail3.setText(details[3]);

                    for(String dressing : dressingNames) {
                        setColor(holder.detail1, details[1], dressing, Color.YELLOW);
                    }

                    break;
                case SOUP:
                case OTHERS:
                case SELF_SALAD:
                case SELF_SOUP:
                    holder.detail1.setText(item.amount != null ? item.amount : "");
                    break;
                case BEVERAGES:
                    holder.detail1.setText("");
                    break;
            }
        }

    private String amountTypeToMultiplier(int amount_type) {
        switch (amount_type) {
            case 1:
                return ".5";
            case 2:
                return "1";
            case 3:
                return "1.5";
            case 4:
                return "2";
            default:
                return "?";
        }
    }
    private void setColor(TextView view, String fulltext, String subtext, int color) {
        view.setText(fulltext, TextView.BufferType.SPANNABLE);
        Spannable str = (Spannable) view.getText();
        int i = fulltext.indexOf(subtext);
        str.setSpan(new BackgroundColorSpan(color), i, i + subtext.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }


    @Override
    public int getItemCount() {
        return mList.size();
    }
}

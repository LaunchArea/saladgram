package com.saladgram.ready;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * Created by yns on 5/31/16.
 */

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.SimpleViewHolder> {

    private final Context mContext;
    private final RecyclerViewClickListener mListener;
    private List<Order> mList = new LinkedList<>();
    private Collection<Integer> mSelectedIds = new HashSet<>();

    public void setList(List<Order> list) {
        this.mList = list;
        notifyDataSetChanged();
    }

    public void setSelectedIdList(Collection<Integer> selectedIds) {
        mSelectedIds.clear();
        mSelectedIds.addAll(selectedIds);
    }


    public static class SimpleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private final RecyclerViewClickListener mListener;
        private final TextView id;
        private final View bg;
        private final TextView order_type;
        private final TextView order_time;
        private final TextView reservation_time;
        private final TextView payment_type;
        private final TextView detail1;
        private final TextView detail2;
        private final TextView detail3;
        private final TextView address;


        public SimpleViewHolder(View view, RecyclerViewClickListener listener) {
            super(view);
            bg = view.findViewById(R.id.bg);
            id = (TextView) view.findViewById(R.id.id);
            order_type = (TextView) view.findViewById(R.id.order_type);
            order_time = (TextView) view.findViewById(R.id.order_time);
            reservation_time = (TextView) view.findViewById(R.id.reservation_time);
            payment_type = (TextView) view.findViewById(R.id.payment_type);
            detail1 = (TextView) view.findViewById(R.id.detail1);
            detail2 = (TextView) view.findViewById(R.id.detail2);
            detail3 = (TextView) view.findViewById(R.id.detail3);
            address = (TextView) view.findViewById(R.id.address);

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

    public OrderAdapter(Context context, RecyclerViewClickListener listner) {
        mContext = context;
        mListener = listner;
    }

    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.order_listitem, parent, false);
        return new SimpleViewHolder(view, mListener);
    }

    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.KOREA);
    @Override
    public void onBindViewHolder(SimpleViewHolder holder, final int position) {
        Order order = mList.get(position);
        holder.id.setText("" + order.id);
        holder.order_type.setText(order.orderType.name().toLowerCase());
        holder.order_time.setText(relativeTime(order.order_time) + " 주문");

        if (order.reservation_time.getTime() > 0) {
            holder.reservation_time.setText(relativeTime(order.reservation_time) + " 까지");
            holder.reservation_time.setVisibility(View.VISIBLE);
            holder.order_time.setVisibility(View.GONE);
        } else {
            holder.reservation_time.setVisibility(View.GONE);
            holder.order_time.setVisibility(View.VISIBLE);
        }
        holder.payment_type.setText(order.paymentType.name());

        String[] details = new String[3];
        for(int i = 0; i < details.length; i++) {
            details[i] = "";
        }
        for (OrderItem item : order.orderItems) {

            details[0] += (item.name + "\n");
            details[1] += (item.amount != null ? item.amount + "\n" : "\n");
            details[2] += ("x " + item.quantity + "\n");

        }
        for(int i = 0; i < details.length; i++) {
            details[i] = details[i].substring(0, details[i].length()-1);
        }
        holder.detail1.setText(details[0]);
        holder.detail2.setText(details[1]);
        holder.detail3.setText(details[2]);

        if (order.addr != null) {
            holder.address.setText(order.addr);
        } else {
            holder.address.setText("");
        }
        if(mSelectedIds.contains(order.id)) {
            holder.bg.setBackgroundResource(android.R.color.darker_gray);
        } else {
            holder.bg.setBackgroundResource(android.R.color.white);
        }
    }

    private CharSequence relativeTime(Date reservation_time) {
        long now = System.currentTimeMillis();
        return DateUtils.getRelativeTimeSpanString(reservation_time.getTime(), now, 0L, DateUtils.FORMAT_ABBREV_ALL);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

}

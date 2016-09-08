package com.saladgram.assemble;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.saladgram.model.MenuItem;
import com.saladgram.model.Order;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by yns on 5/31/16.
 */

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.SimpleViewHolder> {

    private final Context mContext;
    private final RecyclerViewClickListener mListener;
    private List<Order> mList = new LinkedList<>();
    private int mSelectedId;

    public void setList(List<Order> list) {
        this.mList = list;
        notifyDataSetChanged();
    }

    public void setSelectedId(int selectedId) {
        mSelectedId = selectedId;
    }

    public static class SimpleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private final RecyclerViewClickListener mListener;
        private final TextView id;
        private final TextView type;
        private final TextView time;
        private final TextView items;
        private final View bg;
        private final TextView user_id;
        private final TextView comment;

        public SimpleViewHolder(View view, RecyclerViewClickListener listener) {
            super(view);
            bg = view.findViewById(R.id.bg);
            id = (TextView) view.findViewById(R.id.id);
            type = (TextView) view.findViewById(R.id.type);
            time = (TextView) view.findViewById(R.id.time);
            user_id = (TextView) view.findViewById(R.id.user_id);
            items = (TextView) view.findViewById(R.id.items);
            comment = (TextView) view.findViewById(R.id.comment);
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
    private CharSequence relativeTime(Date reservation_time) {
        long now = System.currentTimeMillis();
        return DateUtils.getRelativeTimeSpanString(reservation_time.getTime(), now, 0L, DateUtils.FORMAT_ABBREV_ALL);
    }
    @Override
    public void onBindViewHolder(SimpleViewHolder holder, final int position) {
        Order item = mList.get(position);
        holder.id.setText("" + item.id);
        holder.type.setText(item.orderType.name().toLowerCase());
        boolean reserve = item.reservation_time.getTime() != item.order_time.getTime();
        holder.time.setText((reserve ? "예약 " : "") + sdf.format(item.reservation_time) + "(" + relativeTime(item.reservation_time)+")");
        holder.items.setText(item.getOrderItemSummary());
        holder.user_id.setText(item.user_id + "/" + item.addr);
        if(item.comment != null && item.comment.length() > 0) {
            holder.comment.setText("" + item.comment);
            holder.comment.setVisibility(View.VISIBLE);
        } else {
            holder.comment.setVisibility(View.GONE);
        }
        if(mSelectedId == item.id) {
            holder.bg.setBackgroundResource(android.R.color.darker_gray);
        } else {
            holder.bg.setBackgroundResource(android.R.color.white);
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

}
